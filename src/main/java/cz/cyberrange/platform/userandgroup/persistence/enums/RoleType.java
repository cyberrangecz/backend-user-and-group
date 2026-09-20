package cz.cyberrange.platform.userandgroup.persistence.enums;

/** Governs what a user assigned each role is authorized to do across the platform. */
public enum RoleType {
  /**
   * Gives rights of an administrator to a user. The user with this role can do all actions within
   * CyberRangeCZ Platform.
   */
  ROLE_USER_AND_GROUP_ADMINISTRATOR,
  /**
   * Gives rights of the power user to a user. The user with this role can retrieve any information
   * from CyberRangeCZ Platform.
   */
  ROLE_USER_AND_GROUP_POWER_USER,
  /**
   * Gives rights of the trainee to a user. The user with this role can only retrieve information
   * about himself and anonymized information about other users for the purposes of visualization.
   */
  ROLE_USER_AND_GROUP_TRAINEE
}
