package cz.muni.ics.kypo.userandgroup.security;


import cz.muni.ics.kypo.userandgroup.security.model.ClientConfiguration;

public interface ClientConfigurationService {
    ClientConfiguration getClientConfiguration(String issuer);
}
