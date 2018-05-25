/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz
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
package cz.muni.ics.kypo.dbmodel.userandgroup;

import javax.persistence.*;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "LIFERAY_SN", unique = true, nullable = false)
    private String liferayScreenName;

    @Column(name = "DISPLAY_NAME") //, nullable = false)
    private String displayName;

    @Column(name = "EXTERNAL_ID", unique = true)
    private Long externalId;

    @Column(name = "MAIL")
    private String mail;

    @Column(name = "STATUS")
    private String status;

    @ManyToMany
    @JoinTable(name = "USER_IDM_GROUP", joinColumns = {@JoinColumn(name = "USER_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "IDM_GROUP_ID")})
    private List<IDMGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Long> participants = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private Set<Role> roles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLiferayScreenName() {
        return liferayScreenName;
    }

    public void setLiferayScreenName(String liferayScreenName) {
        this.liferayScreenName = liferayScreenName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<IDMGroup> getGroups() {
        return groups;
    }

    public void addGroup(IDMGroup group) {
        groups.add(group);
    }

    public void removeGroup(IDMGroup group) {
        groups.remove(group);
    }

    public List<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }

    public void addParticipant(Long participant) {
        this.participants.add(participant);
    }

    public void removeParicipants(Long participant) {
        this.participants.remove(participant);
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
        return "User [id=" + id + ", liferayScreenName=" + liferayScreenName + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
