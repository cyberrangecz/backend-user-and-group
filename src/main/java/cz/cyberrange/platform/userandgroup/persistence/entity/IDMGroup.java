package cz.cyberrange.platform.userandgroup.persistence.entity;

import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import org.springframework.util.Assert;

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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a group of users who can have some roles.
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
                        @NamedSubgraph(name = "roles.microservice", attributeNodes = @NamedAttributeNode(value = "microservice"))
                }
        ),
        @NamedEntityGraph(
                name = "IDMGroup.users",
                attributeNodes = @NamedAttributeNode(value = "users")
        )
})
@NamedQueries({
        @NamedQuery(
                name = "IDMGroup.findByNameWithRoles",
                query = "SELECT g FROM IDMGroup g JOIN FETCH g.roles WHERE g.name = :name"
        ),
        @NamedQuery(
                name = "IDMGroup.findAllByRoleType",
                query = "SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = :roleType"
        ),
        @NamedQuery(
                name = "IDMGroup.findAdministratorGroup",
                query = "SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = 'ROLE_USER_AND_GROUP_ADMINISTRATOR'"
        ),
        @NamedQuery(
                name = "IDMGroup.getIDMGroupByNameWithUsers",
                query = "SELECT g FROM IDMGroup g LEFT JOIN FETCH g.users WHERE g.name = :name"
        ),
        @NamedQuery(
                name = "IDMGroup.deleteExpiredIDMGroups",
                query = "DELETE FROM IDMGroup g WHERE g.expirationDate <= CURRENT_TIMESTAMP"
        ),
        @NamedQuery(
                name = "IDMGroup.findUsersOfGivenGroups",
                query = "SELECT DISTINCT u FROM IDMGroup AS g INNER JOIN g.users AS u WHERE g.id IN :groupsIds"
        )
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
        return super.getId();
    }

    /**
     * Sets the ID of type long.
     *
     * @param id the ID of the group.
     */
    public void setId(Long id) {
        super.setId(id);
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
     * Sets the new name of the group.
     *
     * @param name a string representing the new name of the group.
     */
    public void setName(String name) {
        this.name = name;
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
                "id=" + super.getId() +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", externalId=" + externalId +
                ", description='" + description + '\'' +
                '}';
    }
}
