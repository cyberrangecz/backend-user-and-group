package cz.cyberrange.platform.userandgroup.api.dto.enums;

/** Reports the outcome of a request to delete a user. */
public enum UserDeletionStatusDTO {
  /** User was successfully deleted. */
  SUCCESS,
  /** The user is external and still valid, so it cannot be deleted. */
  EXTERNAL_VALID,
  /** Error when deleting the user. */
  ERROR,
  /** User to delete cannot be found. */
  NOT_FOUND
}
