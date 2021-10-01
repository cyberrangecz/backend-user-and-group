package cz.muni.ics.kypo.userandgroup.security.impl;

import cz.muni.ics.kypo.userandgroup.security.ClientConfigurationService;
import cz.muni.ics.kypo.userandgroup.security.model.ClientConfiguration;

import javax.annotation.PostConstruct;
import java.util.Map;

public class StaticClientConfigurationService implements ClientConfigurationService {
    private Map<String, ClientConfiguration> clients;

    public StaticClientConfigurationService() {
    }

    public Map<String, ClientConfiguration> getClients() {
        return this.clients;
    }

    public void setClients(Map<String, ClientConfiguration> clients) {
        this.clients = clients;
    }

    public ClientConfiguration getClientConfiguration(String issuer) {
        return this.clients.get(issuer);
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (this.clients == null || this.clients.isEmpty()) {
            throw new IllegalArgumentException("Clients map cannot be null or empty");
        }
    }
}
