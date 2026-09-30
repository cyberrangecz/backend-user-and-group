package cz.cyberrange.platform.userandgroup.api.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;

/** The basic detail of a group, without its users and roles, returned to the caller. */
@Schema(
    name = "GroupViewDTO",
    description = "A group on its own, without its members or its roles.")
public class GroupViewDTO extends GroupBaseDTO {

  @Override
  public String toString() {
    return "GroupViewDTO{"
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
