package cz.muni.ics.kypo.userandgroup.enums.dto;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "UserAndGroupStatusDTO", description = "The user and group status.")
public enum UserAndGroupStatusDTO {
    VALID, DELETED, DIRTY
}
