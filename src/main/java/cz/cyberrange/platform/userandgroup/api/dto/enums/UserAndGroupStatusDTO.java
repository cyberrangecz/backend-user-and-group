package cz.cyberrange.platform.userandgroup.api.dto.enums;

/** Governs the lifecycle status of a user or group. */
public enum UserAndGroupStatusDTO {
  /** The user or group is active and not marked for deletion. */
  VALID,
  /** The user or group is marked for deletion. */
  DELETED,
  DIRTY
}
