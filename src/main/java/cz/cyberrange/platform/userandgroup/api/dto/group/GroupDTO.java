package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;

/** The full detail of a group, including its users and roles, returned to the caller. */
@Schema(
    name = "GroupDTO",
    description = "A group with its members and the roles granted through it.")
public class GroupDTO extends GroupBaseDTO {

  private Set<RoleDTO> roles = new HashSet<>();

  private Set<UserForGroupsDTO> users = new HashSet<>();

  public Set<UserForGroupsDTO> getUsers() {
    return users;
  }

  public void setUsers(Set<UserForGroupsDTO> users) {
    this.users = users;
  }

  public Set<RoleDTO> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleDTO> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "GroupDTO{"
        + "id="
        + getId()
        + ", name='"
        + getName()
        + '\''
        + ", description='"
        + getDescription()
        + '\''
        + '}';
  }
}
