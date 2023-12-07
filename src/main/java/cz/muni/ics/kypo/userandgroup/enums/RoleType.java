package cz.muni.ics.kypo.userandgroup.enums;

/**
 * Enumeration of a role type.
 */
public enum RoleType {
    /**
     * Gives rights of an administrator to a user. The user with this role can do all
     * actions within the project of kypo-user-and-group.
     */
    ROLE_USER_AND_GROUP_ADMINISTRATOR,
    /**
     * Gives rights of the power user to a user. The user with this role can retrieve any information
     * from the project kypo-user-and-group.
     */
    ROLE_USER_AND_GROUP_POWER_USER,
    /**
     * Gives rights of the trainee to a user. The user with this role can only retrieve information about himself and
     * anonymized information about other users for the purposes of visualization.
     */
    ROLE_USER_AND_GROUP_TRAINEE
}
