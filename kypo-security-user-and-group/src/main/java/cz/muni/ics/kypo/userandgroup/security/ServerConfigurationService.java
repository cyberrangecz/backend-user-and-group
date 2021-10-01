package cz.muni.ics.kypo.userandgroup.security;

import cz.muni.ics.kypo.userandgroup.security.model.WellKnownOpenIDConfiguration;

public interface ServerConfigurationService {
    WellKnownOpenIDConfiguration getServerConfiguration(String var1);
}
