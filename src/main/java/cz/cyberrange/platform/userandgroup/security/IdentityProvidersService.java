package cz.cyberrange.platform.userandgroup.security;

import cz.cyberrange.platform.userandgroup.security.model.WellKnownOpenIDConfiguration;

public interface IdentityProvidersService {
    WellKnownOpenIDConfiguration getIdentityProviderConfiguration(String provider);
}
