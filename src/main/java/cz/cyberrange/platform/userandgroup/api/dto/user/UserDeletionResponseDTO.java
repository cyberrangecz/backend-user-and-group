package cz.cyberrange.platform.userandgroup.api.dto.user;

import cz.cyberrange.platform.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Pairs a deleted user with the outcome of the deletion attempt. Returned to the client immediately
 * after a delete request.
 */
@Schema(
    name = "UserDeletionResponseDTO",
    description = "Outcome of deleting one user, paired with the user itself.")
public class UserDeletionResponseDTO {

  private UserDTO user;

  @Schema(example = "SUCCESS")
  private UserDeletionStatusDTO status;

  public UserDTO getUser() {
    return user;
  }

  public void setUser(UserDTO user) {
    this.user = user;
  }

  public UserDeletionStatusDTO getStatus() {
    return status;
  }

  public void setStatus(UserDeletionStatusDTO status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof UserDeletionResponseDTO)) return false;
    UserDeletionResponseDTO that = (UserDeletionResponseDTO) object;
    return Objects.equals(getUser(), that.getUser()) && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUser(), getStatus());
  }

  @Override
  public String toString() {
    return "UserDeletionResponseDTO{" + "user=" + user + ", status=" + status + '}';
  }
}
