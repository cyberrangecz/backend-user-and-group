package cz.cyberrange.platform.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "UserAndGroupStatusDTO", description = "The user and group status.")
public enum UserAndGroupStatusDTO {
    VALID, DELETED, DIRTY
}
