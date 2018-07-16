package cz.muni.ics.kypo.userandgroup.mapping.userswrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;

import java.util.HashSet;
import java.util.Set;

public class UserWrapper {

    @JsonIgnoreProperties({"id"})
    private User user;

    private Set<RoleType> roles = new HashSet<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleType> roles) {
        this.roles = roles;
    }
}
