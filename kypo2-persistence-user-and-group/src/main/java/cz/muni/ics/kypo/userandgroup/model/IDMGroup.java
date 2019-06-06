package cz.muni.ics.kypo.userandgroup.model;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * @author Pavel Seda
 */
@Entity
@Table(name = "idm_group")
public class IDMGroup {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
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
    @JoinTable(name = "user_idm_group",
            joinColumns = {@JoinColumn(name = "idm_group_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> users = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "idm_group_role",
            joinColumns = @JoinColumn(name = "idm_group_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    public IDMGroup() {
    }

    public IDMGroup(String name, String description) {
        Assert.hasLength(name, "Name of group must not be empty");
        Assert.hasLength(description, "Description of group must not be empty");
        this.name = name;
        this.status = UserAndGroupStatus.VALID;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public Set<User> getUsers() {
        return new HashSet<>(users);
    }

    public void setUsers(Set<User> users) {
        this.users = users;
        for (User user : users) {
            user.addGroup(this);
        }
    }

    public void addUser(User user) {
        users.add(user);
        user.addGroup(this);
    }

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
        return "IDMGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", externalId=" + externalId +
                ", description='" + description + '\'' +
                '}';
    }
}
