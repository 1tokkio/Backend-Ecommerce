package cl.duoc.pedidos360.productos.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProviderProperties {

    private List<Provider> providers = new ArrayList<>();

    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    public static class Provider {

        private String name;
        private String jwkSetUri;
        private List<String> issuers = new ArrayList<>();
        private List<String> audiences = new ArrayList<>();
        private String defaultRole;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }

        public List<String> getIssuers() {
            return issuers;
        }

        public void setIssuers(List<String> issuers) {
            this.issuers = issuers;
        }

        public List<String> getAudiences() {
            return audiences;
        }

        public void setAudiences(List<String> audiences) {
            this.audiences = audiences;
        }

        public String getDefaultRole() {
            return defaultRole;
        }

        public void setDefaultRole(String defaultRole) {
            this.defaultRole = defaultRole;
        }
    }
}
