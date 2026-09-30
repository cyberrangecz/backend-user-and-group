package cz.cyberrange.platform.userandgroup.persistence.enums;

/** Governs whether a user or group is active or marked for deletion. */
public enum UserAndGroupStatus {
  /**
   * Valid: group - either an internal group or external group which is not marked as "to be
   * deleted". user - a complement to invalid users, i.e. either internal user or external user
   * whose account is not marked as "to be deleted"
   */
  VALID,
  /**
   * Deleted: group - an external group marked as "to be deleted". user - an external user whose
   * account is marked as "to be deleted"
   */
  DELETED
}
