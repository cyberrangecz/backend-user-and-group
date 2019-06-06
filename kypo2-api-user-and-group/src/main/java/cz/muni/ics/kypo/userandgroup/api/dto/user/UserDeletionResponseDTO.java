package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
    public boolean equals(Object object) {
        if (!(object instanceof UserDeletionResponseDTO)) return false;
        UserDeletionResponseDTO that = (UserDeletionResponseDTO) object;
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
