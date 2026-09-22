package cl.duoc.pedidos360.ordenes.config;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * El microservicio actua como Resource Server: no emite tokens, solo los valida.
 * Se comprueban las cuatro cosas que hacen valido un token de Microsoft Entra ID:
 * la firma (contra las claves publicas del tenant), el emisor, la audiencia y la vigencia.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String issuerUri;
    private final String tenantId;
    private final String clientId;
    private final List<String> origenesPermitidos;

    public SecurityConfig(@Value("${azure.issuer-uri}") String issuerUri,
                          @Value("${azure.tenant-id}") String tenantId,
                          @Value("${azure.client-id}") String clientId,
                          @Value("${cors.allowed-origins}") List<String> origenesPermitidos) {
        this.issuerUri = issuerUri;
        this.tenantId = tenantId;
        this.clientId = clientId;
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
                        .requestMatchers("/api/v1/ordenes/estado").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                validadorDeEmisor(),
                validadorDeAudiencia()
        ));
        return decoder;
    }

    /**
     * Las claves publicas viven junto al emisor, cambiando el sufijo /v2.0 por
     * /discovery/v2.0/keys. Derivarlo del issuer y no dejarlo escrito a mano
     * permite cambiar de tenant tocando una sola variable de entorno.
     */
    private String jwkSetUri() {
        String base = issuerUri.endsWith("/") ? issuerUri.substring(0, issuerUri.length() - 1) : issuerUri;
        if (base.endsWith("/v2.0")) {
            base = base.substring(0, base.length() - "/v2.0".length());
        }
        return base + "/discovery/v2.0/keys";
    }

    /**
     * Entra ID emite tokens v2 con el issuer configurado, pero si la aplicacion
     * quedo con accessTokenAcceptedVersion en null emite tokens v1, cuyo issuer
     * es sts.windows.net. Aceptamos los dos para no romper segun como este el manifiesto.
     */
    private OAuth2TokenValidator<Jwt> validadorDeEmisor() {
        String emisorV1 = "https://sts.windows.net/" + tenantId + "/";
        return jwt -> {
            String emisor = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
            if (emisor.equals(issuerUri) || emisor.equals(emisorV1)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_issuer", "El emisor " + emisor + " no corresponde al tenant configurado", null));
        };
    }

    /**
     * El token tiene que venir dirigido a esta API. Segun la version del token la
     * audiencia llega como el client id pelado o como el Application ID URI.
     */
    private OAuth2TokenValidator<Jwt> validadorDeAudiencia() {
        return jwt -> {
            List<String> audiencia = jwt.getAudience();
            if (audiencia != null && (audiencia.contains(clientId) || audiencia.contains("api://" + clientId))) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_audience", "El token no fue emitido para esta API", null));
        };
    }

    /**
     * Spring Security espera el prefijo ROLE_ para hasRole(), y SCOPE_ para hasAuthority().
     * Los app roles de Entra llegan en el claim "roles" y los permisos delegados en "scp".
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter conversorDeScopes = new JwtGrantedAuthoritiesConverter();
        conversorDeScopes.setAuthorityPrefix("SCOPE_");
        conversorDeScopes.setAuthoritiesClaimName("scp");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> permisos = new ArrayList<>(conversorDeScopes.convert(jwt));
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(rol -> permisos.add(new SimpleGrantedAuthority("ROLE_" + rol)));
            }
            return permisos;
        });
        return converter;
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
