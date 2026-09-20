package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.api.dto.enums.SourceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.utils.converters.LocalDateTimeUTCSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/** The detail of a group together with its roles, returned to the caller. */
@Schema(
    name = "GroupWithRolesDto",
    description = "A group with the roles granted through it, without its members.")
public class GroupWithRolesDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "Main group of organizers")
  private String name;

  @Schema(example = "Organizers group for training run in June.")
  private String description;

  private Set<RoleDTO> roles = new HashSet<>();

  @Schema(description = "Where the group comes from.", example = "INTERNAL")
  private SourceDTO source;

  @Schema(
      description = "Set by the server; false for the groups it creates itself.",
      example = "false")
  private boolean canBeDeleted = true;

  @Schema(example = "2017-10-19T10:23:54")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime expirationDate;

  public GroupWithRolesDTO() {}

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

  public Set<RoleDTO> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleDTO> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "GroupWithRolesDto{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", source="
        + source
        + ", canBeDeleted="
        + canBeDeleted
        + ", expirationDate="
        + expirationDate
        + '}';
  }
}
