package cz.muni.ics.kypo.userandgroup.security.enums;

/**
 * @author Pavel Seda
 */
public enum SpringProfiles {

    PROD("PROD", ""),
    DEV("DEV", "");

    private String name;
    private String description;

    SpringProfiles(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
