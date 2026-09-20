package cz.cyberrange.platform.userandgroup.persistence.entity;

import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import org.springframework.util.Assert;

/**
 * A group of users who share a set of roles. Group names are unique, and this side owns the
 * many-to-many relationships to both {@link User} and {@link Role}.
 */
@Entity
@Table(name = "idm_group")
@NamedEntityGraphs({
  @NamedEntityGraph(
      name = "IDMGroup.usersRolesMicroservice",
      attributeNodes = {
        @NamedAttributeNode(value = "users"),
        @NamedAttributeNode(value = "roles", subgraph = "roles.microservice")
      },
      subgraphs = {
        @NamedSubgraph(
            name = "roles.microservice",
            attributeNodes = @NamedAttributeNode(value = "microservice"))
      }),
  @NamedEntityGraph(name = "IDMGroup.users", attributeNodes = @NamedAttributeNode(value = "users"))
})
@NamedQueries({
  @NamedQuery(
      name = "IDMGroup.findByNameWithRoles",
      query = "SELECT g FROM IDMGroup g JOIN FETCH g.roles WHERE g.name = :name"),
  @NamedQuery(
      name = "IDMGroup.findAllByRoleType",
      query = "SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = :roleType"),
  @NamedQuery(
      name = "IDMGroup.findAdministratorGroup",
      query =
          "SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = 'ROLE_USER_AND_GROUP_ADMINISTRATOR'"),
  @NamedQuery(
      name = "IDMGroup.getIDMGroupByNameWithUsers",
      query = "SELECT g FROM IDMGroup g LEFT JOIN FETCH g.users WHERE g.name = :name"),
  @NamedQuery(
      name = "IDMGroup.deleteExpiredIDMGroups",
      query = "DELETE FROM IDMGroup g WHERE g.expirationDate <= CURRENT_TIMESTAMP"),
  @NamedQuery(
      name = "IDMGroup.findUsersOfGivenGroups",
      query =
          "SELECT DISTINCT u FROM IDMGroup AS g INNER JOIN g.users AS u WHERE g.id IN :groupsIds")
})
public class IDMGroup extends AbstractEntity<Long> {

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserAndGroupStatus status;

  @Column(name = "external_id", unique = true)
  private Long externalId;

  @Column(name = "description", nullable = false)
  private String description;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_idm_group",
      joinColumns = {@JoinColumn(name = "idm_group_id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id")})
  private Set<User> users = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "idm_group_role",
      joinColumns = @JoinColumn(name = "idm_group_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  // Groups at or past this date are removed by the periodic cleanup; unset groups never expire.
  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  public IDMGroup() {}

  /**
   * Creates a group with the given name and description, marked valid.
   *
   * @param name name of the group
   * @param description description of the group
   * @throws IllegalArgumentException when name or description is empty
   */
  public IDMGroup(String name, String description) {
    Assert.hasLength(name, "Name of group must not be empty");
    Assert.hasLength(description, "Description of group must not be empty");
    this.name = name;
    this.status = UserAndGroupStatus.VALID;
    this.description = description;
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

  public UserAndGroupStatus getStatus() {
    return status;
  }

  public void setStatus(UserAndGroupStatus status) {
    this.status = status;
  }

  public Long getExternalId() {
    return externalId;
  }

  public void setExternalId(Long externalId) {
    this.externalId = externalId;
  }

  /**
   * Returns the users assigned to the group. The set is a copy; changes to it do not affect the
   * group.
   *
   * @return the group's users
   */
  public Set<User> getUsers() {
    return new HashSet<>(users);
  }

  /**
   * Replaces the group's users. Each given user also has this group added to its own set of groups.
   *
   * @param users users to assign to the group
   */
  public void setUsers(Set<User> users) {
    this.users = users;
    for (User user : users) {
      user.addGroup(this);
    }
  }

  /**
   * Assigns one user to the group. The user also has this group added to its own set of groups.
   *
   * @param user user to assign to the group
   */
  public void addUser(User user) {
    users.add(user);
    user.addGroup(this);
  }

  /**
   * Removes one user from the group. The user also has this group removed from its own set of
   * groups.
   *
   * @param user user to remove from the group
   */
  public void removeUser(User user) {
    users.remove(user);
    user.removeGroup(this);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the roles assigned to the group. The set is a copy; changes to it do not affect the
   * group.
   *
   * @return the group's roles
   */
  public Set<Role> getRoles() {
    return new HashSet<>(roles);
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public void addRole(Role role) {
    this.roles.add(role);
  }

  public void removeRole(Role role) {
    this.roles.remove(role);
  }

  public LocalDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof IDMGroup)) {
      return false;
    }
    IDMGroup other = (IDMGroup) object;
    return Objects.equals(getName(), other.getName());
  }

  @Override
  public String toString() {
    return "IDMGroup{"
        + "id="
        + super.getId()
        + ", name='"
        + name
        + '\''
        + ", status="
        + status
        + ", externalId="
        + externalId
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
