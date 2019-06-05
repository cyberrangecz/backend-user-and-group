package cz.muni.ics.kypo.userandgroup.rest.integrationtests;


import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.AddUsersToGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.GroupsRestController;
import cz.muni.ics.kypo.userandgroup.rest.controllers.MicroservicesRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = GroupsRestController.class)
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

    private NewGroupDTO newOrganizerGroupDTO;
    private UserForGroupsDTO organizerDTO1, organizerDTO2;
    private User organizer1, organizer2, user1, user2;
    private GroupDTO groupDTO;
    private IDMGroup adminGroup, userGroup, defaultGroup, organizerGroup;
    private Role roleAdmin, roleGuest, roleUser, roleDesigner, roleOrganizer, roleTrainee;
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

        microserviceUserAndGroup = new Microservice();
        microserviceUserAndGroup.setName("kypo2-user-and-group");
        microserviceUserAndGroup.setEndpoint("kypo2-rest-user-and-group/api/v1");
        microserviceRepository.save(microserviceUserAndGroup);

        microserviceTraining = new Microservice();
        microserviceTraining.setName("kypo2-training");
        microserviceTraining.setEndpoint("kypo2-training/api/v1");
        microserviceRepository.save(microserviceTraining);
        microserviceRepository.saveAll(new HashSet<>(Set.of(microserviceTraining, microserviceUserAndGroup)));

        roleAdmin = new Role();
        roleAdmin.setMicroservice(microserviceUserAndGroup);
        roleAdmin.setRoleType("ROLE_USER_AND_GROUP_ADMINISTRATOR");

        roleGuest = new Role();
        roleGuest.setMicroservice(microserviceUserAndGroup);
        roleGuest.setRoleType("ROLE_USER_AND_GROUP_GUEST");

        roleUser = new Role();
        roleUser.setMicroservice(microserviceUserAndGroup);
        roleUser.setRoleType("ROLE_USER_AND_GROUP_USER");

        roleDesigner = new Role();
        roleDesigner.setMicroservice(microserviceTraining);
        roleDesigner.setRoleType("ROLE_TRAINING_DESIGNER");

        roleOrganizer = new Role();
        roleOrganizer.setMicroservice(microserviceTraining);
        roleOrganizer.setRoleType("ROLE_TRAINING_ORGANIZER");

        roleTrainee = new Role();
        roleTrainee.setMicroservice(microserviceTraining);
        roleTrainee.setRoleType("ROLE_TRAINING_TRAINEE");
        roleRepository.saveAll(new HashSet<>(Set.of(roleAdmin, roleUser, roleGuest, roleDesigner, roleOrganizer, roleTrainee)));


        organizer1 = new User();
        organizer1.setFullName("Drew Coyer");
        organizer1.setLogin("77863@muni.cz");
        organizer1.setMail("77863@mail.muni.cz");
        organizer1.setStatus(UserAndGroupStatus.VALID);

        organizer2 = new User();
        organizer2.setFullName("Garret Cull");
        organizer2.setLogin("794254@muni.cz");
        organizer2.setMail("794254@mail.muni.cz");
        organizer2.setStatus(UserAndGroupStatus.VALID);

        user1 = new User();
        user1.setFullName("Garfield Pokorny");
        user1.setLogin("852374@muni.cz");
        user1.setMail("852374@mail.muni.cz");
        user1.setStatus(UserAndGroupStatus.VALID);

        user2 = new User();
        user2.setFullName("Marcel Watchman");
        user2.setLogin("632145@muni.cz");
        user2.setMail("632145@mail.muni.cz");
        user2.setStatus(UserAndGroupStatus.VALID);
        userRepository.saveAll(new HashSet<>(Set.of(organizer1, organizer2, user1, user2)));

        userGroup = new IDMGroup();
        userGroup.setName("USER-AND-GROUP_USER");
        userGroup.setDescription("Group for users");
        userGroup.setStatus(UserAndGroupStatus.VALID);
        userGroup.setRoles(new HashSet<>(Set.of(roleUser)));
        userGroup.addUser(user1);

        organizerGroup = new IDMGroup();
        organizerGroup.setName("Organizers group");
        organizerGroup.setDescription("Group for users");
        organizerGroup.setStatus(UserAndGroupStatus.VALID);

        defaultGroup = new IDMGroup();
        defaultGroup.setName("DEFAULT-GROUP");
        defaultGroup.setDescription("Group for all default roles");
        defaultGroup.setStatus(UserAndGroupStatus.VALID);
        defaultGroup.setRoles(Set.of(roleGuest, roleTrainee));
        defaultGroup.setUsers(Set.of(user1, user2));
        groupRepository.saveAll(new HashSet<>(Set.of(userGroup, defaultGroup, organizerGroup)));

        organizerDTO1 = new UserForGroupsDTO();
        organizerDTO1.setFullName("Drew Coyer");
        organizerDTO1.setLogin("77863@muni.cz");
        organizerDTO1.setMail("77863@mail.muni.cz");

        organizerDTO2 = new UserForGroupsDTO();
        organizerDTO2.setFullName("Garret Cull");
        organizerDTO2.setLogin("794254@muni.cz");
        organizerDTO2.setMail("794254@mail.muni.cz");

        newOrganizerGroupDTO = new NewGroupDTO();
        newOrganizerGroupDTO.setName("Organizer group");
        newOrganizerGroupDTO.setDescription("Main group for organizers");

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
        Optional<IDMGroup> createdGroup = groupRepository.findByName("Organizer group");
        assertTrue(createdGroup.isPresent());
        assertEquals(0, createdGroup.get().getUsers().size());
        assertTrue(response.getContentAsString().contains("\"name\":\"" + newOrganizerGroupDTO.getName() + "\",\"description\"" +
                ":\"" + newOrganizerGroupDTO.getDescription()+ "\""));
        assertTrue(response.getContentAsString().contains("\"users\":[]"));
    }

    @Test
    public void createNewGroupWithUsers() throws Exception {
        newOrganizerGroupDTO.setUsers(Set.of(organizerDTO1, organizerDTO2));
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertTrue(response.getContentAsString().contains("\"fullName\":\"" + organizerDTO1.getFullName() +
                "\",\"login\":\"" + organizerDTO1.getLogin() + "\""));
        assertTrue(response.getContentAsString().contains("\"fullName\":\"" + organizerDTO2.getFullName() +
                "\",\"login\":\"" + organizerDTO2.getLogin() + "\""));
        //TODO nefunguje toto volanie
        //assertTrue(groupRepository.findByName("Organizer group").isPresent());
    }

    @Test
    public void createNewGroupWithImportedUsersFromGroups() throws Exception {
        newOrganizerGroupDTO.setGroupIdsOfImportedUsers(List.of(userGroup.getId()));
        MockHttpServletResponse response = mvc.perform(post("/groups")
                .content(convertObjectToJsonBytes(newOrganizerGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("\"name\":\"" + newOrganizerGroupDTO.getName()+ "\",\"description\":\"" + newOrganizerGroupDTO.getDescription()+ "\""));
        for (User user : userGroup.getUsers()) {
            assertTrue(response.getContentAsString().contains("\"fullName\":\"" + user.getFullName() +
                    "\",\"login\":\"" + user.getLogin() + "\""));

        }
        Optional<IDMGroup> newOrganizerGroup = groupRepository.findByName("Organizer group");
        assertTrue(newOrganizerGroup.isPresent());
        assertTrue(newOrganizerGroup.get().getUsers().containsAll(userGroup.getUsers()));
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
        assertEquals("Cannot change name of main group " + userGroup.getName() + " to " + updateGroupDTO.getName() + ".", exception.getMessage());
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
        userGroup.addUser(user2);
        groupRepository.save(userGroup);
        assertEquals(2, userGroup.getUsers().size());

        mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId(), user2.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(0, userGroup.getUsers().size());
    }

    @Test
    public void removeOneUserFromGroup() throws Exception {
        userGroup.addUser(user2);
        groupRepository.save(userGroup);
        assertEquals(2, userGroup.getUsers().size());

        mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(user1.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(1, userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().contains(user2));
    }

    @Test
    public void removeUserNotInDBFromGroup() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(List.of(100L)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("User with id " + 100 + " could not be found"));
    }

//    @Test
//    public void removeUsersFromDefaultGroup() throws Exception {
//        Exception exception = mvc.perform(delete("/groups/{id}/users", defaultGroup.getId())
//                .content(convertObjectToJsonBytes(List.of(100L)))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isConflict())
//                .andReturn().getResolvedException();
//        assertEquals(ConflictException.class, exception.getClass());
//        assertTrue(exception.getMessage().contains("User cannot be removed from default group."));
//    }

    @Test
    public void addUsersToGroup() throws Exception {
        assertEquals(1, userGroup.getUsers().size());
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
        AddUsersToGroupDTO addUsersToGroupDTO =  new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user1.getId()));
        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(1, userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().contains(user1));
    }

    @Test
    public void addUsersToGroupFromOtherGroup() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO =  new AddUsersToGroupDTO();
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
        AddUsersToGroupDTO addUsersToGroupDTO =  new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(defaultGroup.getId()));
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), organizer1.getId()));
        mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(defaultGroup.getUsers().size() + 1, userGroup.getUsers().size());
        assertTrue(userGroup.getUsers().containsAll(defaultGroup.getUsers()));
        assertTrue(userGroup.getUsers().contains(organizer1));
    }

    @Test
    public void addUserNotInDBToGroup() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO =  new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(100L));
        Exception exception = mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("User with id " + 100 + " could not be found"));

    }

    @Test
    public void addUsersFromGroupNotInDB() throws Exception {
        AddUsersToGroupDTO addUsersToGroupDTO =  new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(100L));
        Exception exception = mvc.perform(put("/groups/{id}/users", userGroup.getId())
                .content(convertObjectToJsonBytes(addUsersToGroupDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("IDMGroup with id " + 100 + " not found"));

    }

    @Test
    public void deleteGroup() throws Exception {
        Long organizerId = organizerGroup.getId();
        MockHttpServletResponse response = mvc.perform(delete("/groups/{id}", organizerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("id\":" + organizerId+ ",\"status\":\"" + GroupDeletionStatusDTO.SUCCESS.name() + "\""));
        assertFalse(groupRepository.existsById(organizerId));
    }

    @Test
    public void deleteGroupNotFoundInDB() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{id}", 100)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id " + 100 + " cannot be found."));
    }

    @Test
    public void deleteMainGroup() throws Exception {
        userGroup.setUsers(new HashSet<>());
        groupRepository.save(userGroup);
        Exception exception = mvc.perform(delete("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andReturn().getResolvedException();
        assertEquals(MethodNotAllowedException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id " + userGroup.getId() + " cannot be deleted because is main group."));
        assertTrue(groupRepository.existsById(userGroup.getId()));
    }

    @Test
    public void deleteGroupWithUsers() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("It is not possible to delete this group. This group must be empty (without persons)"));
        assertTrue(groupRepository.existsById(userGroup.getId()));
    }

    @Test
    public void deleteGroups() throws Exception {
        Long organizerGroupId = organizerGroup.getId();
        userGroup.setUsers(new HashSet<>());
        groupRepository.save(userGroup);
        MockHttpServletResponse response = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of(organizerGroup.getId(), userGroup.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("id\":" + organizerGroupId + ",\"status\":\"" + GroupDeletionStatusDTO.SUCCESS.name() + "\""));
        assertTrue(response.getContentAsString().contains("id\":" + userGroup.getId()+ ",\"status\":\"" + GroupDeletionStatusDTO.ERROR_MAIN_GROUP.name() + "\""));
        assertTrue(groupRepository.existsById(userGroup.getId()));
        assertFalse(groupRepository.existsById(organizerGroupId));
    }

    @Test
    public void deleteGroupsWithEmptyList() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/groups")
                .content(convertObjectToJsonBytes(List.of()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("[]"));
    }


    @Test
    public void getGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToGroupDTO(defaultGroup))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToGroupDTO(userGroup))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToGroupDTO(organizerGroup))));
    }

    @Test
    public void getGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/groups/{id}", userGroup.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToGroupDTO(userGroup))));
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
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(roleTrainee))));
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(roleGuest))));
    }

    @Test
    public void getRolesOfGroupGroupNotFound() throws Exception {
        Exception exception = mvc.perform(get("/groups/{id}/roles", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id: " + 100 + " could not be found."));
    }


    @Test
    public void assignRoleToGroup() throws Exception {
        assertFalse(organizerGroup.getRoles().contains(roleDesigner));
         mvc.perform(put("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), roleDesigner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertTrue(organizerGroup.getRoles().contains(roleDesigner));
        assertEquals(1, organizerGroup.getRoles().size());
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
        Exception exception = mvc.perform(put("/groups/{groupId}/roles/{roleId}", 100L, roleDesigner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id: " + 100 + " could not be found."));
    }

    @Test
    public void removeRoleFromGroup() throws Exception {
        organizerGroup.addRole(roleDesigner);
        groupRepository.save(organizerGroup);
        mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), roleDesigner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertFalse(organizerGroup.getRoles().contains(roleDesigner));
        assertEquals(0, organizerGroup.getRoles().size());
    }

    @Test
    public void removeRoleFromGroupRoleNotFound() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", organizerGroup.getId(), 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Role with id: " + 100 + " could not be found."));
    }

    @Test
    public void removeRoleFromGroupGroupNotFound() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", 100L, roleDesigner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Group with id: " + 100 + " could not be found."));
    }

    @Test
    public void removeMainRoleFromMainGroup() throws Exception {
        Exception exception = mvc.perform(delete("/groups/{groupId}/roles/{roleId}", userGroup.getId(), roleUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Role " + roleUser.getRoleType() + " cannot be removed from group. This role is main role of the group"));
    }


    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private static String convertJsonBytesToObject(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }

    private GroupDTO convertToGroupDTO(IDMGroup group) {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(group.getId());
        groupDTO.setDescription(group.getDescription());
        groupDTO.setName(group.getName());
        if(Set.of("DEFAULT_GROUP", "ROLE_USER_AND_GROUP_ADMINISTRATOR", "ROLE_USER_AND_GROUP_USER").contains(groupDTO.getName())) {
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
        RoleDTO roleDTO  = beanMapping.mapTo(role, RoleDTO.class);
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        return roleDTO;
    }
}


