package cz.muni.ics.kypo.userandgroup.model;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Represents a user in the system.
 *
 * @author Pavel Seda
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(name = "login", unique = true, nullable = false)
    private String login;
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
    @ManyToMany(mappedBy = "users")
    private Set<IDMGroup> groups = new HashSet<>();

    /**
     * Instantiates a new User.
     */
    public User() {
    }

    /**
     * Instantiates a new User with login. Login should not be empty.
     *
     * @param login the login of type String. Login should be of type 13***5@muni.cz
     */
    public User(String login) {
        Assert.hasLength(login, "Login must not be empty");
        this.login = login;
        this.status = UserAndGroupStatus.VALID;
    }

    /**
     * Gets the ID of the user.
     *
     * @return the ID of type long.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the new ID of the user.
     *
     * @param id the ID of the user.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the login of the user.
     *
     * @return the login of the user.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets a new login of the user.
     *
     * @param login the login of the user.
     */
    public void setLogin(String login) {
        this.login = login;
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
     * @return the status {@link cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus} of the user.
     */
    public UserAndGroupStatus getStatus() {
        return status;
    }

    /**
     * Sets a new status of the user.
     *
     * @param status the status {@link cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus} of the user.
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

    @Override
    public int hashCode() {
        return Objects.hashCode(getLogin());
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        return Objects.equals(this.login, other.getLogin());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", fullName='" + fullName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", externalId=" + externalId +
                ", mail='" + mail + '\'' +
                '}';
    }
}
