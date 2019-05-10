package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class UserDeletionResponseDTO {

    private UserDTO user;
    private UserDeletionStatusDTO status;

    @ApiModelProperty(value = "User to be deleted.")
    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @ApiModelProperty(value = "Status about result of deletion.", example = "SUCCESS")
    public UserDeletionStatusDTO getStatus() {
        return status;
    }

    public void setStatus(UserDeletionStatusDTO status) {
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
