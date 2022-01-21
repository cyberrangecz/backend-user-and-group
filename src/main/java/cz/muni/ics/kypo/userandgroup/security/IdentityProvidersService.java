package cz.muni.ics.kypo.userandgroup.security;

import cz.muni.ics.kypo.userandgroup.security.model.WellKnownOpenIDConfiguration;

public interface IdentityProvidersService {
    WellKnownOpenIDConfiguration getIdentityProviderConfiguration(String provider);
}
