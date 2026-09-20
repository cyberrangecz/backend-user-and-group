package cz.cyberrange.platform.userandgroup.persistence.entity;

import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Represents an authenticated user of the system. The combination of sub and iss is unique across
 * users, and the {@link IDMGroup} side owns the many-to-many relationship to the groups a user
 * belongs to.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"sub", "iss"}))
@NamedEntityGraphs({
  @NamedEntityGraph(
      name = "User.groupsRolesMicroservice",
      attributeNodes = @NamedAttributeNode(value = "groups", subgraph = "groups.roles"),
      subgraphs = {
        @NamedSubgraph(
            name = "groups.roles",
            attributeNodes = @NamedAttributeNode(value = "roles", subgraph = "roles.microservice")),
        @NamedSubgraph(
            name = "roles.microservice",
            attributeNodes = @NamedAttributeNode(value = "microservice"))
      }),
  @NamedEntityGraph(name = "User.groups", attributeNodes = @NamedAttributeNode(value = "groups"))
})
@NamedQueries({
  @NamedQuery(name = "User.getSub", query = "SELECT u.sub FROM User u WHERE u.id = :userId"),
  @NamedQuery(
      name = "User.getRolesOfUser",
      query =
          "SELECT r FROM User u INNER JOIN u.groups g INNER JOIN g.roles r JOIN FETCH r.microservice WHERE u.id = :userId"),
  @NamedQuery(
      name = "User.getUserBySubWithGroups",
      query = "SELECT u FROM User u JOIN FETCH u.groups WHERE u.sub = :sub AND u.iss = :iss"),
  @NamedQuery(
      name = "User.getUserByIdWithGroups",
      query = "SELECT u FROM User u JOIN FETCH u.groups WHERE u.id = :userId"),
  @NamedQuery(name = "User.findAllWithGivenIds", query = "SELECT u FROM User u WHERE u.id IN :ids")
})
public class User extends AbstractEntity<Long> {

  @ManyToMany(mappedBy = "users")
  private final Set<IDMGroup> groups = new HashSet<>();

  // Uniquely identifies the user within its issuing OIDC provider.
  @Column(name = "sub", nullable = false)
  private String sub;

  // Composed of a title before the name, the given name and the family name.
  @Column(name = "full_name")
  private String fullName;

  @Column(name = "given_name")
  private String givenName;

  @Column(name = "family_name")
  private String familyName;

  // Identifies the user when imported from an external source.
  @Column(name = "external_id", unique = true)
  private Long externalId;

  @Column(name = "mail")
  private String mail;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private UserAndGroupStatus status;

  // Identifies the OIDC provider that authenticated the user.
  @Column(name = "iss", nullable = false)
  private String iss;

  // Holds the user's generated identicon image.
  @Lob
  @Column(name = "picture")
  private byte[] picture;

  /** Creates a user with valid status. */
  public User() {
    this.status = UserAndGroupStatus.VALID;
  }

  /**
   * Creates a user identified by the given subject and issuer, with valid status.
   *
   * @param sub subject identifier from the OIDC provider
   * @param iss issuer of the OIDC provider used to authenticate the user
   */
  public User(String sub, String iss) {
    this.sub = sub;
    this.status = UserAndGroupStatus.VALID;
    this.iss = iss;
  }

  public Long getId() {
    return super.getId();
  }

  public void setId(Long id) {
    super.setId(id);
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Long getExternalId() {
    return externalId;
  }

  public void setExternalId(Long externalId) {
    this.externalId = externalId;
  }

  public String getMail() {
    return mail;
  }

  public void setMail(String mail) {
    this.mail = mail;
  }

  public UserAndGroupStatus getStatus() {
    return status;
  }

  public void setStatus(UserAndGroupStatus status) {
    this.status = status;
  }

  /**
   * Returns the groups the user belongs to. The set is a copy; changes to it do not affect the
   * user.
   *
   * @return the user's groups
   */
  public Set<IDMGroup> getGroups() {
    return new HashSet<>(groups);
  }

  /**
   * Adds this user to each of the given groups; group membership not included in the given set is
   * left unchanged. Each given group also has this user added to its own set of users.
   *
   * @param groups groups to add this user to
   */
  public void setGroups(Set<IDMGroup> groups) {
    for (IDMGroup group : groups) {
      group.addUser(this);
    }
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  /**
   * Adds one group to this user's set of groups. Does not add this user to the group's own set of
   * users.
   *
   * @param group group to add
   */
  public void addGroup(IDMGroup group) {
    groups.add(group);
  }

  /**
   * Removes one group from this user's set of groups. Does not remove this user from the group's
   * own set of users.
   *
   * @param group group to remove
   */
  public void removeGroup(IDMGroup group) {
    groups.remove(group);
  }

  public String getIss() {
    return iss;
  }

  public void setIss(String iss) {
    this.iss = iss;
  }

  public byte[] getPicture() {
    return picture;
  }

  public void setPicture(byte[] picture) {
    this.picture = picture;
  }

  @PreRemove
  private void removeUserFromGroups() {
    for (IDMGroup group : this.getGroups()) {
      group.removeUser(this);
    }
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof User)) return false;
    User user = (User) object;
    return Objects.equals(getSub(), user.getSub()) && Objects.equals(getIss(), user.getIss());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSub(), getIss());
  }

  @Override
  public String toString() {
    return "User{"
        + "id="
        + super.getId()
        + ", sub='"
        + sub
        + '\''
        + ", fullName='"
        + fullName
        + '\''
        + ", givenName='"
        + givenName
        + '\''
        + ", familyName='"
        + familyName
        + '\''
        + ", externalId="
        + externalId
        + ", mail='"
        + mail
        + '\''
        + ", iss='"
        + iss
        + '\''
        + '}';
  }
}
