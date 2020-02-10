package cz.muni.ics.kypo.userandgroup.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.RoleTypeDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class TestDataFactory {

    private ObjectMapper mapper = new ObjectMapper().registerModule( new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private List<Role> generatedRoles = generateRoleList(50);
    private List<Microservice> generatedMicroservices = generateMicroserviceList(50);
    private List<User> generatedUsers = generateUserList(50);
    private List<IDMGroup> generatedGroups = generateGroupList(50);

    private Microservice kypoTrainingMicroservice
            = new Microservice("training", "http://kypo2-training/api/v1");
    private Microservice kypoUaGMicroservice
            = new Microservice("userAndGroup", "http://kypo2-user-and-group/api/v1");

    private NewMicroserviceDTO newMicroserviceDTO
            = generateNewMicroserviceDTO("training", "http://kypo2-training/api/v1");

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

    private RoleDTO uAGUserRoleDTO
            = generateRoleDTO(RoleType.ROLE_USER_AND_GROUP_USER.name(), "User description");
    private RoleDTO uAGGuestRoleDTO
            = generateRoleDTO(RoleType.ROLE_USER_AND_GROUP_GUEST.name(), "Guest description");
    private RoleDTO uAGAdminRoleDTO
            = generateRoleDTO(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(), "UaG admin description");

    private RoleForNewMicroserviceDTO trainingAdminRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_ADMIN", "Training admin description");
    private RoleForNewMicroserviceDTO trainingOrganizerRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_ORGANIZER", "Training organizer description");
    private RoleForNewMicroserviceDTO trainingDesignerRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_DESIGNER", "Training designer description");
    private RoleForNewMicroserviceDTO trainingTraineeRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_TRAINEE", "Training trainee description");

    private User user1
            = generateUser("852374@muni.cz", "Garfield", "Pokorny", "852374@mail.muni.cz", "https://oidc.muni.cz/oidc/", "pic".getBytes());
    private User user2
            = generateUser("632145@muni.cz", "Marcel", "Watchman", "632145@mail.muni.cz", "https://oidc.muni.cz/oidc/", "icon".getBytes());
    private User user3
            = generateUser("77863@muni.cz", "Drew", "Coyer", "77863@mail.muni.cz", "https://oidc.provider.cz/oidc/", "default".getBytes());
    private User user4
            = generateUser("794254@muni.cz", "Garret", "Cull", "794254@mail.muni.cz", "https://oidc.provider.cz/oidc/", "profile".getBytes());

    private UserDTO user1DTO
            = generateUserDTO("852374@muni.cz", "Garfield", "Pokorny", "852374@mail.muni.cz", "https://oidc.muni.cz/oidc/", "pic".getBytes());
    private UserDTO user2DTO
            = generateUserDTO("632145@muni.cz", "Marcel", "Watchman", "632145@mail.muni.cz", "https://oidc.muni.cz/oidc/", "icon".getBytes());
    private UserForGroupsDTO userForGroupsDTO1
            = generateUserForGroupsDTO("852374@muni.cz", "Garfield", "Pokorny", "852374@mail.muni.cz", "https://oidc.muni.cz/oidc/", "pic".getBytes());
    private UserForGroupsDTO userForGroupsDTO2
            = generateUserForGroupsDTO("77863@muni.cz", "Drew", "Coyer", "77863@mail.muni.cz", "https://oidc.muni.cz/oidc/", "pic".getBytes());
    private UserForGroupsDTO userForGroupsDTO3
            = generateUserForGroupsDTO("794254@muni.cz", "Garret", "Cull", "794254@mail.muni.cz", "https://oidc.muni.cz/oidc/", "pic".getBytes());

    private IDMGroup trainingTraineeGroup = generateGroup("trainingTraineeGroup", new HashSet<Role>(List.of(trainingTraineeRole)), 9);
    private IDMGroup trainingOrganizerGroup = generateGroup("trainingOrganizerGroup", new HashSet<Role>(List.of(trainingOrganizerRole)), 23);
    private IDMGroup trainingDesignerGroup = generateGroup("trainingDesignerGroup", new HashSet<Role>(List.of(trainingDesignerRole)), 5);
    private IDMGroup trainingAdminGroup = generateGroup("trainingAdminGroup", new HashSet<Role>(List.of(trainingAdminRole)), 6);
    private IDMGroup uAGUserGroup = generateGroup(ImplicitGroupNames.USER_AND_GROUP_USER.getName(), new HashSet<Role>(List.of(uAGUserRole)), 1);
    private IDMGroup uAGDefaultGroup = generateGroup(ImplicitGroupNames.DEFAULT_GROUP.getName(), new HashSet<Role>(List.of(uAGGuestRole)), 7);
    private IDMGroup uAGAdminGroup = generateGroup(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), new HashSet<Role>(List.of(uAGAdminRole)), 29);

    private GroupDTO uAGUserGroupDTO = generateGroupDTO(ImplicitGroupNames.USER_AND_GROUP_USER.getName(), Source.INTERNAL);
    private GroupDTO uAGUAdminGroupDTO = generateGroupDTO(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), Source.INTERNAL);
    private NewGroupDTO newGroupDTO = generateNewGroupDTO("New Group");
    private UpdateGroupDTO updateGroupDTO = generateUpdateGroupDTO("Update Group");

    public Microservice getKypoTrainingMicroservice() {
        return clone(kypoTrainingMicroservice, Microservice.class);
    }

    public Microservice getKypoUaGMicroservice() {
        return clone(kypoUaGMicroservice, Microservice.class);
    }

    public NewMicroserviceDTO getNewMicroserviceDTO(){
        return clone(newMicroserviceDTO, NewMicroserviceDTO.class);
    }

    public Role getTrainingTraineeRole() {
        return clone(trainingTraineeRole, Role.class);
    }

    public Role getTrainingOrganizerRole() {
        return clone(trainingOrganizerRole, Role.class);
    }

    public Role getTrainingDesignerRole() {
        return clone(trainingDesignerRole, Role.class);
    }

    public Role getTrainingAdminRole() {
        return clone(trainingAdminRole, Role.class);
    }

    public Role getUAGUserRole() {
        return clone(uAGUserRole, Role.class);
    }

    public Role getUAGGuestRole() {
        return clone(uAGGuestRole, Role.class);
    }

    public Role getUAGAdminRole(){
        return clone(uAGAdminRole, Role.class);
    }

    public RoleDTO getUAGUserRoleDTO() {
        return clone(uAGUserRoleDTO, RoleDTO.class);
    }

    public RoleDTO getuAGGuestRoleDTO() {
        return clone(uAGGuestRoleDTO, RoleDTO.class);
    }

    public RoleDTO getuAGAdminRoleDTO(){
        return clone(uAGAdminRoleDTO, RoleDTO.class);
    }

    public RoleForNewMicroserviceDTO getTrainingAdminRoleForNewMicroserviceDTO() {
        return clone(trainingAdminRoleForNewMicroserviceDTO, RoleForNewMicroserviceDTO.class);
    }
    public RoleForNewMicroserviceDTO getTrainingDesignerRoleForNewMicroserviceDTO() {
        return clone(trainingDesignerRoleForNewMicroserviceDTO, RoleForNewMicroserviceDTO.class);
    }
    public RoleForNewMicroserviceDTO getTrainingOrganizerRoleForNewMicroserviceDTO() {
        return clone(trainingOrganizerRoleForNewMicroserviceDTO, RoleForNewMicroserviceDTO.class);
    }
    public RoleForNewMicroserviceDTO getTrainingTraineeRoleForNewMicroserviceDTO() {
        return clone(trainingTraineeRoleForNewMicroserviceDTO, RoleForNewMicroserviceDTO.class);
    }

    public User getUser1() {
        return clone(user1, User.class);
    }

    public User getUser2() {
        return clone(user2, User.class);
    }

    public User getUser3() {
        return clone(user3, User.class);
    }

    public User getUser4() {
        return clone(user4, User.class);
    }

    public UserDTO getUser1DTO() {
        return clone(user1DTO, UserDTO.class);
    }

    public UserDTO getUser2DTO() {
        return clone(user2DTO, UserDTO.class);
    }

    public UserForGroupsDTO getUserForGroupsDTO1(){
        return clone(userForGroupsDTO1, UserForGroupsDTO.class);
    }
    public UserForGroupsDTO getUserForGroupsDTO2(){
        return clone(userForGroupsDTO2, UserForGroupsDTO.class);
    }
    public UserForGroupsDTO getUserForGroupsDTO3(){
        return clone(userForGroupsDTO3, UserForGroupsDTO.class);
    }

    public IDMGroup getTrainingTraineeGroup() {
        return clone(trainingTraineeGroup, IDMGroup.class);
    }

    public IDMGroup getTrainingOrganizerGroup() {
        return clone(trainingOrganizerGroup, IDMGroup.class);
    }

    public IDMGroup getTrainingDesignerGroup() {
        return clone(trainingDesignerGroup, IDMGroup.class);
    }

    public IDMGroup getTrainingAdminGroup() {
        return clone(trainingAdminGroup, IDMGroup.class);
    }

    public IDMGroup getUAGUserGroup() {
        return clone(uAGUserGroup, IDMGroup.class);
    }

    public IDMGroup getUAGDefaultGroup() {
        return clone(uAGDefaultGroup, IDMGroup.class);
    }

    public IDMGroup getUAGAdminGroup() {
        return clone(uAGAdminGroup, IDMGroup.class);
    }

    public GroupDTO getUAGUserGroupDTO() {
        return clone(uAGUserGroupDTO, GroupDTO.class);
    }

    public GroupDTO getUAGUAdminGroupDTO() {
        return clone(uAGUAdminGroupDTO, GroupDTO.class);
    }

    public NewGroupDTO getNewGroupDTO() {
        return clone(newGroupDTO, NewGroupDTO.class);
    }

    public UpdateGroupDTO getUpdateGroupDTO() {
        return clone(updateGroupDTO, UpdateGroupDTO.class);
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

    private IDMGroup generateGroup(String name, Set<Role> roles, int timeUntilExpiration){
        IDMGroup group = new IDMGroup();
        group.setName(name);
        group.setStatus(UserAndGroupStatus.VALID);
        group.setDescription("Description of " + name);
        group.setRoles(roles);
        group.setExpirationDate(LocalDateTime.now(Clock.systemUTC()).plusHours(timeUntilExpiration));
        return group;
    }

    private GroupDTO generateGroupDTO(String name, Source source){
        GroupDTO group = new GroupDTO();
        group.setName(name);
        group.setSource(source);
        group.setDescription("Description of " + name);
        return group;
    }

    private UpdateGroupDTO generateUpdateGroupDTO(String name){
        UpdateGroupDTO group = new UpdateGroupDTO();
        group.setName(name);
        group.setDescription("Description of " + name);
        return group;
    }

    private NewGroupDTO generateNewGroupDTO(String name){
        NewGroupDTO group = new NewGroupDTO();
        group.setName(name);
        group.setDescription("Description of " + name);
        return group;
    }

    private Role generateRole(String roleType, Microservice microservice, String description){
        Role role = new Role();
        role.setRoleType(roleType);
        role.setMicroservice(microservice);
        role.setDescription(description);
        return role;
    }

    private RoleDTO generateRoleDTO(String roleType, String description){
        RoleDTO role = new RoleDTO();
        role.setRoleType(roleType);
        role.setDescription(description);
        return role;
    }

    private RoleForNewMicroserviceDTO generateRoleForNewMicroserviceDTO(String roleType, String description){
        RoleForNewMicroserviceDTO role = new RoleForNewMicroserviceDTO();
        role.setRoleType(roleType);
        role.setDescription(description);
        role.setDefault(false);
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

    private UserDTO generateUserDTO(String login, String givenName, String familyName, String mail, String iss, byte[] picture){
        UserDTO user = new UserDTO();
        user.setLogin(login);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private UserForGroupsDTO generateUserForGroupsDTO(String login, String givenName, String familyName, String mail, String iss, byte[] picture){
        UserForGroupsDTO user = new UserForGroupsDTO();
        user.setLogin(login);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private NewMicroserviceDTO generateNewMicroserviceDTO(String name, String endpoint){
        NewMicroserviceDTO newMicroserviceDTO = new NewMicroserviceDTO();
        newMicroserviceDTO.setName(name);
        newMicroserviceDTO.setEndpoint(endpoint);
        return newMicroserviceDTO;
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

    private <T> T clone(Object object, Class<T> tClass){
        try {
            String json = mapper.writeValueAsString(object);
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
