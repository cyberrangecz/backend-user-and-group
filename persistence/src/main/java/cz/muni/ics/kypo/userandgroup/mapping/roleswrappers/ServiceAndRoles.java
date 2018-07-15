package cz.muni.ics.kypo.userandgroup.mapping.roleswrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ServiceAndRoles {

    private String service;
    private List<RoleWrapper> roles;

    @JsonCreator
    public ServiceAndRoles(@JsonProperty(value = "service", required = true) String service,
                           @JsonProperty(value = "roles", required = true) List<RoleWrapper> roles) {
        this.service = service;
        if (roles == null) {
            this.roles = new ArrayList<>();
        } else {
            this.roles = roles;
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<RoleWrapper> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWrapper> roles) {
        if (roles == null) {
            this.roles = new ArrayList<>();
        } else {
            this.roles = roles;
        }
    }
}
