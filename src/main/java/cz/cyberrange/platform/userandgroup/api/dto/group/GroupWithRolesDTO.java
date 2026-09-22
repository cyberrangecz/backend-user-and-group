package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;

/** The detail of a group together with its roles, returned to the caller. */
@Schema(
    name = "GroupWithRolesDTO",
    description = "A group with the roles granted through it, without its members.")
public class GroupWithRolesDTO extends GroupBaseDTO {

  private Set<RoleDTO> roles = new HashSet<>();

  public Set<RoleDTO> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleDTO> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "GroupWithRolesDTO{"
        + "id="
        + getId()
        + ", name='"
        + getName()
        + '\''
        + ", description='"
        + getDescription()
        + '\''
        + ", canBeDeleted="
        + isCanBeDeleted()
        + ", expirationDate="
        + getExpirationDate()
        + '}';
  }
}
