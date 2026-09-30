package cz.cyberrange.platform.userandgroup.startup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds a user together with the role types assigned to it, as read from the initial users
 * configuration file.
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

  /**
   * Stores a copy of the given roles, leaving the given set unaffected by later changes to this
   * instance.
   *
   * @param roles roles to copy
   */
  public void setRoles(Set<RoleType> roles) {
    this.roles = new HashSet<>(roles);
  }

  @Override
  public String toString() {
    return "UsersWrapper{" + "user=" + user + ", roles=" + roles + '}';
  }
}
