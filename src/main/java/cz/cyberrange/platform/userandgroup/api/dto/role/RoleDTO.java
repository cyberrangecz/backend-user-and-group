package cz.cyberrange.platform.userandgroup.api.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/** The detail of a role, together with the microservice it belongs to, returned to the caller. */
@Schema(name = "RoleDTO", description = "A role, with the microservice that defines it.")
public class RoleDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "ROLE_USER_AND_GROUP_ADMINISTRATOR")
  @JsonProperty("role_type")
  private String roleType;

  // Left unset unless the role was mapped together with its microservice.
  @Schema(example = "5")
  private Long idOfMicroservice;

  // Left unset unless the role was mapped together with its microservice.
  @Schema(example = "training")
  private String nameOfMicroservice;

  @Schema(example = "This role will allow you to create and delete groups.")
  private String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRoleType() {
    return roleType;
  }

  public void setRoleType(String roleType) {
    this.roleType = roleType;
  }

  public Long getIdOfMicroservice() {
    return idOfMicroservice;
  }

  public void setIdOfMicroservice(Long idOfMicroservice) {
    this.idOfMicroservice = idOfMicroservice;
  }

  public String getNameOfMicroservice() {
    return nameOfMicroservice;
  }

  public void setNameOfMicroservice(String nameOfMicroservice) {
    this.nameOfMicroservice = nameOfMicroservice;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof RoleDTO)) return false;
    RoleDTO roleDTO = (RoleDTO) object;
    return Objects.equals(getId(), roleDTO.getId())
        && Objects.equals(getRoleType(), roleDTO.getRoleType())
        && Objects.equals(getIdOfMicroservice(), roleDTO.getIdOfMicroservice())
        && Objects.equals(getNameOfMicroservice(), roleDTO.getNameOfMicroservice());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getRoleType(), getIdOfMicroservice(), getNameOfMicroservice());
  }

  @Override
  public String toString() {
    return "RoleDTO{"
        + "id="
        + id
        + ", roleType='"
        + roleType
        + '\''
        + ", idOfMicroservice="
        + idOfMicroservice
        + ", nameOfMicroservice='"
        + nameOfMicroservice
        + '\''
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
