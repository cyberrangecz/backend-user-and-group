package cz.cyberrange.platform.userandgroup.api.dto.microservice;

import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/** Request body for registering a new microservice together with the roles it defines. */
@Schema(
    name = "NewMicroserviceDTO",
    description = "A service to register, together with the roles it defines.")
public class NewMicroserviceDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "training")
  @NotEmpty(message = "{microservice.name.NotEmpty.message}")
  private String name;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "/training/api/v1")
  @NotEmpty(message = "{microservice.endpoint.NotEmpty.message}")
  private String endpoint;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @Valid
  @NotNull(message = "{microservice.roles.NotNull.message}")
  private Set<@NotNull RoleForNewMicroserviceDTO> roles;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public Set<RoleForNewMicroserviceDTO> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleForNewMicroserviceDTO> roles) {
    this.roles = roles;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof NewMicroserviceDTO)) return false;
    NewMicroserviceDTO that = (NewMicroserviceDTO) object;
    return Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return "NewMicroserviceDTO{"
        + "name='"
        + name
        + '\''
        + ", endpoint='"
        + endpoint
        + '\''
        + ", roles="
        + roles
        + '}';
  }
}
