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
package cz.muni.ics.kypo.userandgroup.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Represents the role of users. Each role gives different rights to users.
 *
 * @author Pavel Seda
 */
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
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
        return id;
    }

    /**
     * Sets a new ID of the role.
     *
     * @param id the ID of the role.
     */
    public void setId(Long id) {
        this.id = id;
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
        if(!(object instanceof Role)) {
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
                "id=" + id +
                ", roleType=" + roleType +
                '}';
    }
}
