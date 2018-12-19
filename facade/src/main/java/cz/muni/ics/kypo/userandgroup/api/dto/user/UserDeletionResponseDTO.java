package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDeletionResponseDTO that = (UserDeletionResponseDTO) o;
        return Objects.equals(getUser(), that.getUser()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getStatus());
    }

    @Override
    public String toString() {
        return "UserDeletionResponseDTO{" +
                "user=" + user +
                ", status=" + status +
                '}';
    }
}
