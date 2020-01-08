package cz.muni.ics.kypo.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

/**
 * Enumeration of role types.
 *
 */
@ApiModel(value = "RoleTypeDTO",
        description = "The types of a default roles.")
public enum RoleTypeDTO {
    /**
     * The role of administrator.
     */
    ADMINISTRATOR,
    /**
     * The role of user.
     */
    USER,
    /**
     * Base role of guest.
     */
    GUEST;
}
