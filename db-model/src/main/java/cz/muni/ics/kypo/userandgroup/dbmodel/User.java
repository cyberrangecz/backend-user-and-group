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
package cz.muni.ics.kypo.userandgroup.dbmodel;

import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "SCREEN_NAME", unique = true, nullable = false)
    private String screenName;

    @Column(name = "DISPLAY_NAME")
    private String fullName;

    @Column(name = "EXTERNAL_ID", unique = true)
    private Long externalId;

    @Column(name = "MAIL")
    private String mail;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private UserAndGroupStatus status;

    @ManyToMany
    @JoinTable(name = "USER_IDM_GROUP", joinColumns = {@JoinColumn(name = "USER_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "IDM_GROUP_ID")})
    private List<IDMGroup> groups = new ArrayList<>();

    protected User(){
    }

    public User(String screenName) {
        Assert.hasLength(screenName, "Screen name must not be empty");
        this.screenName = screenName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
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

    public List<IDMGroup> getGroups() {
        return groups;
    }

    public void addGroup(IDMGroup group) {
        groups.add(group);
    }

    public void removeGroup(IDMGroup group) {
        groups.remove(group);
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", screenName=" + screenName + "]";
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
        if (!Objects.equals(this.screenName, other.screenName)) {
            return false;
        }
        return true;
    }
}
