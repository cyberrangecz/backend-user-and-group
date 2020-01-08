package cz.muni.ics.kypo.userandgroup.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used for loading and wrapping {@link User} with his {@link cz.muni.ics.kypo.userandgroup.model.Role}s from the configuration file.
 *
 */
public class UsersWrapper {

    @JsonIgnoreProperties({"id"})
    private User user;
    private Set<RoleType> roles = new HashSet<>();
    private String iss;

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
     * Sets {@link cz.muni.ics.kypo.userandgroup.model.Role}s to the {@link User}.
     *
     * @param roles roles to be set for the {@link User}
     */
    public void setRoles(Set<RoleType> roles) {
        this.roles = new HashSet<>(roles);
    }

    /**
     * Gets the URI of oidc provider which is used to authenticate the user.
     *
     * @return issuer - URI of the oidc provider.
     */
    public String getIss() {
        return iss;
    }

    /**
     * Sets URI of the oidc provider.
     *
     * @param iss issuer - URI of the oidc provider
     */
    public void setIss(String iss) {
        this.iss = iss;
    }

    @Override
    public String toString() {
        return "UsersWrapper{" +
                "user=" + user +
                ", roles=" + roles +
                ", iss=" + iss +
                '}';
    }
}
