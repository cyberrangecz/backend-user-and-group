package cz.cyberrange.platform.userandgroup.persistence.enums.dto;

/**
 * The enum Implicit group names of main microservice <strong>User-and-group</strong>.
 */
public enum ImplicitGroupNames {

    /**
     * Implicit name for default group which contains default role of all microservices.
     */
    DEFAULT_GROUP("DEFAULT-GROUP"),
    /**
     * Implicit group name for administrators.
     */
    USER_AND_GROUP_ADMINISTRATOR("USER-AND-GROUP_ADMINISTRATOR"),
    /**
     * Implicit group names for users.
     */
    USER_AND_GROUP_POWER_USER("USER-AND-GROUP_USER");

    private String name;

    ImplicitGroupNames(String name) {
        setName(name);
    }

    /**
     * Gets implicit name of group.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets implicit name of group.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
}
