package cz.muni.ics.kypo.userandgroup.util;

import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;

import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDataFactory {

    private List<Role> generatedRoles = generateRoleList(50);
    private List<Microservice> generatedMicroservices = generateMicroserviceList(50);
    private List<User> generatedUsers = generateUserList(50);
    private List<IDMGroup> generatedGroups = generateGroupList(50);

    public List<Role> getGeneratedRoles(){
        return Collections.unmodifiableList(generatedRoles);
    }

    public List<Microservice> getGeneratedMicroservices(){
        return Collections.unmodifiableList(generatedMicroservices);
    }

    public List<User> getGeneratedUsers() {
        return Collections.unmodifiableList(generatedUsers);
    }

    public List<IDMGroup> getGeneratedGroups() {
        return Collections.unmodifiableList(generatedGroups);
    }

    private List<Role> generateRoleList(int count){
        List<Role> generatedRoles = new ArrayList<>();
        Microservice microserviceForGeneratedRoles =
                new Microservice("microserviceForGeneratedRoles", "/microserviceForGeneratedRoles");
        for (int i = 0; i<count; i++){
            Role role = new Role();
            role.setMicroservice(microserviceForGeneratedRoles);
            role.setRoleType("Generated Role Type " + i);
            role.setDescription("Generated Role Description " + i);
            generatedRoles.add(role);
        }
        return generatedRoles;
    }

    private List<Microservice> generateMicroserviceList(int count){
        List<Microservice> generatedMicroservices = new ArrayList<>();
        for (int i = 0; i<count; i++){
            Microservice microservice = new Microservice();
            microservice.setName("Generated Microservice " + i);
            microservice.setEndpoint("/endpoint" + i);
            generatedMicroservices.add(microservice);
        }
        return generatedMicroservices;
    }

    private List<User> generateUserList(int count){
        List<User> generatedUsers = new ArrayList<>();
        for (int i = 0; i<count; i++){
            User user = new User();
            user.setLogin("Generated User Login " + i);
            user.setFullName("Generated Full Name " + i);
            user.setGivenName("Generated Given Name " + i);
            user.setFamilyName("Generated Family Name " + i);
            user.setStatus(UserAndGroupStatus.VALID);
            user.setIss("Generated Iss " + i);
            user.setPicture(BigInteger.valueOf(i).toByteArray());
            generatedUsers.add(user);
        }
        return generatedUsers;
    }

    private List<IDMGroup> generateGroupList(int count){
        List<IDMGroup> generatedGroups = new ArrayList<>();
        for (int i = 0; i<count; i++){
            IDMGroup group = new IDMGroup();
            group.setName("Generated Group Name " + i);
            group.setStatus(UserAndGroupStatus.VALID);
            group.setDescription("Generated Description " + i);
            group.setExpirationDate(LocalDateTime.now(Clock.systemUTC()).plusMinutes(i));
            generatedGroups.add(group);
        }
        return generatedGroups;
    }




}
