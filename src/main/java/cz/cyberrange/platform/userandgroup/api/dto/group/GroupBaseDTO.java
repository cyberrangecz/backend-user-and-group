package cz.cyberrange.platform.userandgroup.api.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Objects;

/** The state every group representation returned to the caller carries. */
public abstract class GroupBaseDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
  private Long id;

  @Schema(example = "Main group of organizers")
  private String name;

  @Schema(example = "Organizers group for training run in June.")
  private String description;

  @Schema(
      description = "Set by the server; false for the groups it creates itself.",
      example = "false")
  private boolean canBeDeleted = true;

  @Schema(example = "2017-10-19T10:23:54")
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

  public boolean isCanBeDeleted() {
    return canBeDeleted;
  }

  public void setCanBeDeleted(boolean canBeDeleted) {
    this.canBeDeleted = canBeDeleted;
  }

  public LocalDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  /**
   * Compares this group with the given object on the group's identity, so that two groups of the
   * same representation are equal when they carry the same id and name.
   *
   * @param object object to compare with
   * @return whether the given object is a group of the same representation and identity
   */
  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    GroupBaseDTO groupDTO = (GroupBaseDTO) object;
    return Objects.equals(getId(), groupDTO.getId())
        && Objects.equals(getName(), groupDTO.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName());
  }
}
