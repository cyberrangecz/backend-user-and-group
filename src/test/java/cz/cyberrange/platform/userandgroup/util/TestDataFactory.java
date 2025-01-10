package cz.cyberrange.platform.userandgroup.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupViewDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserBasicViewDto;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserUpdateDTO;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import cz.cyberrange.platform.userandgroup.api.dto.enums.SourceDTO;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TestDataFactory {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final List<Role> generatedRoles = generateRoleList(50);
    private final List<Microservice> generatedMicroservices = generateMicroserviceList(50);
    private final List<User> generatedUsers = generateUserList(50);
    private final List<IDMGroup> generatedGroups = generateGroupList(50);

    private final Microservice crczpTrainingMicroservice
            = new Microservice("training", "http://training/api/v1");
    private final Microservice crczpUaGMicroservice
            = new Microservice("userAndGroup", "http://user-and-group/api/v1");

    private final NewMicroserviceDTO newMicroserviceDTO
            = generateNewMicroserviceDTO("training", "http://training/api/v1");

    private final MicroserviceDTO microserviceTrainingDTO
            = generateMicroserviceDTO("training", "http://training/api/v1");
    private final MicroserviceDTO microserviceUserAndGroupDTO
            = generateMicroserviceDTO("userAndGroup", "http://user-and-group/api/v1");

    private final Role trainingTraineeRole
            = generateRole("ROLE_TRAINING_TRAINEE", crczpTrainingMicroservice, "Trainee description");
    private final Role trainingOrganizerRole
            = generateRole("ROLE_TRAINING_ORGANIZER", crczpTrainingMicroservice, "Organizer description");
    private final Role trainingDesignerRole
            = generateRole("ROLE_TRAINING_DESIGNER", crczpTrainingMicroservice, "Designer description");
    private final Role trainingAdminRole
            = generateRole("ROLE_TRAINING_ADMIN", crczpTrainingMicroservice, "Training admin description");
    private final Role uAGPowerUserRole
            = generateRole(RoleType.ROLE_USER_AND_GROUP_POWER_USER.name(), crczpUaGMicroservice, "User description");
    private final Role uAGTraineeRole
            = generateRole(RoleType.ROLE_USER_AND_GROUP_TRAINEE.name(), crczpUaGMicroservice, "Guest description");
    private final Role uAGAdminRole
            = generateRole(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(), crczpUaGMicroservice, "UaG admin description");

    private final RoleDTO uAGPowerUserRoleDTO
            = generateRoleDTO(RoleType.ROLE_USER_AND_GROUP_POWER_USER.name(), "User description", 1L, "userAndGroup");
    private final RoleDTO uAGTraineeRoleDTO
            = generateRoleDTO(RoleType.ROLE_USER_AND_GROUP_TRAINEE.name(), "Guest description", 1L, "userAndGroup");
    private final RoleDTO uAGAdminRoleDTO
            = generateRoleDTO(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(), "UaG admin description", 1L, "userAndGroup");

    private final RoleForNewMicroserviceDTO trainingAdminRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_ADMIN", "Training admin description");
    private final RoleForNewMicroserviceDTO trainingOrganizerRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_ORGANIZER", "Training organizer description");
    private final RoleForNewMicroserviceDTO trainingDesignerRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_DESIGNER", "Training designer description");
    private final RoleForNewMicroserviceDTO trainingTraineeRoleForNewMicroserviceDTO
            = generateRoleForNewMicroserviceDTO("ROLE_TRAINING_TRAINEE", "Training trainee description");

    private final User user1
            = generateUser("852374@test.cz", "Garfield", "Pokorny", "852374@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());
    private final User user2
            = generateUser("632145@test.cz", "Marcel", "Watchman", "632145@mail.test.cz", "https://oidc.provider.cz/oidc/", "icon".getBytes());
    private final User user3
            = generateUser("77863@test.cz", "Drew", "Coyer", "77863@mail.test.cz", "https://oidc.provider.cz/oidc/", "default".getBytes());
    private final User user4
            = generateUser("794254@test.cz", "Garret", "Cull", "794254@mail.test.cz", "https://oidc.provider.cz/oidc/", "profile".getBytes());

    private final UserDTO user1DTO
            = generateUserDTO("852374@test.cz", "Garfield", "Pokorny", "852374@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());
    private final UserDTO user2DTO
            = generateUserDTO("632145@test.cz", "Marcel", "Watchman", "632145@mail.test.cz", "https://oidc.provider.cz/oidc/", "icon".getBytes());

    private final UserBasicViewDto userBasicViewDto1
            = generateBasicUserViewDTO("852374@test.cz", "Garfield", "Pokorny", "852374@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());
    private final UserBasicViewDto userBasicViewDto2
            = generateBasicUserViewDTO("632145@test.cz", "Marcel", "Watchman", "632145@mail.test.cz", "https://oidc.provider.cz/oidc/", "icon".getBytes());

    private final UserForGroupsDTO userForGroupsDTO1
            = generateUserForGroupsDTO("852374@test.cz", "Garfield", "Pokorny", "852374@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());
    private final UserForGroupsDTO userForGroupsDTO2
            = generateUserForGroupsDTO("632145@test.cz", "Marcel", "Watchman", "632145@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());
    private final UserForGroupsDTO userForGroupsDTO3
            = generateUserForGroupsDTO("794254@test.cz", "Garret", "Cull", "794254@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());

    private final UserUpdateDTO userUpdateDTO
            = generateUserUpdateDTO("852374@test.cz", "Garfield", "Pokorny", "852374@mail.test.cz", "https://oidc.provider.cz/oidc/", "pic".getBytes());

    private final IDMGroup trainingTraineeGroup = generateGroup("trainingTraineeGroup", new HashSet<Role>(List.of(trainingTraineeRole)), 9);
    private final IDMGroup trainingOrganizerGroup = generateGroup("trainingOrganizerGroup", new HashSet<Role>(List.of(trainingOrganizerRole)), 23);
    private final IDMGroup trainingDesignerGroup = generateGroup("trainingDesignerGroup", new HashSet<Role>(List.of(trainingDesignerRole)), 5);
    private final IDMGroup trainingAdminGroup = generateGroup("trainingAdminGroup", new HashSet<Role>(List.of(trainingAdminRole)), 6);
    private final IDMGroup uAGPowerUserGroup = generateGroup(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName(), new HashSet<Role>(List.of(uAGPowerUserRole)), 1);
    private final IDMGroup uAGDefaultGroup = generateGroup(ImplicitGroupNames.DEFAULT_GROUP.getName(), new HashSet<Role>(List.of(uAGTraineeRole)), 7);
    private final IDMGroup uAGAdminGroup = generateGroup(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), new HashSet<Role>(List.of(uAGAdminRole)), 29);

    private final GroupDTO uAGPowerUserGroupDTO = generateGroupDTO(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName(), SourceDTO.INTERNAL);
    private final GroupDTO uAGUAdminGroupDTO = generateGroupDTO(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), SourceDTO.INTERNAL);
    private final GroupViewDTO powerUserGroupViewDTO = generateGroupViewDTO(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName(), SourceDTO.INTERNAL);
    private final GroupViewDTO adminGroupViewDTO = generateGroupViewDTO(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), SourceDTO.INTERNAL);
    private final NewGroupDTO newGroupDTO = generateNewGroupDTO("New Group");
    private final UpdateGroupDTO updateGroupDTO = generateUpdateGroupDTO("Update Group");

    public Microservice getCrczpTrainingMicroservice() {
        return clone(crczpTrainingMicroservice, Microservice.class);
    }

    public Microservice getCrczpUaGMicroservice() {
        return clone(crczpUaGMicroservice, Microservice.class);
    }

    public NewMicroserviceDTO getNewMicroserviceDTO() {
        return clone(newMicroserviceDTO, NewMicroserviceDTO.class);
    }

    public MicroserviceDTO getMicroserviceTrainingDTO() {
        return clone(microserviceTrainingDTO, MicroserviceDTO.class);
    }

    public MicroserviceDTO getMicroserviceUserAndGroupDTO() {
        return clone(microserviceUserAndGroupDTO, MicroserviceDTO.class);
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

    public Role getUAGPowerUserRole() {
        return clone(uAGPowerUserRole, Role.class);
    }

    public Role getUAGTraineeRole() {
        return clone(uAGTraineeRole, Role.class);
    }

    public Role getUAGAdminRole() {
        return clone(uAGAdminRole, Role.class);
    }

    public RoleDTO getUAGPowerUserRoleDTO() {
        return clone(uAGPowerUserRoleDTO, RoleDTO.class);
    }

    public RoleDTO getUAGTraineeRoleDTO() {
        return clone(uAGTraineeRoleDTO, RoleDTO.class);
    }

    public RoleDTO getUAGAdminRoleDTO() {
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

    public UserBasicViewDto getUserBasicViewDto1() {
        return clone(userBasicViewDto1, UserBasicViewDto.class);
    }

    public UserBasicViewDto getUserBasicViewDto2() {
        return clone(userBasicViewDto2, UserBasicViewDto.class);
    }

    public UserForGroupsDTO getUserForGroupsDTO1() {
        return clone(userForGroupsDTO1, UserForGroupsDTO.class);
    }

    public UserForGroupsDTO getUserForGroupsDTO2() {
        return clone(userForGroupsDTO2, UserForGroupsDTO.class);
    }

    public UserForGroupsDTO getUserForGroupsDTO3() {
        return clone(userForGroupsDTO3, UserForGroupsDTO.class);
    }

    public UserUpdateDTO getUserUpdateDTO() {
        return clone(userUpdateDTO, UserUpdateDTO.class);
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

    public IDMGroup getUAGPowerUserGroup() {
        return clone(uAGPowerUserGroup, IDMGroup.class);
    }

    public IDMGroup getUAGDefaultGroup() {
        return clone(uAGDefaultGroup, IDMGroup.class);
    }

    public IDMGroup getUAGAdminGroup() {
        return clone(uAGAdminGroup, IDMGroup.class);
    }

    public GroupDTO getUAGUserGroupDTO() {
        return clone(uAGPowerUserGroupDTO, GroupDTO.class);
    }

    public GroupDTO getUAGUAdminGroupDTO() {
        return clone(uAGUAdminGroupDTO, GroupDTO.class);
    }

    public GroupViewDTO getPowerUserGroupViewDTO() {
        return clone(powerUserGroupViewDTO, GroupViewDTO.class);
    }

    public GroupViewDTO getAdminGroupViewDTO() {
        return clone(adminGroupViewDTO, GroupViewDTO.class);
    }

    public NewGroupDTO getNewGroupDTO() {
        return clone(newGroupDTO, NewGroupDTO.class);
    }

    public UpdateGroupDTO getUpdateGroupDTO() {
        return clone(updateGroupDTO, UpdateGroupDTO.class);
    }

    public List<Role> getGeneratedRoles() {
        return Collections.unmodifiableList(generatedRoles);
    }

    public List<Microservice> getGeneratedMicroservices() {
        return Collections.unmodifiableList(generatedMicroservices);
    }

    public List<User> getGeneratedUsers() {
        return Collections.unmodifiableList(generatedUsers);
    }

    public List<IDMGroup> getGeneratedGroups() {
        return Collections.unmodifiableList(generatedGroups);
    }

    private IDMGroup generateGroup(String name, Set<Role> roles, int timeUntilExpiration) {
        IDMGroup group = new IDMGroup();
        group.setName(name);
        group.setStatus(UserAndGroupStatus.VALID);
        group.setDescription("Description of " + name);
        group.setRoles(roles);
        group.setExpirationDate(LocalDateTime.now(Clock.systemUTC()).plusHours(timeUntilExpiration));
        return group;
    }

    private GroupDTO generateGroupDTO(String name, SourceDTO source) {
        GroupDTO group = new GroupDTO();
        group.setName(name);
        group.setSource(source);
        group.setDescription("Description of " + name);
        return group;
    }

    private GroupViewDTO generateGroupViewDTO(String name, SourceDTO source) {
        GroupViewDTO group = new GroupViewDTO();
        group.setName(name);
        group.setSource(source);
        group.setDescription("Description of " + name);
        return group;
    }

    private UpdateGroupDTO generateUpdateGroupDTO(String name) {
        UpdateGroupDTO group = new UpdateGroupDTO();
        group.setName(name);
        group.setDescription("Description of " + name);
        return group;
    }

    private NewGroupDTO generateNewGroupDTO(String name) {
        NewGroupDTO group = new NewGroupDTO();
        group.setName(name);
        group.setDescription("Description of " + name);
        return group;
    }

    private Role generateRole(String roleType, Microservice microservice, String description) {
        Role role = new Role();
        role.setRoleType(roleType);
        role.setMicroservice(microservice);
        role.setDescription(description);
        return role;
    }

    private RoleDTO generateRoleDTO(String roleType, String description, Long idOfMicroservice, String nameOfMicroservice) {
        RoleDTO role = new RoleDTO();
        role.setRoleType(roleType);
        role.setDescription(description);
        role.setIdOfMicroservice(idOfMicroservice);
        role.setNameOfMicroservice(nameOfMicroservice);
        return role;
    }

    private RoleForNewMicroserviceDTO generateRoleForNewMicroserviceDTO(String roleType, String description) {
        RoleForNewMicroserviceDTO role = new RoleForNewMicroserviceDTO();
        role.setRoleType(roleType);
        role.setDescription(description);
        role.setDefault(false);
        return role;
    }

    private User generateUser(String sub, String givenName, String familyName, String mail, String iss, byte[] picture) {
        User user = new User();
        user.setSub(sub);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setStatus(UserAndGroupStatus.VALID);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private UserDTO generateUserDTO(String sub, String givenName, String familyName, String mail, String iss, byte[] picture) {
        UserDTO user = new UserDTO();
        user.setSub(sub);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private UserBasicViewDto generateBasicUserViewDTO(String sub, String givenName, String familyName, String mail, String iss, byte[] picture) {
        UserBasicViewDto user = new UserBasicViewDto();
        user.setSub(sub);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private UserForGroupsDTO generateUserForGroupsDTO(String sub, String givenName, String familyName, String mail, String iss, byte[] picture) {
        UserForGroupsDTO user = new UserForGroupsDTO();
        user.setSub(sub);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private UserUpdateDTO generateUserUpdateDTO(String sub, String givenName, String familyName, String mail, String iss, byte[] picture) {
        UserUpdateDTO user = new UserUpdateDTO();
        user.setSub(sub);
        user.setFullName(givenName + " " + familyName);
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        user.setMail(mail);
        user.setIss(iss);
        user.setPicture(picture);
        return user;
    }

    private NewMicroserviceDTO generateNewMicroserviceDTO(String name, String endpoint) {
        NewMicroserviceDTO newMicroserviceDTO = new NewMicroserviceDTO();
        newMicroserviceDTO.setName(name);
        newMicroserviceDTO.setEndpoint(endpoint);
        return newMicroserviceDTO;
    }

    private MicroserviceDTO generateMicroserviceDTO(String name, String endpoint) {
        MicroserviceDTO microserviceDTO = new MicroserviceDTO();
        microserviceDTO.setName(name);
        microserviceDTO.setEndpoint(endpoint);
        return microserviceDTO;
    }

    private List<Role> generateRoleList(int count) {
        List<Role> generatedRoles = new ArrayList<>();
        Microservice microserviceForGeneratedRoles =
                new Microservice("microserviceForGeneratedRoles", "/microserviceForGeneratedRoles");
        for (int i = 0; i < count; i++) {
            Role role = new Role();
            role.setMicroservice(microserviceForGeneratedRoles);
            role.setRoleType("Generated Role Type " + i);
            role.setDescription("Generated Role Description " + i);
            generatedRoles.add(role);
        }
        return generatedRoles;
    }

    private List<Microservice> generateMicroserviceList(int count) {
        List<Microservice> generatedMicroservices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Microservice microservice = new Microservice();
            microservice.setName("Generated Microservice " + i);
            microservice.setEndpoint("/endpoint" + i);
            generatedMicroservices.add(microservice);
        }
        return generatedMicroservices;
    }

    private List<User> generateUserList(int count) {
        List<User> generatedUsers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setSub("Generated User sub " + i);
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

    private List<IDMGroup> generateGroupList(int count) {
        List<IDMGroup> generatedGroups = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            IDMGroup group = new IDMGroup();
            group.setName("Generated Group Name " + i);
            group.setStatus(UserAndGroupStatus.VALID);
            group.setDescription("Generated Description " + i);
            group.setExpirationDate(LocalDateTime.now(Clock.systemUTC()).plusMinutes(i));
            generatedGroups.add(group);
        }
        return generatedGroups;
    }

    private <T> T clone(Object object, Class<T> tClass) {
        try {
            String json = mapper.writeValueAsString(object);
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
