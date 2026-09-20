package cz.cyberrange.platform.userandgroup.security;

import cz.cyberrange.platform.userandgroup.security.model.WellKnownOpenIDConfiguration;

/** Looks up the published OpenID Connect configuration for a trusted identity provider. */
public interface IdentityProvidersService {

  /**
   * Returns the published OpenID Connect configuration for the given identity provider.
   *
   * @param provider issuer URL of the identity provider
   * @return the provider's configuration, or null when it could not be loaded
   * @throws org.springframework.security.authentication.AuthenticationServiceException when no
   *     identity provider is configured for that issuer
   */
  WellKnownOpenIDConfiguration getIdentityProviderConfiguration(String provider);
}
