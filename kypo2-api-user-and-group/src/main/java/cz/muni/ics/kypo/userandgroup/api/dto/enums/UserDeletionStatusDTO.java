package cz.muni.ics.kypo.userandgroup.api.dto.enums;

/**
 * Enumeration of user deletion statuses.
 *
 * @author Jan Duda
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
