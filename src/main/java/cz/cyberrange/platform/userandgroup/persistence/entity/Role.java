package cz.cyberrange.platform.userandgroup.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * A role that grants rights to the users of the group it is assigned to. Its role type is unique,
 * and it owns the required reference to the {@link Microservice} it belongs to.
 */
@Entity
@Table(name = "role")
@NamedEntityGraphs({
  @NamedEntityGraph(
      name = "Role.microservice",
      attributeNodes = {@NamedAttributeNode(value = "microservice")})
})
@NamedQueries({
  @NamedQuery(
      name = "Role.findById",
      query = "SELECT r FROM Role r JOIN FETCH r.microservice WHERE r.id= :id"),
  @NamedQuery(
      name = "Role.getAllRolesByMicroserviceName",
      query =
          "SELECT r FROM Role r JOIN FETCH r.microservice ms WHERE ms.name = :microserviceName"),
  @NamedQuery(
      name = "Role.findDefaultRoleOfMicroservice",
      query =
          "SELECT r FROM Role r INNER JOIN r.microservice m WHERE m.name = :microserviceName AND r IN (SELECT r FROM IDMGroup g INNER JOIN g.roles r WHERE g.name = 'DEFAULT-GROUP')")
})
public class Role extends AbstractEntity<Long> {

  // Also serves as the role's unique display name.
  @Column(name = "role_type", unique = true, nullable = false)
  private String roleType;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Microservice microservice;

  // States the rights the role grants to a user.
  @Column(name = "description")
  private String description;

  public Long getId() {
    return super.getId();
  }

  public void setId(Long id) {
    super.setId(id);
  }

  public String getRoleType() {
    return roleType;
  }

  public void setRoleType(String roleType) {
    this.roleType = roleType;
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public void setMicroservice(Microservice microservice) {
    this.microservice = microservice;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Role)) {
      return false;
    }
    Role other = (Role) object;
    return Objects.equals(getRoleType(), other.getRoleType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleType);
  }

  @Override
  public String toString() {
    return "Role{" + "id=" + super.getId() + ", roleType=" + roleType + '}';
  }
}
