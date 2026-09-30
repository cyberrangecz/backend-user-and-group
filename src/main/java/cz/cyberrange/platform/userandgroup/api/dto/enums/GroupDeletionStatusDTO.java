package cz.cyberrange.platform.userandgroup.api.dto.enums;

/** The outcome of a request to delete a group, reported back to the caller. */
public enum GroupDeletionStatusDTO {
  /** The group is external valid and cannot be deleted. */
  EXTERNAL_VALID,
  /** The group was successfully deleted. */
  SUCCESS,
  /** Error when deleting a group. */
  ERROR,
  /** The group to delete could not be found. */
  NOT_FOUND,
  /** The group cannot be deleted because it is main group. */
  ERROR_MAIN_GROUP
}
