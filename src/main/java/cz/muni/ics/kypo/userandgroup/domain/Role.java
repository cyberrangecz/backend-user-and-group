package cz.muni.ics.kypo.userandgroup.domain;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents the role of users. Each role gives different rights to users.
 */
@Entity
@Table(name = "role")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "Role.microservice",
                attributeNodes = {@NamedAttributeNode(value = "microservice")}
        )
})
@NamedQueries({
        @NamedQuery(
                name = "Role.findById",
                query = "SELECT r FROM Role r JOIN FETCH r.microservice WHERE r.id= :id"
        ),
        @NamedQuery(
                name = "Role.getAllRolesByMicroserviceName",
                query = "SELECT r FROM Role r JOIN FETCH r.microservice ms WHERE ms.name = :microserviceName"
        ),
        @NamedQuery(
                name = "Role.findDefaultRoleOfMicroservice",
                query = "SELECT r FROM Role r INNER JOIN r.microservice m WHERE m.name = :microserviceName AND r IN (SELECT r FROM IDMGroup g INNER JOIN g.roles r WHERE g.name = 'DEFAULT-GROUP')"
        )
})
public class Role extends AbstractEntity<Long> {

    @Column(name = "role_type", unique = true, nullable = false)
    private String roleType;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Microservice microservice;
    @Column(name = "description")
    private String description;

    /**
     * Gets the ID of the role.
     *
     * @return the ID of the role.
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets a new ID of the role.
     *
     * @param id the ID of the role.
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets role type of the role. Basically, it is the name of the role.
     *
     * @return the role type of the role.
     */
    public String getRoleType() {
        return roleType;
    }

    /**
     * Sets a new role type of the role.
     *
     * @param roleType the role type of type String
     */
    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    /**
     * Gets microservice of the role in which is role used.
     *
     * @return {@link Microservice} in which role is used.
     */
    public Microservice getMicroservice() {
        return microservice;
    }

    /**
     * Sets a new microservice of the role in which is role used.
     *
     * @param microservice {@link Microservice} in which role is used.
     */
    public void setMicroservice(Microservice microservice) {
        this.microservice = microservice;
    }

    /**
     * Gets a short description of the role. What rights the role gives to a user.
     *
     * @return the description of the role.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a new description of the role.
     *
     * @param description the description of the role.
     */
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
        return "Role{" +
                "id=" + super.getId() +
                ", roleType=" + roleType +
                '}';
    }
}
