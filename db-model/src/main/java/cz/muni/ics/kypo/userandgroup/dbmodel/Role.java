package cz.muni.ics.kypo.userandgroup.dbmodel;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "ROLE")
public class Role {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "ROLE_TYPE", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType name) {
        this.roleType = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        return roleType == role.roleType;
    }

    @Override
    public int hashCode() {
        return roleType != null ? roleType.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleType=" + roleType +
                '}';
    }
}
