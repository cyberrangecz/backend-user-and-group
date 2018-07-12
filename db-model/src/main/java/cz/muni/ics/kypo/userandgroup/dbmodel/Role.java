/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Jan Duda 394179@mail.muni.cz
 *
 *  Date      : 29.5.2018
 *
 *  (c) Copyright 2016 MASARYK UNIVERSITY
 *  All rights reserved.
 *
 *  This software is freely available for non-commercial use under license
 *  specified in following license agreement in LICENSE file. Please review the terms
 *  of the license agreement before using this software. If you are interested in
 *  using this software commercially orin ways not allowed in aforementioned
 *  license, feel free to contact Technology transfer office of the Masaryk university
 *  in order to negotiate ad-hoc license agreement.
 */
package cz.muni.ics.kypo.userandgroup.dbmodel;

import org.springframework.util.Assert;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "ROLE")
public class Role {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "ROLE_TYPE", unique = true, nullable = false)
    private String roleType;

    @ManyToMany
    @JoinTable(name = "HIERARCHY_OF_ROLES", joinColumns = @JoinColumn(name = "PARENT_ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "CHILD_ROLE_ID"))
    private Set<Role> childrenRoles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String name) {
        this.roleType = name;
    }

    public Set<Role> getChildrenRoles() {
        return childrenRoles;
    }

    public void setChildrenRoles(Set<Role> childrenRoles) {
        this.childrenRoles = childrenRoles;
    }

    public void addChildRole(Role role) {
        this.childrenRoles.add(role);
    }

    public void removeChildRole(Role role) {
        this.childrenRoles.remove(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        return roleType.equals(role.roleType);
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
