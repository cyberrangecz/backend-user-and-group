package cz.muni.ics.kypo.userandgroup.domain;


import cz.muni.ics.kypo.userandgroup.enums.UserAndGroupStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user in the system.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"sub", "iss"}))
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "User.groupsRolesMicroservice",
                attributeNodes = @NamedAttributeNode(value = "groups", subgraph = "groups.roles"),
                subgraphs = {
                        @NamedSubgraph(name = "groups.roles", attributeNodes = @NamedAttributeNode(value = "roles", subgraph = "roles.microservice")),
                        @NamedSubgraph(name = "roles.microservice", attributeNodes = @NamedAttributeNode(value = "microservice"))
                }
        ),
        @NamedEntityGraph(
                name = "User.groups",
                attributeNodes = @NamedAttributeNode(value = "groups")
        )
})
@NamedQueries({
        @NamedQuery(
                name = "User.getSub",
                query = "SELECT u.sub FROM User u WHERE u.id = :userId"
        ),
        @NamedQuery(
                name = "User.getRolesOfUser",
                query = "SELECT r FROM User u INNER JOIN u.groups g INNER JOIN g.roles r JOIN FETCH r.microservice WHERE u.id = :userId"
        ),
        @NamedQuery(
                name = "User.getUserBySubWithGroups",
                query = "SELECT u FROM User u JOIN FETCH u.groups WHERE u.sub = :sub AND u.iss = :iss"
        ),
        @NamedQuery(
                name = "User.getUserByIdWithGroups",
                query = "SELECT u FROM User u JOIN FETCH u.groups WHERE u.id = :userId"
        ),
        @NamedQuery(
                name = "User.findAllWithGivenIds",
                query = "SELECT u FROM User u WHERE u.id IN :ids"
        )
})
public class User extends AbstractEntity<Long> {

    @ManyToMany(mappedBy = "users")
    private final Set<IDMGroup> groups = new HashSet<>();
    @Column(name = "sub", nullable = false)
    private String sub;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "given_name")
    private String givenName;
    @Column(name = "family_name")
    private String familyName;
    @Column(name = "external_id", unique = true)
    private Long externalId;
    @Column(name = "mail")
    private String mail;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserAndGroupStatus status;
    @Column(name = "iss", nullable = false)
    private String iss;
    @Lob
    @Column(name = "picture")
    private byte[] picture;

    /**
     * Instantiates a new User.
     */
    public User() {
        this.status = UserAndGroupStatus.VALID;
    }

    /**
     * Instantiates a new User with sub and his oidc provider. Sub should not be empty.
     *
     * @param sub the sub of type String. Sub should be of type 13***5@muni.cz
     * @param iss URI of provider which will be used to authenticate this user.
     */
    public User(String sub, String iss) {
        this.sub = sub;
        this.status = UserAndGroupStatus.VALID;
        this.iss = iss;
    }

    /**
     * Gets the ID of the user.
     *
     * @return the ID of type long.
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets the new ID of the user.
     *
     * @param id the ID of the user.
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets the sub of the user.
     *
     * @return the sub of the user.
     */
    public String getSub() {
        return sub;
    }

    /**
     * Sets a new sub of the user.
     *
     * @param sub the sub of the user.
     */
    public void setSub(String sub) {
        this.sub = sub;
    }

    /**
     * Gets the full name of the user. Full name is composed of title before a name, given name and family name.
     *
     * @return the full name of of the user.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the new full name of the user.
     *
     * @param fullName the full name of the user.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the external ID of the user. This ID is used when the user is imported from an external source.
     *
     * @return the external ID of the user.
     */
    public Long getExternalId() {
        return externalId;
    }

    /**
     * Sets the new external ID of the user.
     *
     * @param externalId the external ID of the user.
     */
    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    /**
     * Gets the mail of the user.
     *
     * @return the mail of the user.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets a new mail of the user.
     *
     * @param mail the mail of the user
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Gets the status of the user.
     *
     * @return the status {@link UserAndGroupStatus} of the user.
     */
    public UserAndGroupStatus getStatus() {
        return status;
    }

    /**
     * Sets a new status of the user.
     *
     * @param status the status {@link UserAndGroupStatus} of the user.
     */
    public void setStatus(UserAndGroupStatus status) {
        this.status = status;
    }

    /**
     * Gets groups in which user participates.
     *
     * @return the set of {@link IDMGroup}s.
     */
    public Set<IDMGroup> getGroups() {
        return new HashSet<>(groups);
    }

    /**
     * Sets a new set of groups of the user.
     *
     * @param groups the {@link IDMGroup}s in which the user participates.
     */
    public void setGroups(Set<IDMGroup> groups) {
        for (IDMGroup group : groups) {
            group.addUser(this);
        }
    }

    /**
     * Gets the given name of the user.
     *
     * @return the given name of the user.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets a new given name of the user.
     *
     * @param givenName the given name of the user.
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Gets the family name of the user.
     *
     * @return the new family name of the user.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Sets the new family name of the user.
     *
     * @param familyName the family name of the user.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     * Add the user to the group.
     *
     * @param group the {@link IDMGroup} to which the user is added.
     */
    public void addGroup(IDMGroup group) {
        groups.add(group);
    }

    /**
     * Remove group.
     *
     * @param group the group
     */
    public void removeGroup(IDMGroup group) {
        groups.remove(group);
    }

    /**
     * Gets the URI of provider which will be used to authenticate this user.
     *
     * @return URI of the oidc provider.
     */
    public String getIss() {
        return iss;
    }

    /**
     * Sets the URI of provider which will be used to authenticate this user.
     *
     * @param iss the URI of the oidc provider.
     */
    public void setIss(String iss) {
        this.iss = iss;
    }

    /**
     * Gets the identicon of the user encoded in base64.
     *
     * @return identicon of the user.
     */
    public byte[] getPicture() {
        return picture;
    }

    /**
     * Sets the identicon of the user encoded in base64.
     *
     * @param picture encoded identicon of the user.
     */
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
        return Objects.equals(getSub(), user.getSub()) &&
                Objects.equals(getIss(), user.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSub(), getIss());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + super.getId() +
                ", sub='" + sub + '\'' +
                ", fullName='" + fullName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", externalId=" + externalId +
                ", mail='" + mail + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}
