package cz.cyberrange.platform.userandgroup.api.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request body listing the users to add to a group, either directly by id or by importing every
 * user of the given groups.
 */
@Schema(
    name = "AddUsersToGroupDTO",
    description = "The users to add to a group, named directly or taken from other groups.")
public class AddUsersToGroupDTO {

  @Schema(example = "[1,2]")
  private List<Long> idsOfUsersToBeAdd = new ArrayList<>();

  @Schema(
      description = "Ids of the groups whose members are copied into the group.",
      example = "[1,2]")
  private List<Long> idsOfGroupsOfImportedUsers = new ArrayList<>();

  public List<Long> getIdsOfUsersToBeAdd() {
    return idsOfUsersToBeAdd;
  }

  public void setIdsOfUsersToBeAdd(List<Long> idsOfUsersToBeAdd) {
    this.idsOfUsersToBeAdd = idsOfUsersToBeAdd;
  }

  public List<Long> getIdsOfGroupsOfImportedUsers() {
    return idsOfGroupsOfImportedUsers;
  }

  public void setIdsOfGroupsOfImportedUsers(List<Long> idsOfGroupsOfImportedUsers) {
    this.idsOfGroupsOfImportedUsers = idsOfGroupsOfImportedUsers;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof AddUsersToGroupDTO)) return false;
    AddUsersToGroupDTO that = (AddUsersToGroupDTO) object;
    return Objects.equals(getIdsOfUsersToBeAdd(), that.getIdsOfUsersToBeAdd())
        && Objects.equals(getIdsOfGroupsOfImportedUsers(), that.getIdsOfGroupsOfImportedUsers());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIdsOfUsersToBeAdd(), getIdsOfGroupsOfImportedUsers());
  }

  @Override
  public String toString() {
    return "AddUsersToGroupDTO{"
        + ", idsOfUsersToBeAdd="
        + idsOfUsersToBeAdd
        + ", idsOfGroupsOfImportedUsers="
        + idsOfGroupsOfImportedUsers
        + '}';
  }
}
