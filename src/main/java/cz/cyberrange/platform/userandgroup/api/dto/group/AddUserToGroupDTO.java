package cz.cyberrange.platform.userandgroup.api.dto.group;

public class AddUserToGroupDTO {
    private Long userId;

    public AddUserToGroupDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
