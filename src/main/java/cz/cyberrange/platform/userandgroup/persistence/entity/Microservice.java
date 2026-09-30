package cz.cyberrange.platform.userandgroup.persistence.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A microservice registered with the system, identified by a unique name. Each {@link Role} that
 * belongs to it owns the reference to it.
 */
@Entity
@Table(name = "microservice")
public class Microservice extends AbstractEntity<Long> {

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "endpoint", nullable = false)
  private String endpoint;

  public Microservice() {}

  /**
   * Creates a microservice with the given name and endpoint.
   *
   * @param name name of the microservice
   * @param endpoint endpoint of the microservice
   * @throws IllegalArgumentException when name or endpoint is empty, or endpoint contains
   *     whitespace
   */
  public Microservice(String name, String endpoint) {
    Assert.hasLength(name, "Name of microservice must not be empty");
    Assert.hasLength(endpoint, "Endpoint of microservice must not be empty");
    Assert.isTrue(
        !StringUtils.containsWhitespace(endpoint),
        "Endpoint of microservice must not contain whitespace");
    this.name = name;
    this.endpoint = endpoint;
  }

  public Long getId() {
    return super.getId();
  }

  public void setId(Long id) {
    super.setId(id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Microservice)) {
      return false;
    }
    Microservice other = (Microservice) object;
    return Objects.equals(getName(), other.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return "Microservice{"
        + "id="
        + super.getId()
        + ", name='"
        + name
        + '\''
        + ", endpoint='"
        + endpoint
        + '\''
        + '}';
  }
}
