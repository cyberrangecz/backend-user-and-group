package cz.muni.ics.kypo.userandgroup.security.config;


import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("kypo.identity")
public class IdentityProvidersConfig {

    private List<IdentityProvider> providers = new ArrayList<>();

    @PostConstruct
    private void checkProviders() {
        if (this.providers.isEmpty()) {
            throw new BeanCreationException("Error creating configuration bean with name 'identityProvidersConfig': At least one identity provider must be configured.");
        }
        for (IdentityProvider provider : providers) {
            if (provider.getIssuer().isBlank()) {
                throw new BeanCreationException("Error creating configuration bean with name 'identityProvidersConfig': Property 'issuer' of the identity provider cannot be blank.");
            }
        }
    }

    public List<IdentityProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<IdentityProvider> providers) {
        this.providers = providers;
    }

    public Map<String, String> getUserInfoEndpointsMapping() {
        return providers.stream()
                .filter(ip -> ip.getUserInfoEndpoint() != null && !ip.getUserInfoEndpoint().isBlank())
                .collect(Collectors.toMap(IdentityProvider::getIssuer, IdentityProvider::getUserInfoEndpoint));
    }

    public Set<String> getSetOfIssuers() {
        return providers.stream()
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
