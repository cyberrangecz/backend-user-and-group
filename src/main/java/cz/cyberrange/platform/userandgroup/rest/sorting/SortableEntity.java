package cz.cyberrange.platform.userandgroup.rest.sorting;

import java.util.List;

/** The entity kinds a paged endpoint returns, each with the properties a client may sort it by. */
public enum SortableEntity {
  USER("id", "sub", "iss", "fullName", "givenName", "familyName", "mail", "status", "externalId"),
  GROUP("id", "name", "description", "status", "externalId", "expirationDate"),
  ROLE("id", "roleType", "description"),
  MICROSERVICE("id", "name", "endpoint");

  private final List<String> properties;

  SortableEntity(String... properties) {
    this.properties = List.of(properties);
  }

  /**
   * Returns the sortable property names, each as written in the {@code sort} request parameter.
   *
   * @return the allowed property names
   */
  public List<String> properties() {
    return properties;
  }
}
