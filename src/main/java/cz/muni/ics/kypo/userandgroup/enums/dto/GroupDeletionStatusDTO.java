package cz.muni.ics.kypo.userandgroup.enums.dto;

import io.swagger.annotations.ApiModel;

/**
 * Enumeration of group deletion statuses.
 */
@ApiModel(value = "GroupDeletionStatusDTO",
        description = "The statuses that are used in a group deletion.")
public enum GroupDeletionStatusDTO {
    /**
     * The group is external valid and cannot be deleted.
     */
    EXTERNAL_VALID,
    /**
     * The group was successfully deleted.
     */
    SUCCESS,
    /**
     * Error when deleting a group.
     */
    ERROR,
    /**
     * The group to delete could not be found.
     */
    NOT_FOUND,
    /**
     * The group cannot be deleted because it is main group.
     */
    ERROR_MAIN_GROUP

}
