package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.utils.converters.LocalDateTimeUTCSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Request body for creating a new group, either with an explicit set of users or by importing every
 * user of the given groups.
 */
@Schema(
    name = "NewGroupDTO",
    description = "A group to create, with the groups its first members are taken from.")
public class NewGroupDTO {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Main group")
  @NotEmpty(message = "{group.name.NotEmpty.message}")
  private String name;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Group for main users.")
  @NotEmpty(message = "{group.description.NotEmpty.message}")
  private String description;

  @Schema(example = "2019-11-20T10:28:02.727")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime expirationDate;

  private Set<UserForGroupsDTO> users = new HashSet<>();

  @Schema(
      description = "Ids of the groups whose members are copied into the new group.",
      example = "[1]")
  private List<Long> groupIdsOfImportedUsers = new ArrayList<>();

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

  public List<Long> getGroupIdsOfImportedUsers() {
    return groupIdsOfImportedUsers;
  }

  public void setGroupIdsOfImportedUsers(List<Long> groupIdsOfImportedUsers) {
    this.groupIdsOfImportedUsers = groupIdsOfImportedUsers;
  }

  public LocalDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof NewGroupDTO)) return false;
    NewGroupDTO that = (NewGroupDTO) object;
    return Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return "NewGroupDTO{"
        + "name='"
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
