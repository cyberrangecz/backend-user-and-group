package cz.muni.ics.kypo.userandgroup.startup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used for loading and wrapping {@link User} with his {@link cz.muni.ics.kypo.userandgroup.domain.Role}s from the configuration file.
 */
public class UsersWrapper {

    @JsonIgnoreProperties({"id"})
    private User user;
    private Set<RoleType> roles = new HashSet<>();

    /**
     * Gets an instance of a user.
     *
     * @return the {@link User} inserted to system through the configuration file.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets new user.
     *
     * @param user the {@link User} to be wrapped by this class.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the roles of the user.
     *
     * @return the roles of the {@link User} inserted through the configuration file.
     */
    public Set<RoleType> getRoles() {
        return roles;
    }

    /**
     * Sets {@link cz.muni.ics.kypo.userandgroup.domain.Role}s to the {@link User}.
     *
     * @param roles roles to be set for the {@link User}
     */
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
