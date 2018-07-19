/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz, Jan Duda 394179@mail.muni.cz
 *
 *  Date      : 31.5.2016
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

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "IDM_GROUP")
@NamedEntityGraph(name = "IDMGroup.users",
        attributeNodes = @NamedAttributeNode("users"))
public class IDMGroup {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAndGroupStatus status;

    @Column(name = "EXTERNAL_ID", unique = true)
    private Long externalId;

    @Column(name ="DESCRIPTION", nullable = false)
    private String description;

    @ManyToMany
    @JoinTable(name = "USER_IDM_GROUP", joinColumns = {@JoinColumn(name = "IDM_GROUP_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "USER_ID")})
    private List<User> users = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "IDM_GROUP_ROLE", joinColumns = @JoinColumn(name = "IDM_GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private Set<Role> roles = new HashSet<>();

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

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Role> getRoles() {
        return roles;
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

    @Override
    public String toString() {
        return "IDM Group [id=" + id + ",name=" + name + ",descritpion=" + description + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IDMGroup)) {
            return false;
        }
        IDMGroup other = (IDMGroup) object;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
