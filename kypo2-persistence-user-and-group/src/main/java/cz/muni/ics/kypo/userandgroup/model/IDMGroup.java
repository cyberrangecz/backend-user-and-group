package cz.muni.ics.kypo.userandgroup.model;

import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Represents a group of users who can have some roles.
 *
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

    /**
     * Instantiates a new IDMGroup.
     */
    public IDMGroup() {
    }

    /**
     * Instantiates a new IDMGroup with attributes bellow. Neither of them can be empty.
     *
     * @param name        new name of the group
     * @param description the description of the group
     */
    public IDMGroup(String name, String description) {
        Assert.hasLength(name, "Name of group must not be empty");
        Assert.hasLength(description, "Description of group must not be empty");
        this.name = name;
        this.status = UserAndGroupStatus.VALID;
        this.description = description;
    }

    /**
     * Gets the ID of the IDMGroup.
     *
     * @return the ID of the group.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of type long.
     *
     * @param id the ID of the group.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the new name of the group.
     *
     * @param name a string representing the new name of the group.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the IDMGroup.
     *
     * @return a string representing the group's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the status of the IDMGroup.
     *
     * @return the status {@link UserAndGroupStatus} of the IDMGroup.
     */
    public UserAndGroupStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the IDMGroup.
     *
     * @param status a new status of the IDMGroup.
     */
    public void setStatus(UserAndGroupStatus status) {
        this.status = status;
    }

    /**
     * Gets the external ID of the IDMGroup.
     *
     * @return the external ID of the group.
     */
    public Long getExternalId() {
        return externalId;
    }

    /**
     * Sets the external ID.
     *
     * @param externalId the external ID of the group.
     */
    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    /**
     * Gets users assigned to the group.
     *
     * @return set of {@link User}s.
     */
    public Set<User> getUsers() {
        return new HashSet<>(users);
    }

    /**
     * Sets set of users that should be assigned to the group.
     *
     * @param users set of {@link User}s
     */
    public void setUsers(Set<User> users) {
        this.users = users;
        for (User user : users) {
            user.addGroup(this);
        }
    }

    /**
     * Assignees one new user to the group.
     *
     * @param user the user who should be assigned to the group
     */
    public void addUser(User user) {
        users.add(user);
        user.addGroup(this);
    }

    /**
     * Remove the user from the IDMGroup.
     *
     * @param user the user who should be removed from the IDMGroup.
     */
    public void removeUser(User user) {
        users.remove(user);
        user.removeGroup(this);
    }

    /**
     * Gets a short description of the IDMGroup.
     *
     * @return the description of the group
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a new short description of the IDMGroup.
     *
     * @param description the description of the group.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets a set of roles assigned to the group. Every user in the group take on those roles.
     *
     * @return set of the {@link Role}s
     */
    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }

    /**
     * Sets a set of roles to the group.
     *
     * @param roles set of the {@link Role}s
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Add a new role to the group.
     *
     * @param role the {@link Role}.
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Remove the role from the group.
     *
     * @param role the role {@link Role}.
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Gets expiration date of the IDMGroup. After expiration date group is deleted from the system.
     *
     * @return the expiration date of type {@link LocalDateTime}
     */
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets expiration date.
     *
     * @param expirationDate the expiration date of type {@link LocalDateTime}
     */
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
