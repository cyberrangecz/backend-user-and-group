package cz.muni.ics.kypo.userandgroup.integration;


import com.fasterxml.jackson.core.type.TypeReference;
import cz.muni.ics.kypo.userandgroup.controller.GroupsRestController;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.group.AddUsersToGroupDTO;
import cz.muni.ics.kypo.userandgroup.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.dto.group.UpdateGroupDTO;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.exceptions.errors.ApiEntityError;
import cz.muni.ics.kypo.userandgroup.handler.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.util.ObjectConverter;
import cz.muni.ics.kypo.userandgroup.util.TestAuthorityGranter;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static cz.muni.ics.kypo.userandgroup.util.ObjectConverter.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        IntegrationTestApplication.class,
        GroupsRestController.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class IDMGroupsIntegrationTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GroupsRestController groupsRestController;
    @Autowired
    private MicroserviceRepository microserviceRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private IDMGroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    private ModelMapper modelMapper;
    private MockMvc mvc;

    private NewGroupDTO newOrganizerGroupDTO;
    private UserForGroupsDTO organizerDTO1, organizerDTO2;
    private User organizer1, organizer2, user1, user2;
    private IDMGroup adminGroup, powerUserGroup, defaultGroup, organizerGroup, designerGroup;
    private Role adminRole, guestRole, powerUserRole, designerRole, organizerRole, traineeRole;
    private Microservice microserviceUserAndGroup, microserviceTraining;

    @BeforeEach
    public void init() throws SQLException {
        this.mvc = MockMvcBuilders
                .standaloneSetup(groupsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();
        modelMapper = new ModelMapper();

        organizer1 = testDataFactory.getUser1();
        organizer2 = testDataFactory.getUser2();
        user1 = testDataFactory.getUser3();
        user2 = testDataFactory.getUser4();
        userRepository.saveAll(new HashSet<>(Set.of(organizer1, organizer2, user1, user2)));

        microserviceUserAndGroup = testDataFactory.getKypoUaGMicroservice();
        microserviceTraining = testDataFactory.getKypoTrainingMicroservice();
        microserviceRepository.saveAll(new HashSet<>(Set.of(microserviceTraining, microserviceUserAndGroup)));

        adminGroup = testDataFactory.getUAGAdminGroup();
        adminGroup.setExpirationDate(null);
        powerUserGroup = testDataFactory.getUAGPowerUserGroup();
        powerUserGroup.setExpirationDate(null);
        defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setExpirationDate(null);
        organizerGroup = testDataFactory.getTrainingOrganizerGroup();
        organizerGroup.setExpirationDate(null);
        designerGroup = testDataFactory.getTrainingDesignerGroup();
        designerGroup.setExpirationDate(null);

        adminRole = adminGroup.getRoles().iterator().next();
        adminRole.setMicroservice(microserviceUserAndGroup);
        powerUserRole = powerUserGroup.getRoles().iterator().next();
        powerUserRole.setMicroservice(microserviceUserAndGroup);
        guestRole = defaultGroup.getRoles().iterator().next();
        guestRole.setMicroservice(microserviceUserAndGroup);
        designerRole = designerGroup.getRoles().iterator().next();
        designerRole.setMicroservice(microserviceTraining);
        organizerRole = organizerGroup.getRoles().iterator().next();
        organizerRole.setMicroservice(microserviceTraining);
        traineeRole = testDataFactory.getTrainingTraineeRole();
        traineeRole.setMicroservice(microserviceTraining);
        roleRepository.saveAll(new HashSet<>(Set.of(adminRole, powerUserRole, guestRole, designerRole, organizerRole, traineeRole)));

        defaultGroup.addRole(traineeRole);
        groupRepository.saveAll(new HashSet<>(Set.of(adminGroup, powerUserGroup, defaultGroup, organizerGroup, designerGroup)));


        organizerDTO1 = testDataFactory.getUserForGroupsDTO1();
        organizerDTO2 = testDataFactory.getUserForGroupsDTO2();

        newOrganizerGroupDTO = new NewGroupDTO();
        newOrganizerGroupDTO.setName("Organizer group");
        newOrganizerGroupDTO.setDescription("Main group for organizers");

        defaultGroup.setUsers(new HashSet<>(Set.of(organizer1, organizer2, user1, user2)));
        powerUserGroup.setUsers(new HashSet<>(Set.of(user1, user2)));
        groupRepository.save(powerUserGroup);

        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);
    }

    @Test
    public void createNewGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void createNewGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void createNewGroupWithoutUsers() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        Optional<IDMGroup> createdGroup = groupRepository.findByName(newOrganizerGroupDTO.getName());
        assertTrue(createdGroup.isPresent());

        assertEquals(0, createdGroup.get().getUsers().size());
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), modelMapper.map(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void createNewGroupWithUsers() throws Exception {
        organizerDTO1.setId(organizer1.getId());
        organizerDTO2.setId(organizer2.getId());
        newOrganizerGroupDTO.setUsers(Set.of(organizerDTO1, organizerDTO2));
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        Optional<IDMGroup> createdGroup = groupRepository.findByName(newOrganizerGroupDTO.getName());
        assertTrue(createdGroup.isPresent());
        assertEquals(2, createdGroup.get().getUsers().size());
        assertTrue(createdGroup.get().getUsers().containsAll(Set.of(organizer1, organizer2)));
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), modelMapper.map(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void createNewGroupWithImportedUsersFromGroups() throws Exception {
        newOrganizerGroupDTO.setGroupIdsOfImportedUsers(List.of(powerUserGroup.getId()));
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        Optional<IDMGroup> createdGroup = groupRepository.findByName(newOrganizerGroupDTO.getName());
        assertTrue(createdGroup.isPresent());
        assertEquals(powerUserGroup.getUsers().size(), createdGroup.get().getUsers().size());
        assertTrue(createdGroup.get().getUsers().containsAll(powerUserGroup.getUsers()));

        GroupDTO createdGroupDTO = convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class);
        assertTrue(createdGroupDTO.getUsers().containsAll(createdGroup.get().getUsers()
                .stream()
                .map(user -> modelMapper.map(user, UserForGroupsDTO.class))
                .collect(Collectors.toList())
        ));
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), modelMapper.map(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void updateNewGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(powerUserGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription(powerUserGroup.getDescription());
        Exception exception = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void updateGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(powerUserGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription(powerUserGroup.getDescription());
        Exception exception = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void updateNameOfMainGroup() throws Exception {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(powerUserGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription(powerUserGroup.getDescription());
        String groupNameBefore = powerUserGroup.getName();

        MockHttpServletResponse response = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        Optional<IDMGroup> updatedGroup = groupRepository.findById(powerUserGroup.getId());
        assertTrue(updatedGroup.isPresent());
        assertEquals(groupNameBefore, updatedGroup.get().getName());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", powerUserGroup.getId().toString(),
                "Name of main group cannot be changed");
    }

    @Test
    public void updateDescriptionOfMainGroup() throws Exception {
        String newDescription = "New description of main group";
        String groupDescriptionBefore = powerUserGroup.getName();
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(powerUserGroup.getId());
        updateGroupDTO.setName(powerUserGroup.getName());
        updateGroupDTO.setDescription(newDescription);

        mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Optional<IDMGroup> updatedGroup = groupRepository.findById(powerUserGroup.getId());
        assertTrue(updatedGroup.isPresent());
        assertNotEquals(groupDescriptionBefore, updatedGroup.get().getDescription());
        assertEquals(newDescription, updatedGroup.get().getDescription());
    }

    @Test
    public void updateNameOfGroup() throws Exception {
        String newName = "Changed name of group";
        String groupNameBefore = organizerGroup.getName();
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(organizerGroup.getId());
        updateGroupDTO.setName(newName);
        updateGroupDTO.setDescription(organizerGroup.getDescription());

        mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Optional<IDMGroup> updatedGroup = groupRepository.findById(organizerGroup.getId());
        assertTrue(updatedGroup.isPresent());
        assertNotEquals(groupNameBefore, updatedGroup.get().getName());
        assertEquals(newName, updatedGroup.get().getName());
    }

    @Test
    public void removeUsersFromGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(delete("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void removeUsersFromGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(delete("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void removeAllUsersFromGroup() throws Exception {
        assertEquals(2, powerUserGroup.getUsers().size());

        mvc.perform(delete("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(0, powerUserGroup.getUsers().size());
    }

    @Test
    public void removeOneUserFromGroup() throws Exception {
        assertEquals(2, powerUserGroup.getUsers().size());

        mvc.perform(delete("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(1, powerUserGroup.getUsers().size());
        assertTrue(powerUserGroup.getUsers().contains(user2));
    }

    @Test
    public void removeUserFromGroupWithGroupNotFound() throws Exception {
        Long groupId = 500L;
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}/users", groupId)
                .content(convertObjectToJsonBytes(List.of(user1.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", groupId.toString(),
                "Entity IDMGroup (id: 500) not found.");
    }

    @Test
    public void removeUserNotInDBFromGroup() throws Exception {
        int numberOfUsersBefore = powerUserGroup.getUsers().size();
        mvc.perform(delete("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(List.of(100L)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(numberOfUsersBefore, powerUserGroup.getUsers().size());
    }

    @Test
    public void addUsersToGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        Exception exception = mvc.perform(put("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void addUsersToGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        Exception exception = mvc.perform(put("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void addUsersToGroup() throws Exception {
        assertEquals(2, powerUserGroup.getUsers().size());
        Set<User> usersInGroupBefore = powerUserGroup.getUsers();
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));

        mvc.perform(put("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(3, powerUserGroup.getUsers().size());
        usersInGroupBefore.add(organizer1);
        assertTrue(powerUserGroup.getUsers().containsAll(usersInGroupBefore));

    }

    @Test
    public void addUserToGroupAlreadyThere() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user1.getId()));
        int numberOfUsersBefore = powerUserGroup.getUsers().size();
        mvc.perform(put("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(numberOfUsersBefore, powerUserGroup.getUsers().size());
        assertTrue(powerUserGroup.getUsers().contains(user1));
    }

    @Test
    public void addUsersToGroupFromOtherGroup() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(defaultGroup.getId()));
        mvc.perform(put("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(defaultGroup.getUsers().size(), powerUserGroup.getUsers().size());
        assertTrue(powerUserGroup.getUsers().containsAll(defaultGroup.getUsers()));
    }

    @Test
    public void addUsersToGroupIndividuallyAndFromOtherGroup() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(defaultGroup.getId()));
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        mvc.perform(put("/groups/{id}/users", powerUserGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(defaultGroup.getUsers().size(), powerUserGroup.getUsers().size());
        assertTrue(powerUserGroup.getUsers().containsAll(defaultGroup.getUsers()));
        assertTrue(powerUserGroup.getUsers().contains(organizer1));
    }

    @Test
    public void addUsersToGroupFromWithGroupNotFound() throws Exception {
        Long groupId = 600L;
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(defaultGroup.getId()));

        MockHttpServletResponse response = mvc.perform(put("/groups/{id}/users", groupId)
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", groupId.toString(),
                "Entity IDMGroup (id: 600) not found.");
    }

    @Test
    public void deleteGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Long organizerId = organizerGroup.getId();
        Exception exception = mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Long organizerId = organizerGroup.getId();
        Exception exception = mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteGroup() throws Exception {
        Long organizerId = organizerGroup.getId();
        mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertFalse(groupRepository.existsById(organizerId));
    }

    @Test
    public void deleteGroupNotFoundInDB() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}", 100)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Entity IDMGroup (id: 100) not found.");
    }

    @Test
    public void deleteMainGroup() throws Exception {
        powerUserGroup.setUsers(new HashSet<>());
        groupRepository.save(powerUserGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}", powerUserGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "name", powerUserGroup.getName(),
                "This group is User and Group default group that cannot be deleted.");
    }

    @Test
    public void deleteGroupWithUsers() throws Exception {
        organizerGroup.addUser(user1);
        groupRepository.save(organizerGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}", organizerGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", organizerGroup.getId().toString(),
                "The group must be empty (without users) before it is deleted.");
    }

    @Test
    public void deleteGroupsUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), designerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteGroupsGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), designerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteGroups() throws Exception {
        groupRepository.saveAll(Set.of(organizerGroup, designerGroup));
        Long organizerGroupId = organizerGroup.getId();
        Long designerGroupId = designerGroup.getId();
        mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), designerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertFalse(groupRepository.existsById(designerGroupId));
        assertFalse(groupRepository.existsById(organizerGroupId));
    }

    @Test
    public void deleteGroupsWithMainGroup() throws Exception {
        Long organizerGroupId = organizerGroup.getId();
        powerUserGroup.setUsers(new HashSet<>());
        groupRepository.save(powerUserGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), powerUserGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertTrue(groupRepository.existsById(powerUserGroup.getId()));
        assertTrue(groupRepository.existsById(organizerGroupId));
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "name", powerUserGroup.getName(),
                "This group is User and Group default group that cannot be deleted.");

    }

    @Test
    public void deleteGroupsWithUsers() throws Exception {
        organizerGroup.addUser(user1);
        groupRepository.save(organizerGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertTrue(groupRepository.existsById(powerUserGroup.getId()));
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", organizerGroup.getId().toString(),
                "The group must be empty (without users) before it is deleted.");
    }

    @Test
    public void deleteGroupsWithEmptyList() throws Exception {
        mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getGroupsUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroupsGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<GroupDTO> responseGroupDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<GroupDTO>>() {
        }).getContent();
        assertTrue(responseGroupDTOs.contains(convertToGroupDTO(powerUserGroup)));
        assertTrue(responseGroupDTOs.contains(convertToGroupDTO(powerUserGroup)));
        assertTrue(responseGroupDTOs.contains(convertToGroupDTO(organizerGroup)));
    }

    @Test
    public void getGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(get("/groups/{id}", powerUserGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(get("/groups/{id}", powerUserGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}", powerUserGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), modelMapper.map(powerUserGroup, GroupDTO.class));
    }

    @Test
    public void getGroupNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Entity IDMGroup (id: 100) not found.");
    }

    @Test
    public void getRolesOfGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getRolesOfGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getRolesOfGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> responseRoles = convertJsonBytesToObject(response.getContentAsString(), new TypeReference<PageResultResource<RoleDTO>>() {
        });
        assertTrue(responseRoles.getContent().contains(convertToRoleDTO(traineeRole)));
        assertTrue(responseRoles.getContent().contains(convertToRoleDTO(guestRole)));
        assertEquals(groupRepository.findByNameWithRoles(defaultGroup.getName()).get().getRoles().size(), responseRoles.getContent().size());
    }

    @Test
    public void getRolesOfGroupGroupNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}/roles", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Group not found.");
    }

    @Test
    public void assignRoleToGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), organizerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void assignRoleToGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), organizerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void assignRoleToGroup() throws Exception {
        assertFalse(organizerGroup.getRoles().contains(designerRole));
        int numberOfRolesBefore = organizerGroup.getRoles().size();
        mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertTrue(organizerGroup.getRoles().contains(designerRole));
        assertEquals(numberOfRolesBefore + 1, organizerGroup.getRoles().size());
    }

    @Test
    public void assignRoleToGroupAlreadyAssigned() throws Exception {
        assertTrue(organizerGroup.getRoles().contains(organizerRole));
        int numberOfRolesBefore = organizerGroup.getRoles().size();
        mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), organizerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertTrue(organizerGroup.getRoles().contains(organizerRole));
        assertEquals(numberOfRolesBefore, organizerGroup.getRoles().size());
    }

    @Test
    public void assignRoleToGroupRoleNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "id", "100",
                "Role not found. Start up of the project or registering of microservice probably went wrong, please contact support.");
    }

    @Test
    public void assignRoleToGroupGroupNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(put("/groups/{groupId}/roles/{roleId}", 100L, designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Entity IDMGroup (id: 100) not found.");
    }

    @Test
    public void removeRoleToGroupUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void removeRoleToGroupGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void removeRoleFromGroup() throws Exception {
        organizerGroup.addRole(designerRole);
        groupRepository.save(organizerGroup);
        int numberOfRolesBefore = organizerGroup.getRoles().size();
        mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertFalse(organizerGroup.getRoles().contains(designerRole));
        assertEquals(numberOfRolesBefore - 1, organizerGroup.getRoles().size());
    }

    @Test
    public void removeRoleFromGroupNotAssignedToGroup() throws Exception {
        assertFalse(organizerGroup.getRoles().contains(designerRole));
        int numberOfRolesBefore = organizerGroup.getRoles().size();
        MockHttpServletResponse response = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "id", designerRole.getId().toString(),
                "Role was not found in group.");
        assertFalse(organizerGroup.getRoles().contains(designerRole));
        assertEquals(numberOfRolesBefore, organizerGroup.getRoles().size());
    }

    @Test
    public void removeRoleFromGroupRoleNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "id", "100",
                "Role was not found in group.");
    }

    @Test
    public void removeRoleFromGroupGroupNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", 100L, designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Entity IDMGroup (id: 100) not found.");
    }

    @Test
    public void removeMainRoleFromMainGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", powerUserGroup.getId(), powerUserRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "roleType", powerUserRole.getRoleType(),
                "Main role of the group cannot be removed.");
    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        assertEquals(value, entityErrorDetail.getIdentifierValue().toString());
        assertEquals(reason, entityErrorDetail.getReason());
    }

    @SpringBootApplication
    static class TestConfiguration {

    }

}

