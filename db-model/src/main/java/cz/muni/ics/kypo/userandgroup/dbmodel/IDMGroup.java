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
package cz.muni.ics.kypo.userandgroup.dbmodel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "IDM_GROUP")
public class IDMGroup {

    public static final String ADMINISTRATORS = "Administrators";
    public static final String SCENARISTS = "Scenarists";
    public static final String SCENARIOS = "Scenarios";
    public static final String KYPO_ORGANIZERS = "Scenarists"; //"%:Organizers"; //"KYPO-Organizers";
    public static final String ORGANIZERS = "Organizers"; // Prefix
    public static final String SUPERVISORS = "Supervisors"; // Prefix
    public static final String PARTICIPANTS = "Participants"; // Prefix

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "STATUS")
    private String status;

    @Column(name = "EXTERNAL_ID", unique = true)
    private Long externalId;

    @Column(name ="DESCRIPTION")
    private String description;

    @ManyToMany
    @JoinTable(name = "USER_IDM_GROUP", joinColumns = {@JoinColumn(name = "IDM_GROUP_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "USER_ID")})
    private List<User> users = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(name = "ORGANIZER", joinColumns = {@JoinColumn(name = "GROUP_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "SCENARIO_ID")})
    private List<Long> scenarios = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(name = "SUPERVISOR", joinColumns = {@JoinColumn(name = "GROUP_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "SCENARIO_INSTANCE_ID")})
    private List<Long> scenarioInstancesOfSupervisors = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(name = "PARTICIPANT", joinColumns = {@JoinColumn(name = "GROUP_ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "SCENARIO_INSTANCE_ID")})
    private List<Long> scenarioInstancesOfParticipants = new ArrayList<>();

    public IDMGroup() {
    }

    public IDMGroup(String name) {
        this.name = name;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public List<Long> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<Long> scenarios) {
        this.scenarios = scenarios;
    }
    
    public void addScenario(Long scenario){
        scenarios.add(scenario);
    }
    
    public void removeScenario(Long scenario){
        scenarios.remove(scenario);
    }

    public List<Long> getScenarioInstancesOfSupervisors() {
        return scenarioInstancesOfSupervisors;
    }

    public void setScenarioInstancesOfSupervisors(List<Long> scenarioInstancesOfSupervisors) {
        this.scenarioInstancesOfSupervisors = scenarioInstancesOfSupervisors;
    }
    
    public void addScenarioInstancesOfSupervisors(Long scenarioInstanceId) {
        scenarioInstancesOfSupervisors.add(scenarioInstanceId);
    }
    
    public void removeScenarioInstancesOfSupervisors(Long scenarioInstanceId) {
        scenarioInstancesOfSupervisors.remove(scenarioInstanceId);
    }
    
    public List<Long> getScenarioInstancesOfParticipants() {
        return scenarioInstancesOfParticipants;
    }

    public void setScenarioInstancesOfParticipants(List<Long> scenarioInstancesOfParticipants) {
        this.scenarioInstancesOfParticipants = scenarioInstancesOfParticipants;
    }
    
    public void addScenarioInstancesOfParticipants(Long scenarioInstanceId) {
        scenarioInstancesOfParticipants.add(scenarioInstanceId);
    }
    
    public void removeScenarioInstancesOfParticipants(Long scenarioInstanceId) {
        scenarioInstancesOfParticipants.remove(scenarioInstanceId);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "IDM Group [id=" + id + ",name=" + name + "]";
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
