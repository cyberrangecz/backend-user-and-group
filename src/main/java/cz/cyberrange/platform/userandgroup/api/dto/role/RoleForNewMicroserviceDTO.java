package cz.cyberrange.platform.userandgroup.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/** A role defined by a microservice being registered, nested inside its registration request. */
@Schema(
    name = "RoleForNewMicroserviceDTO",
    description = "A role a microservice declares when it registers.")
public class RoleForNewMicroserviceDTO {

  @Schema(
      example = "ROLE_USER_AND_GROUP_ADMINISTRATOR",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "{role.roleType.NotEmpty.message}")
  private String roleType;

  @Schema(
      description = "Adds the role to the group every new user joins; at most one may set it.",
      example = "true",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "{role.isDefault.NotNull.message}")
  private boolean isDefault;

  @Schema(example = "This role will allow you to create and delete groups.")
  private String description;

  public String getRoleType() {
    return roleType;
  }

  public void setRoleType(String roleType) {
    this.roleType = roleType;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof RoleForNewMicroserviceDTO)) return false;
    RoleForNewMicroserviceDTO that = (RoleForNewMicroserviceDTO) object;
    return isDefault() == that.isDefault()
        && Objects.equals(getRoleType(), that.getRoleType())
        && Objects.equals(getDescription(), that.getDescription());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRoleType(), isDefault(), getDescription());
  }

  @Override
  public String toString() {
    return "RoleForNewMicroserviceDTO{"
        + "roleType='"
        + roleType
        + '\''
        + ", isDefault="
        + isDefault
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
