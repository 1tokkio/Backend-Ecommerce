package cl.duoc.pedidos360.auditoria.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

// Resource Server de dos proveedores OIDC: Azure para administradores, Cognito para clientes.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProviderProperties.class)
public class SecurityConfig {

    private final JwtProviderProperties providerProperties;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtProviderProperties providerProperties,
                          @Value("${cors.allowed-origins}") List<String> origenesPermitidos) {
        this.providerProperties = providerProperties;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auditoria/estado").permitAll()
                        // Los eventos del sistema son enteramente de administracion.
                        .anyRequest().hasRole("Admin")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    // Un decoder por proveedor; MultiIssuerJwtDecoder enruta por "iss".
    @Bean
    public JwtDecoder jwtDecoder() {
        Map<String, JwtDecoder> decodersPorEmisor = new LinkedHashMap<>();

        for (JwtProviderProperties.Provider provider : providerProperties.getProviders()) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(provider.getJwkSetUri()).build();

            List<OAuth2TokenValidator<Jwt>> validadores = new ArrayList<>();
            for (String issuer : provider.getIssuers()) {
                validadores.add(JwtValidators.createDefaultWithIssuer(issuer));
            }
            if (!provider.getAudiences().isEmpty()) {
                validadores.add(audienceValidator(provider.getAudiences()));
            }
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validadores));

            for (String issuer : provider.getIssuers()) {
                decodersPorEmisor.put(issuer, decoder);
            }
        }

        return new MultiIssuerJwtDecoder(decodersPorEmisor);
    }

    // Azure y el ID token de Cognito usan "aud"; el ACCESS token de Cognito usa
    // "client_id" en su lugar (asi no sirve donde se espera un ID token).
    private OAuth2TokenValidator<Jwt> audienceValidator(List<String> audiences) {
        return token -> {
            List<String> aud = token.getClaimAsStringList("aud");
            List<String> clientId = token.getClaimAsStringList("client_id");

            boolean aceptada = contieneUno(aud, audiences) || contieneUno(clientId, audiences);

            if (aceptada) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token",
                    "El token no fue emitido para este backend. aud: " + aud + ", client_id: " + clientId, null));
        };
    }

    private boolean contieneUno(List<String> valores, List<String> esperados) {
        return valores != null && valores.stream().anyMatch(esperados::contains);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::autoridades);
        return converter;
    }

    // Azure usa "roles"; Cognito usa "cognito:groups", pero solo en el ID token,
    // no en el access token. Por eso el access token de Cognito llega sin rol y
    // se aplica el default-role como respaldo.
    private Collection<GrantedAuthority> autoridades(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new LinkedHashSet<>();

        authorities.addAll(rolesDelClaim(jwt, "roles"));
        authorities.addAll(rolesDelClaim(jwt, "cognito:groups"));

        if (authorities.isEmpty()) {
            authorities.addAll(rolPorDefecto(jwt));
        }

        return authorities;
    }

    private Collection<GrantedAuthority> rolesDelClaim(Jwt jwt, String claim) {
        List<String> roles = jwt.getClaimAsStringList(claim);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(rol -> rol != null && !rol.isBlank())
                .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + rol.trim()))
                .toList();
    }

    private Collection<GrantedAuthority> rolPorDefecto(Jwt jwt) {
        String issuer = jwt.getClaimAsString("iss");
        if (issuer == null) {
            return List.of();
        }

        for (JwtProviderProperties.Provider provider : providerProperties.getProviders()) {
            String rol = provider.getDefaultRole();
            if (rol == null || rol.isBlank()) {
                continue;
            }
            if (provider.getIssuers().contains(issuer)) {
                return List.of(new SimpleGrantedAuthority("ROLE_" + rol.trim()));
            }
        }

        return List.of();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
