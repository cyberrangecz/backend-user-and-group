package cz.muni.ics.kypo.userandgroup.security.enums;

/**
 * @author Pavel Seda
 */
public enum ImplicitGroupNames {

    DEFAULT_GROUP("DEFAULT-GROUP"), USER_AND_GROUP_ADMINISTRATOR("USER-AND-GROUP_ADMINISTRATOR"), USER_AND_GROUP_USER("USER-AND-GROUP_USER");

    private String name;

    ImplicitGroupNames(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
