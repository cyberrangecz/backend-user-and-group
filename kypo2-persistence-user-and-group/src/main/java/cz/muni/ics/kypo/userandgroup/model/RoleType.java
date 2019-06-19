package cz.muni.ics.kypo.userandgroup.model;

/**
 * Enumeration of a role type.
 *
 * @author Dominik Pilar
 */
public enum RoleType {
    /**
     * Gives rights of an administrator to a user. The user with this role can do all
     * actions within the project of kypo2-user-and-group.
     */
    ROLE_USER_AND_GROUP_ADMINISTRATOR,
    /**
     * Gives rights of the user to a user.
     */
    ROLE_USER_AND_GROUP_USER,
    /**
     * Gives rights of the guest to a user. User with this role cannot do anything.
     */
    ROLE_USER_AND_GROUP_GUEST;
}
