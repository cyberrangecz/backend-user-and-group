package cz.cyberrange.platform.userandgroup.api.dto.group;

/** Request body identifying a single user to add to a group. */
public class AddUserToGroupDTO {
  private Long userId;

  public AddUserToGroupDTO() {}

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
