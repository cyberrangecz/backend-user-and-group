package cz.muni.ics.kypo.userandgroup.security.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("kypo.idp")
public class IdentityProvidersConfig {

    private List<IdentityProvider> identityProviders;

    public List<IdentityProvider> getIdentityProviders() {
        return identityProviders;
    }

    public void setIdentityProviders(List<IdentityProvider> identityProviders) {
        this.identityProviders = identityProviders;
    }

    public Map<String, String> getUserInfoEndpointsMapping() {
        return identityProviders.stream()
                .filter(ip -> ip.getUserInfoEndpoint() != null && !ip.getUserInfoEndpoint().isBlank())
                .collect(Collectors.toMap(IdentityProvider::getIssuer, IdentityProvider::getUserInfoEndpoint));
    }

    public Set<String> getSetOfIssuers() {
        return identityProviders.stream()
                .map(IdentityProvider::getIssuer)
                .collect(Collectors.toSet());
    }

    private static class IdentityProvider {
        private String issuer;
        private String userInfoEndpoint;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getUserInfoEndpoint() {
            return userInfoEndpoint;
        }

        public void setUserInfoEndpoint(String userInfoEndpoint) {
            this.userInfoEndpoint = userInfoEndpoint;
        }
    }
}
