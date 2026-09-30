package cz.cyberrange.platform.userandgroup.persistence.enums.dto;

/** Names of the groups the User-and-group microservice creates automatically. */
public enum ImplicitGroupNames {

  /** Name of the default group holding the default role of every microservice. */
  DEFAULT_GROUP("DEFAULT-GROUP"),
  /** Name of the group of administrators. */
  USER_AND_GROUP_ADMINISTRATOR("USER-AND-GROUP_ADMINISTRATOR"),
  /** Name of the group of power users. */
  USER_AND_GROUP_POWER_USER("USER-AND-GROUP_USER");

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
