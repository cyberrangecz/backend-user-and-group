package cz.cyberrange.platform.userandgroup.api.dto;

import com.google.common.base.Objects;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserImportDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

/**
 * Carries the users to import together with the name of a new group to assign them to. Received as
 * the request body of the users import endpoint.
 */
@Schema(
    name = "UsersImportDTO",
    description = "Users to create, with the optional name of a new group to put them in.")
public class UsersImportDTO {

  @Valid private List<UserImportDTO> users = new ArrayList<>();

  private String groupName;

  public List<UserImportDTO> getUsers() {
    return users;
  }

  public void setUsers(List<UserImportDTO> users) {
    this.users = users;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UsersImportDTO)) return false;
    UsersImportDTO that = (UsersImportDTO) o;
    return Objects.equal(users, that.users) && Objects.equal(groupName, that.groupName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(users, groupName);
  }

  @Override
  public String toString() {
    return "UsersImportDTO{" + "users=" + users + ", groupName='" + groupName + '\'' + '}';
  }
}
