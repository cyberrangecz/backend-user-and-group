package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;

public class UserDeletionResponseDTO {

    private UserDTO user;

    private UserDeletionStatus status;

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UserDeletionStatus getStatus() {
        return status;
    }

    public void setStatus(UserDeletionStatus status) {
        this.status = status;
    }
}
