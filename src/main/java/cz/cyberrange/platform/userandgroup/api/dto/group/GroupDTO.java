package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.api.dto.enums.SourceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.enums.UserAndGroupStatusDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.utils.converters.LocalDateTimeUTCSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/** The full detail of a group, including its users and roles, returned to the caller. */
@Schema(
    name = "GroupDTO",
    description = "A group with its members and the roles granted through it.")
public class GroupDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "Main group of organizers")
  private String name;

  @Schema(example = "Organizers group for training run in June.")
  private String description;

  private Set<RoleDTO> roles = new HashSet<>();

  private Set<UserForGroupsDTO> users = new HashSet<>();

  @Schema(description = "Where the group comes from.", example = "INTERNAL")
  private SourceDTO source;

  @Schema(
      description = "Set by the server; false for the groups it creates itself.",
      example = "false")
  private boolean canBeDeleted = true;

  @Schema(example = "2017-10-19T10:23:54")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime expirationDate;

  /**
   * Sets the group's source from the given external id: internal when the id is absent, perun
   * otherwise.
   *
   * @param externalId the group's external identifier, or null for an internal group
   */
  public void convertExternalIdToSource(Long externalId) {
    if (externalId == null) {
      this.source = SourceDTO.INTERNAL;
    } else {
      this.source = SourceDTO.PERUN;
    }
  }

  /**
   * Sets whether the group can be deleted from the given status: true when deleted, false when
   * valid. Leaves the current value unchanged for any other status.
   *
   * @param status status to derive the flag from
   */
  public void convertStatusToCanBeDeleted(UserAndGroupStatusDTO status) {
    if (status.equals(UserAndGroupStatusDTO.DELETED)) {
      this.canBeDeleted = true;
    }
    if (status.equals(UserAndGroupStatusDTO.VALID)) {
      this.canBeDeleted = false;
    }
  }

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

  public SourceDTO getSource() {
    return source;
  }

  public void setSource(SourceDTO source) {
    this.source = source;
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

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof GroupDTO)) return false;
    GroupDTO groupDTO = (GroupDTO) object;
    return Objects.equals(getId(), groupDTO.getId())
        && Objects.equals(getName(), groupDTO.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName());
  }

  @Override
  public String toString() {
    return "GroupDTO{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
