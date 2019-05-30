package cz.muni.ics.kypo.userandgroup.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jan Duda
 * @author Pavel Seda
 */
public class UsersWrapper {

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
        this.roles = new HashSet<>(roles);
    }

    @Override
    public String toString() {
        return "UsersWrapper{" +
                "user=" + user +
                ", roles=" + roles +
                '}';
    }
}
