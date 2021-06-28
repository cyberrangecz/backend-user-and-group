package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about a deleted user.
 */
@ApiModel(value = "UserDeletionResponseDTO", description = "The information provided immediately to the client after the client deletes a given user.")
public class UserDeletionResponseDTO {

    @ApiModelProperty(value = "User to be deleted.")
    private UserDTO user;
    @ApiModelProperty(value = "Status about result of deletion.", example = "SUCCESS")
    private UserDeletionStatusDTO status;

    /**
     * Gets user who has been deleted.
     *
     * @return the {@link UserDTO} who has been deleted.
     */
    public UserDTO getUser() {
        return user;
    }

    /**
     * Sets user who has been deleted.
     *
     * @param user the {@link UserDTO} who has been deleted.
     */
    public void setUser(UserDTO user) {
        this.user = user;
    }

    /**
     * Gets the status of user deletion.
     *
     * @return the {@link UserDeletionStatusDTO} of user who has been deleted.
     */
    public UserDeletionStatusDTO getStatus() {
        return status;
    }

    /**
     * Sets the status of user deletion.
     *
     * @param status the {@link UserDeletionStatusDTO} of user who has been deleted.
     */
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
