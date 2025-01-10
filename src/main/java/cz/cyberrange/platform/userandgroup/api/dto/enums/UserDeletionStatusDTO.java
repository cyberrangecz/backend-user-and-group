package cz.cyberrange.platform.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

/**
 * Enumeration of user deletion statuses.
 */
@ApiModel(value = "UserDeletionStatusDTO", description = "The user and deletion status.")
public enum UserDeletionStatusDTO {
    /**
     * User was successfully deleted.
     */
    SUCCESS,
    /**
     * The user is external valid and therefore cannot be deleted.
     */
    EXTERNAL_VALID,
    /**
     * Error when deleting the user.
     */
    ERROR,
    /**
     * User to delete cannot be found.
     */
    NOT_FOUND
}
