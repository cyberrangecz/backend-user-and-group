package cz.cyberrange.platform.userandgroup.api.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/** Request body identifying an existing group and the values to update it with. */
@Schema(name = "UpdateGroupDTO", description = "New values for an existing group, named by its id.")
public class UpdateGroupDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  @NotNull(message = "{group.id.NotNull.message}")
  private Long id;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Main group.")
  @NotEmpty(message = "{group.name.NotEmpty.message}")
  private String name;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Group for main users.")
  @NotEmpty(message = "{group.description.NotEmpty.message}")
  private String description;

  @Schema(example = "2019-11-20T10:28:02.727Z")
  private LocalDateTime expirationDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof UpdateGroupDTO)) return false;
    UpdateGroupDTO that = (UpdateGroupDTO) object;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName());
  }

  @Override
  public String toString() {
    return "UpdateGroupDTO{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", expirationDate="
        + expirationDate
        + '}';
  }
}
