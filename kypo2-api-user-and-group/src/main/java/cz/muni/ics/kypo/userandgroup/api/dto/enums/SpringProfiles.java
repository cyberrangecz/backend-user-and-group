package cz.muni.ics.kypo.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

/**
 * @author Pavel Seda
 */
@ApiModel(value = "SpringProfiles",
        description = "The environment profiles.")
public enum SpringProfiles {
    /**
     * The PROD profile.
     */
    PROD,
    /**
     * The DEV profile.
     */
    DEV;
}
