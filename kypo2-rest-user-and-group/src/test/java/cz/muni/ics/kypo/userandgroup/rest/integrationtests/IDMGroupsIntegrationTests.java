package cz.muni.ics.kypo.userandgroup.rest.integrationtests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.AddUsersToGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.GroupsRestController;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import cz.muni.ics.kypo.userandgroup.service.impl.SecurityService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
    private SecurityService securityService;
    @Autowired
    private TestDataFactory testDataFactory;

    private NewGroupDTO newOrganizerGroupDTO;
    private UserForGroupsDTO organizerDTO1, organizerDTO2;
    private User organizer1, organizer2, user1, user2;
    private GroupDTO groupDTO;
    private IDMGroup adminGroup, userGroup, defaultGroup, organizerGroup, designerGroup;
    private Role adminRole, guestRole, userRole, designerRole, organizerRole, traineeRole;
    private Microservice microserviceUserAndGroup, microserviceTraining;

    @SpringBootApplication
    static class TestConfiguration {

    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(groupsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
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


        organizerDTO1 = testDataFactory.getUserForGroupsDTO2();
        organizerDTO2 = testDataFactory.getUserForGroupsDTO3();

        newOrganizerGroupDTO = new NewGroupDTO();
        newOrganizerGroupDTO.setName("Organizer group");
        newOrganizerGroupDTO.setDescription("Main group for organizers");

        defaultGroup.setUsers(new HashSet<>(Set.of(organizer1, organizer2, user1, user2)));
        userGroup.setUsers(new HashSet<>(Set.of(user1, user2)));
        groupRepository.save(userGroup);

    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "microservice", "idm_group", "users", "role");
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
        userRepository.saveAll(Set.of(organizer1, organizer2));
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
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), beanMapping.mapTo(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void createNewGroupWithImportedUsersFromGroups() throws Exception {
        userRepository.saveAll(Set.of(user1));

        newOrganizerGroupDTO.setGroupIdsOfImportedUsers(List.of(userGroup.getId()));
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        Optional<IDMGroup> createdGroup = groupRepository.findByName(newOrganizerGroupDTO.getName());
        assertTrue(createdGroup.isPresent());
        assertEquals(userGroup.getUsers().size(), createdGroup.get().getUsers().size());

        GroupDTO createdGroupDTO = convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class);
        assertTrue(createdGroupDTO.getUsers().containsAll(beanMapping.mapTo(createdGroup.get().getUsers(), UserForGroupsDTO.class)));
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), beanMapping.mapTo(createdGroup.get(), GroupDTO.class));
    }

    @Test
    public void updateGroupChangeNameOfMainGroup() throws Exception {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(userGroup.getId());
        updateGroupDTO.setName("Changed user group name");
        updateGroupDTO.setDescription("Description");

        Exception exception = mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertEquals("Cannot change name of main group " + userGroup.getName() + " to " + updateGroupDTO.getName() + ".", getInitialExceptionMessage(exception));
    }

    @Test
    public void updateMainGroup() throws Exception {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(userGroup.getId());
        updateGroupDTO.setName(userGroup.getName());
        updateGroupDTO.setDescription("Description of main group");

        mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateGroup() throws Exception {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(organizerGroup.getId());
        updateGroupDTO.setName("Change group name");
        updateGroupDTO.setDescription("Now group for designers.");

        mvc.perform(put("/groups")
                .content(convertObjectToJsonBytes(updateGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
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

//    @Test
//    public void removeUserNotInDBFromGroup() throws Exception {
//        Exception exception = mvc.perform(delete("/groups/{id}/users", userGroup.getId())
//                .content(convertObjectToJsonBytes(List.of(100L)))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//        assertEquals(ResourceNotFoundException.class, exception.getClass());
//        assertTrue(exception.getMessage().contains("No users with given ids were found"));
//    }

    @Test
    public void addUsersToGroup() throws Exception {
        assertEquals(2, userGroup.getUsers().size());
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));

        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(3, userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().containsAll(Set.of(user1, user2, organizer1)));
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

//    @Test
//    public void addUserNotInDBToGroup() throws Exception {
//        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
//        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(100L));
//        Exception exception = mvc.perform(put("/groups/{id}/users", userGroup.getId())
//                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//        assertEquals(ResourceNotFoundException.class, exception.getClass());
//        assertTrue(exception.getMessage().contains("No users with given ids were found"));
//    }

//    @Test
//    public void addUsersFromGroupNotInDB() throws Exception {
//        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
//        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(100L));
//        Exception exception = mvc.perform(put("/groups/{id}/users", userGroup.getId())
//                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//        assertEquals(ResourceNotFoundException.class, exception.getClass());
//        assertTrue(exception.getMessage().contains("No groups with given ids were found. Ids: " + List.of(100L)));
//    }

    @Test
    public void deleteGroup() throws Exception {
        Long organizerId = organizerGroup.getId();
        mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    @Test
    public void deleteGroupNotFoundInDB() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{id}", 100)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("IDMGroup with id " + 100 + " not found."));
    }

    @Test
    public void deleteMainGroup() throws Exception {
        userGroup.setUsers(new HashSet<>());
        groupRepository.save(userGroup);
        Exception exception = mvc.perform(delete("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("It is not possible to delete group with id: " + userGroup.getId() +
                ". This group is User and Group default group that could not be deleted."));
        assertTrue(groupRepository.existsById(userGroup.getId()));
    }

    @Test
    public void deleteGroupWithUsers() throws Exception {
        userRepository.saveAll(Set.of(user1, user2));
        organizerGroup.addUser(user1);
        groupRepository.save(organizerGroup);
        Exception exception = mvc.perform(delete("/groups/{id}", organizerGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        System.out.println(getInitialExceptionMessage(exception));
        assertTrue(getInitialExceptionMessage(exception).contains("It is not possible to delete group with id: " + organizerGroup.getId() + ". The group must be empty (without users)"));
        assertTrue(groupRepository.existsById(organizerGroup.getId()));
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
        Exception exception = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), userGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertTrue(groupRepository.existsById(userGroup.getId()));
        assertTrue(groupRepository.existsById(organizerGroupId));
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("It is not possible to delete group with id: " + userGroup.getId() + ". " +
                "This group is User and Group default group that could not be deleted."));

    }

    @Test
    public void deleteGroupsWithUsers() throws Exception {
        organizerGroup.addUser(user1);
        groupRepository.save(organizerGroup);
        Exception exception = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertTrue(groupRepository.existsById(userGroup.getId()));

        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("It is not possible to delete group with id: " + organizerGroup.getId() + ". The group must be empty (without users)"));
    }

    @Test
    public void deleteGroupsWithEmptyList() throws Exception {
        mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of()))
                .content(convertObjectToJsonBytes(List.of()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
    public void getGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), GroupDTO.class), convertToGroupDTO(userGroup));
    }

    @Test
    public void getGroupNotFound() throws Exception {
        Exception exception = mvc.perform(get("/groups/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("IDMGroup with id " + 100 + " not found"));
    }

    @Test
    public void getRolesOfGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}/roles", defaultGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        System.out.println(response.getContentAsString());
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(traineeRole))));
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(guestRole))));
    }

    @Test
    public void getRolesOfGroupGroupNotFound() throws Exception {
        Exception exception = mvc.perform(get("/groups/{id}/roles", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id " + 100 + " not found."));
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
    public void assignRoleToGroupRoleNotFound() throws Exception {
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Role with id: " + 100 + " could not be found."));
    }

    @Test
    public void assignRoleToGroupGroupNotFound() throws Exception {
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", 100L, designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id " + 100 + " not found."));
    }

    @Test
    public void removeRoleFromGroup() throws Exception {
        organizerGroup.addRole(designerRole);
        groupRepository.save(organizerGroup);
        mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertFalse(organizerGroup.getRoles().contains(designerRole));
        assertEquals(1, organizerGroup.getRoles().size());
    }

    @Test
    public void removeRoleFromGroupRoleNotFound() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Role with id: " + 100 + " could not be found in given group."));
    }

    @Test
    public void removeRoleFromGroupGroupNotFound() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", 100L, designerRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id " + 100 + " not found."));
    }

    @Test
    public void removeMainRoleFromMainGroup() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", userGroup.getId(), userRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Role " + userRole.getRoleType() + " cannot be removed from group. This role is main role of the group"));
    }


    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private static String convertJsonBytesToObject(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }

    private static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule( new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, objectClass);
    }

    private static <T> T convertJsonBytesToObject(String object, TypeReference<T> tTypeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule( new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, tTypeReference);
    }

    private GroupDTO convertToGroupDTO(IDMGroup group) {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(group.getId());
        groupDTO.setDescription(group.getDescription());
        groupDTO.setName(group.getName());
        if (Set.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_USER.getName()).contains(groupDTO.getName())) {
            groupDTO.setCanBeDeleted(false);
        }
        groupDTO.setRoles(group.getRoles().stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toSet()));
        groupDTO.setUsers(group.getUsers().stream()
                .map(user -> {
                    return beanMapping.mapTo(user, UserForGroupsDTO.class);
                })
                .collect(Collectors.toSet()));
        return groupDTO;
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = beanMapping.mapTo(role, RoleDTO.class);
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        return roleDTO;
    }

    private String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }
}

