package cz.muni.ics.kypo.userandgroup.model;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
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

    public User() {
    }

    public User(String login) {
        Assert.hasLength(login, "Login must not be empty");
        this.login = login;
        this.status = UserAndGroupStatus.VALID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public Set<IDMGroup> getGroups() {
        return Collections.unmodifiableSet(groups);
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

    public void setGroups(Set<IDMGroup> groups) {
        if (!this.groups.isEmpty()) {
            for (IDMGroup idmGroup : this.groups) {
                idmGroup.removeUser(this);
            }
        }
        this.groups = groups;
        for (IDMGroup idmGroup : groups) {
            idmGroup.addUser(this);
        }
    }

    public void addGroup(IDMGroup group) {
        groups.add(group);
        group.addUser(this);
    }

    public void removeGroup(IDMGroup group) {
        groups.remove(group);
        group.removeUser(this);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
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
