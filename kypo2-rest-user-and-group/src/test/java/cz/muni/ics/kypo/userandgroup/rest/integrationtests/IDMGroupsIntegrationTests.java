package cz.muni.ics.kypo.userandgroup.rest.integrationtests;


import com.fasterxml.jackson.core.type.TypeReference;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.AddUsersToGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.Microservice;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import cz.muni.ics.kypo.userandgroup.entities.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.GroupsRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static cz.muni.ics.kypo.userandgroup.rest.util.ObjectConverter.*;
import static cz.muni.ics.kypo.userandgroup.rest.util.TestAuthorityGranter.mockSpringSecurityContextForGet;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GroupsRestController.class, TestDataFactory.class})
@DataJpaTest
@Import(RestConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class IDMGroupsIntegrationTests {

    private BeanMapping beanMapping;
    private MockMvc mvc;
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

    private NewGroupDTO newOrganizerGroupDTO;
    private UserForGroupsDTO organizerDTO1, organizerDTO2;
    private User organizer1, organizer2, user1, user2;
    private IDMGroup adminGroup, userGroup, defaultGroup, organizerGroup, designerGroup;
    private Role adminRole, guestRole, userRole, designerRole, organizerRole, traineeRole;
    private Microservice microserviceUserAndGroup, microserviceTraining;

    @SpringBootApplication
    static class TestConfiguration {

    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders
                .standaloneSetup(groupsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();
        beanMapping = new BeanMappingImpl(new ModelMapper());

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
        userGroup = testDataFactory.getUAGUserGroup();
        userGroup.setExpirationDate(null);
        defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setExpirationDate(null);
        organizerGroup = testDataFactory.getTrainingOrganizerGroup();
        organizerGroup.setExpirationDate(null);
        designerGroup = testDataFactory.getTrainingDesignerGroup();
        designerGroup.setExpirationDate(null);

        adminRole = adminGroup.getRoles().iterator().next();
        adminRole.setMicroservice(microserviceUserAndGroup);
        userRole = userGroup.getRoles().iterator().next();
        userRole.setMicroservice(microserviceUserAndGroup);
        guestRole = defaultGroup.getRoles().iterator().next();
        guestRole.setMicroservice(microserviceUserAndGroup);
        designerRole = designerGroup.getRoles().iterator().next();
        designerRole.setMicroservice(microserviceTraining);
        organizerRole = organizerGroup.getRoles().iterator().next();
        organizerRole.setMicroservice(microserviceTraining);
        traineeRole = testDataFactory.getTrainingTraineeRole();
        traineeRole.setMicroservice(microserviceTraining);
        roleRepository.saveAll(new HashSet<>(Set.of(adminRole, userRole, guestRole, designerRole, organizerRole, traineeRole)));

        defaultGroup.addRole(traineeRole);
        groupRepository.saveAll(new HashSet<>(Set.of(adminGroup, userGroup, defaultGroup, organizerGroup, designerGroup)));


        organizerDTO1 = testDataFactory.getUserForGroupsDTO1();
        organizerDTO2 = testDataFactory.getUserForGroupsDTO2();

        newOrganizerGroupDTO = new NewGroupDTO();
        newOrganizerGroupDTO.setName("Organizer group");
        newOrganizerGroupDTO.setDescription("Main group for organizers");

        defaultGroup.setUsers(new HashSet<>(Set.of(organizer1, organizer2, user1, user2)));
        userGroup.setUsers(new HashSet<>(Set.of(user1, user2)));
        groupRepository.save(userGroup);

        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);

    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "microservice", "idm_group", "users", "role");
    }

    @Test
    public void createNewGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void createNewGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
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
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), beanMapping.mapTo(createdGroup.get(), GroupDTO.class));
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
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), beanMapping.mapTo(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void createNewGroupWithImportedUsersFromGroups() throws Exception {
        newOrganizerGroupDTO.setGroupIdsOfImportedUsers(List.of(userGroup.getId()));
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        Optional<IDMGroup> createdGroup = groupRepository.findByName(newOrganizerGroupDTO.getName());
        assertTrue(createdGroup.isPresent());
        assertEquals(userGroup.getUsers().size(), createdGroup.get().getUsers().size());
        assertTrue(createdGroup.get().getUsers().containsAll(userGroup.getUsers()));

        GroupDTO createdGroupDTO = convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class);
        assertTrue(createdGroupDTO.getUsers().containsAll(beanMapping.mapTo(createdGroup.get().getUsers(), UserForGroupsDTO.class)));
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), beanMapping.mapTo(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void updateNewGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(userGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription(userGroup.getDescription());
        Exception exception = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void updateGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(userGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription(userGroup.getDescription());
        Exception exception = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void updateNameOfMainGroup() throws Exception {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(userGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription(userGroup.getDescription());
        String groupNameBefore = userGroup.getName();

        MockHttpServletResponse response = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        Optional<IDMGroup> updatedGroup = groupRepository.findById(userGroup.getId());
        assertTrue(updatedGroup.isPresent());
        assertEquals(groupNameBefore, updatedGroup.get().getName());
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", userGroup.getId().toString(),
                "Name of main group cannot be changed");
    }

    @Test
    public void updateDescriptionOfMainGroup() throws Exception {
        String newDescription = "New description of main group";
        String groupDescriptionBefore = userGroup.getName();
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(userGroup.getId());
        updateGroupDTO.setName(userGroup.getName());
        updateGroupDTO.setDescription(newDescription);

        mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Optional<IDMGroup> updatedGroup = groupRepository.findById(userGroup.getId());
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
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void removeUsersFromGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void removeAllUsersFromGroup() throws Exception {
        assertEquals(2, userGroup.getUsers().size());

        mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(0, userGroup.getUsers().size());
    }

    @Test
    public void removeOneUserFromGroup() throws Exception {
        assertEquals(2, userGroup.getUsers().size());

        mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(1, userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().contains(user2));
    }

    @Test
    public void removeUserFromGroupWithGroupNotFound() throws Exception {
        Long groupId = 500L;
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}/users", groupId)
                .content(convertObjectToJsonBytes(List.of(user1.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", groupId.toString(),
                "Group not found.");
    }

    @Test
    public void removeUserNotInDBFromGroup() throws Exception {
        int numberOfUsersBefore = userGroup.getUsers().size();
        mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(100L)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(numberOfUsersBefore, userGroup.getUsers().size());
    }

    @Test
    public void addUsersToGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        Exception exception = mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void addUsersToGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        Exception exception = mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void addUsersToGroup() throws Exception {
        assertEquals(2, userGroup.getUsers().size());
        Set<User> usersInGroupBefore = userGroup.getUsers();
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));

        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(3, userGroup.getUsers().size());
        usersInGroupBefore.add(organizer1);
        assertTrue(userGroup.getUsers().containsAll(usersInGroupBefore));

    }

    @Test
    public void addUserToGroupAlreadyThere() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user1.getId()));
        int numberOfUsersBefore = userGroup.getUsers().size();
        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(numberOfUsersBefore, userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().contains(user1));
    }

    @Test
    public void addUsersToGroupFromOtherGroup() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(defaultGroup.getId()));
        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(defaultGroup.getUsers().size(), userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().containsAll(defaultGroup.getUsers()));
    }

    @Test
    public void addUsersToGroupIndividuallyAndFromOtherGroup() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(defaultGroup.getId()));
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(defaultGroup.getUsers().size(), userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().containsAll(defaultGroup.getUsers()));
        assertTrue(userGroup.getUsers().contains(organizer1));
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", groupId.toString(),
                "Group not found.");
    }

    @Test
    public void deleteGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Long organizerId = organizerGroup.getId();
        Exception exception = mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Long organizerId = organizerGroup.getId();
        Exception exception = mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Group not found.");
    }

    @Test
    public void deleteMainGroup() throws Exception {
        userGroup.setUsers(new HashSet<>());
        groupRepository.save(userGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "name", userGroup.getName(),
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", organizerGroup.getId().toString(),
                "The group must be empty (without users) before it is deleted.");
    }

    @Test
    public void deleteGroupsUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), designerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteGroupsGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), designerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
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
        userGroup.setUsers(new HashSet<>());
        groupRepository.save(userGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), userGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertTrue(groupRepository.existsById(userGroup.getId()));
        assertTrue(groupRepository.existsById(organizerGroupId));
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "name", userGroup.getName(),
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
        assertTrue(groupRepository.existsById(userGroup.getId()));
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
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
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroupsGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<GroupDTO> responseGroupDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<GroupDTO>>() {}).getContent();
        assertTrue(responseGroupDTOs.contains(convertToGroupDTO(userGroup)));
        assertTrue(responseGroupDTOs.contains(convertToGroupDTO(userGroup)));
        assertTrue(responseGroupDTOs.contains(convertToGroupDTO(organizerGroup)));
    }

    @Test
    public void getGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), beanMapping.mapTo(userGroup, GroupDTO.class));
    }

    @Test
    public void getGroupNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Group not found.");
    }

    @Test
    public void getRolesOfGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRolesOfGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRolesOfGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> responseRoles = convertJsonBytesToObject(response.getContentAsString(), new TypeReference<PageResultResource<RoleDTO>>() {});
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Group not found.");;
    }

    @Test
    public void assignRoleToGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), organizerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void assignRoleToGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), organizerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Group not found.");
    }

    @Test
    public void removeRoleToGroupUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void removeRoleToGroupGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
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

        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "id", "100",
                "Group not found.");
    }

    @Test
    public void removeMainRoleFromMainGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", userGroup.getId(), userRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "roleType", userRole.getRoleType(),
                "Main role of the group cannot be removed.");
    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        assertEquals(value, entityErrorDetail.getIdentifierValue().toString());
        assertEquals(reason, entityErrorDetail.getReason());
    }

}

