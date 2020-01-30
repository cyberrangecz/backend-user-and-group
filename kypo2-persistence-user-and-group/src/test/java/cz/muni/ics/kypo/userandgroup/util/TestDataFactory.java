package cz.muni.ics.kypo.userandgroup.util;

import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
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

    private Microservice kypoTrainingMicroservice
            = new Microservice("training", "http://kypo2-training/api/v1");
    private Microservice kypoUaGMicroservice
            = new Microservice("userAndGroup", "http://kypo2-user-and-group/api/v1");

    private Role trainingTraineeRole
            = generateRole("ROLE_TRAINING_TRAINEE", kypoTrainingMicroservice, "Trainee description");
    private Role trainingOrganizerRole
            = generateRole("ROLE_TRAINING_ORGANIZER", kypoTrainingMicroservice, "Organizer description");
    private Role trainingDesignerRole
            = generateRole("ROLE_TRAINING_DESIGNER", kypoTrainingMicroservice, "Designer description");
    private Role trainingAdminRole
            = generateRole("ROLE_TRAINING_ADMIN", kypoTrainingMicroservice, "Training admin description");
    private Role uAGUserRole
            = generateRole(RoleType.ROLE_USER_AND_GROUP_USER.name(), kypoUaGMicroservice, "User description");
    private Role uAGGuestRole
            = generateRole(RoleType.ROLE_USER_AND_GROUP_GUEST.name(), kypoUaGMicroservice, "Guest description");
    private Role uAGAdminRole
            = generateRole(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(), kypoUaGMicroservice, "UaG admin description");

    private User user1
            = generateUser("852374@muni.cz", "Garfield", "Pokorny", "852374@mail.muni.cz", "https://oidc.muni.cz/oidc/", "pic".getBytes());
    private User user2
            = generateUser("632145@muni.cz", "Marcel", "Watchman", "632145@mail.muni.cz", "https://oidc.muni.cz/oidc/", "icon".getBytes());
    private User user3
            = generateUser("77863@muni.cz", "Drew", "Coyer", "77863@mail.muni.cz", "https://oidc.provider.cz/oidc/", "default".getBytes());
    private User user4
            = generateUser("794254@muni.cz", "Garret", "Cull", "794254@mail.muni.cz", "https://oidc.provider.cz/oidc/", "profile".getBytes())

    public Microservice getKypoTrainingMicroservice() {
        return kypoTrainingMicroservice;
    }

    public Microservice getKypoUaGMicroservice() {
        return kypoUaGMicroservice;
    }

    public Role getTrainingTraineeRole() {
        return trainingTraineeRole;
    }

    public Role getTrainingOrganizerRole() {
        return trainingOrganizerRole;
    }

    public Role getTrainingDesignerRole() {
        return trainingDesignerRole;
    }

    public Role getTrainingAdminRole() {
        return trainingAdminRole;
    }

    public Role getUAGUserRole() {
        return uAGUserRole;
    }

    public Role getUAGGuestRole() {
        return uAGGuestRole;
    }

    public Role getUAGAdminRole() {
        return uAGAdminRole;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public User getUser3() {
        return user3;
    }

    public User getUser4() {
        return user4;
    }

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


    private Role generateRole(String roleType, Microservice microservice, String description){
        Role role = new Role();
        role.setRoleType(roleType);
        role.setMicroservice(microservice);
        role.setDescription(description);
        return role;
    }

    private User generateUser(String login, String givenName, String familyName, String mail, String iss, byte[] picture){
        User user = new User();
        user.setLogin(login);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setStatus(UserAndGroupStatus.VALID);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
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
