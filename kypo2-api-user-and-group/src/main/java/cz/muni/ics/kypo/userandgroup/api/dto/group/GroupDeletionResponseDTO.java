package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Data transfer object used when some group is deleted.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class GroupDeletionResponseDTO {

    private Long id;
    private GroupDeletionStatusDTO status;

    /**
     * Gets the ID of the group.
     *
     * @return the ID of the group
     */
    @ApiModelProperty(value = "Main identifiers of deleted group.", example = "1")
    public Long getId() {
        return id;
    }

    /**
     * Sets ID of group.
     *
     * @param id the ID of group.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the status of the deletion.
     *
     * @return the status of the deletion {@link cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO}.
     */
    @ApiModelProperty(value = "Result of deleting group: \n" +
            "1) SUCCESS - group was deleted\n " +
            "2) NOT_FOUND - group has not found\n" +
            "3) ERROR - group could not be deleted, try it later\n" +
            "4) ERROR_MAIN_GROUP - group cannot be deleted due to it is one of the main group for roles (ADMINISTRATOR, USER, GUEST)", example = "SUCCESS")
    public GroupDeletionStatusDTO getStatus() {
        return status;
    }

    /**
     * Sets the status of the deletion.
     *
     * @param status the status of the deletion {@link cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO}.
     */
    public void setStatus(GroupDeletionStatusDTO status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GroupDeletionResponseDTO)) return false;
        GroupDeletionResponseDTO that = (GroupDeletionResponseDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStatus());
    }

    @Override
    public String toString() {
        return "GroupDeletionResponseDTO{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}
