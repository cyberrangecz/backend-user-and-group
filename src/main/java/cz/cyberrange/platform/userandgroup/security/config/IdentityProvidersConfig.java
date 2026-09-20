package cz.cyberrange.platform.userandgroup.security.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/** Holds the identity providers trusted by this application, bound from configuration. */
@Component
@EnableConfigurationProperties
@ConfigurationProperties("crczp.identity")
public class IdentityProvidersConfig {

  private List<IdentityProvider> providers = new ArrayList<>();

  /**
   * Rejects the bound configuration when no identity provider is configured, or when a configured
   * provider has a blank issuer.
   *
   * @throws BeanCreationException when either condition holds
   */
  @PostConstruct
  private void checkProviders() {
    if (this.providers.isEmpty()) {
      throw new BeanCreationException(
          "Error creating configuration bean with name 'identityProvidersConfig': At least one identity provider must be configured.");
    }
    for (IdentityProvider provider : providers) {
      if (provider.getIssuer().isBlank()) {
        throw new BeanCreationException(
            "Error creating configuration bean with name 'identityProvidersConfig': Property 'issuer' of the identity provider cannot be blank.");
      }
    }
  }

  public List<IdentityProvider> getProviders() {
    return providers;
  }

  public void setProviders(List<IdentityProvider> providers) {
    this.providers = providers;
  }

  /**
   * Returns the configured user info endpoint for each identity provider that has one. Providers
   * without a configured user info endpoint are left out of the map.
   *
   * @return issuer URLs mapped to their user info endpoint
   */
  public Map<String, String> getUserInfoEndpointsMapping() {
    return providers.stream()
        .filter(ip -> ip.getUserInfoEndpoint() != null && !ip.getUserInfoEndpoint().isBlank())
        .collect(
            Collectors.toMap(IdentityProvider::getIssuer, IdentityProvider::getUserInfoEndpoint));
  }

  /**
   * Returns the issuer of every configured identity provider.
   *
   * @return the configured issuers
   */
  public Set<String> getSetOfIssuers() {
    return providers.stream().map(IdentityProvider::getIssuer).collect(Collectors.toSet());
  }

  /** One identity provider entry bound from configuration. */
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
