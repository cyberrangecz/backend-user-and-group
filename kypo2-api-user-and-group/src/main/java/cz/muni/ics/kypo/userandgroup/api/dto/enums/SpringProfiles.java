package cz.muni.ics.kypo.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "SpringProfiles", description = "The environment profiles.")
public enum SpringProfiles {
    /**
     * The PROD profile.
     */
    PROD("PROD"),
    /**
     * The DEV profile.
     */
    DEV("DEV");

    private String name;

    SpringProfiles(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
