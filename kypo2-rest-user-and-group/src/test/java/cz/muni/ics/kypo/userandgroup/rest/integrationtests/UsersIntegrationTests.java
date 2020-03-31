package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
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
import cz.muni.ics.kypo.userandgroup.rest.controllers.UsersRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiEntityError;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.apache.commons.lang3.StringUtils;
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
import static cz.muni.ics.kypo.userandgroup.rest.util.TestAuthorityGranter.mockSpringSecurityContextForGetUserInfo;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class, UsersRestController.class})
@DataJpaTest
@Import(RestConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsersIntegrationTests {
    private MockMvc mvc;
    private BeanMapping beanMapping;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UsersRestController usersRestController;
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

    private Microservice microserviceUserAndGroup, microserviceTraining;
    private Role roleAdmin, roleGuest, roleDesigner, roleOrganizer, roleTrainee, roleUser;
    private IDMGroup group1, group2;
    private User user1, user2, user3, user4;

    @SpringBootApplication
    static class TestConfiguration {

    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(usersRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();

        beanMapping = new BeanMappingImpl(new ModelMapper());

        microserviceUserAndGroup = testDataFactory.getKypoUaGMicroservice();
        microserviceTraining = testDataFactory.getKypoTrainingMicroservice();
        microserviceRepository.saveAll(new HashSet<>(Set.of(microserviceTraining, microserviceUserAndGroup)));

        roleAdmin = testDataFactory.getUAGAdminRole();
        roleAdmin.setMicroservice(microserviceUserAndGroup);

        roleGuest = testDataFactory.getUAGGuestRole();
        roleGuest.setMicroservice(microserviceUserAndGroup);

        roleUser = testDataFactory.getUAGUserRole();
        roleUser.setMicroservice(microserviceUserAndGroup);

        roleDesigner = testDataFactory.getTrainingDesignerRole();
        roleDesigner.setMicroservice(microserviceTraining);

        roleOrganizer = testDataFactory.getTrainingOrganizerRole();
        roleOrganizer.setMicroservice(microserviceTraining);

        roleTrainee = testDataFactory.getTrainingTraineeRole();
        roleTrainee.setMicroservice(microserviceTraining);
        roleRepository.saveAll(new HashSet<>(Set.of(roleAdmin, roleUser, roleGuest, roleDesigner, roleOrganizer, roleTrainee)));

        user1 = testDataFactory.getUser1();
        user2 = testDataFactory.getUser2();
        user3 = testDataFactory.getUser3();
        user4 = testDataFactory.getUser4();
        userRepository.saveAll(new HashSet<>(Set.of(user1, user2, user3, user4)));

        group1 = testDataFactory.getTrainingOrganizerGroup();
        group1.setRoles(new HashSet<>(Set.of(roleOrganizer)));
        group1.addUser(user4);

        group2 = testDataFactory.getUAGDefaultGroup();
        group2.setRoles(Set.of(roleOrganizer, roleTrainee, roleGuest));
        group2.setUsers(new HashSet<>(Set.of(user1, user2)));
        groupRepository.saveAll(new HashSet<>(Set.of(group1, group2)));

        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);

    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "users");
    }

    @Test
    public void getUsers() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>() {}).getContent();
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user2, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user3, Set.of())));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user4, Set.of(roleOrganizer))));

    }

    @Test
    public void getUsersWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGivenIdsWithAdminRole() throws Exception {
        userRepository.saveAll(Set.of(user1, user2, user3, user4));
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", StringUtils.join(Set.of(user2.getId(), user4.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserDTO>>() {}).getContent();
        assertFalse(responseUsersDTOs.contains(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user2, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user4, Set.of(roleOrganizer))));
    }

    @Test
    public void getUsersWithGivenIdsWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        userRepository.saveAll(Set.of(user1, user2, user3, user4));
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", StringUtils.join(Set.of(user2.getId(), user4.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserDTO>>() {}).getContent();
        assertFalse(responseUsersDTOs.contains(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user2, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user4, Set.of(roleOrganizer))));
    }

    @Test
    public void getUsersWithGivenIdsWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        userRepository.saveAll(Set.of(user1, user2, user3, user4));
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", StringUtils.join(Set.of(user2.getId(), user4.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserDTO>>() {}).getContent();
        assertFalse(responseUsersDTOs.contains(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user2, Set.of(roleTrainee, roleOrganizer, roleGuest))));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user4, Set.of(roleOrganizer))));
    }

    @Test
    public void getUsersWithGivenIdsNotInDB() throws Exception {
        userRepository.saveAll(Set.of(user1, user2, user3, user4));
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", StringUtils.join(Set.of(100, 200), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(0, convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserDTO>>() {}).getContent().size());
    }

    @Test
    public void getUsersWithGivenIdsEmptyListOfIds() throws Exception {
        userRepository.saveAll(Set.of());
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", StringUtils.join(Set.of(), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(0, convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserDTO>>() {}).getContent().size());
    }

    @Test
    public void getUserInfoWithAdministratorRole() throws Exception {
        mockSpringSecurityContextForGetUserInfo(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR, user1);
        MockHttpServletResponse response = mvc.perform(get("/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), UserDTO.class), convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest)));
    }

    @Test
    public void getUserInfoWithUserRole() throws Exception {
        mockSpringSecurityContextForGetUserInfo(RoleType.ROLE_USER_AND_GROUP_USER, user1);
        MockHttpServletResponse response = mvc.perform(get("/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), UserDTO.class), convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest)));
    }

    @Test
    public void getUserInfoWithGuestRole() throws Exception {
        mockSpringSecurityContextForGetUserInfo(RoleType.ROLE_USER_AND_GROUP_GUEST, user1);
        MockHttpServletResponse response = mvc.perform(get("/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertJsonBytesToObject(response.getContentAsString(), UserDTO.class), convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest)));
    }

    @Test
    public void getUsersInGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/groups")
                .param("ids", StringUtils.join(Set.of(group1.getId(), group2.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserForGroupsDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserForGroupsDTO>>() {}).getContent();
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user1, UserForGroupsDTO.class)));
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user2, UserForGroupsDTO.class)));
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user4, UserForGroupsDTO.class)));
    }

    @Test
    public void getUsersInGroupsWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/users/groups")
                .param("ids", StringUtils.join(Set.of(group1.getId(), group2.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersInGroupsWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/users/groups")
                .param("ids", StringUtils.join(Set.of(group1.getId(), group2.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }


    @Test
    public void getUsersInTwoGroupsAndNonExistGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/groups")
                .param("ids", StringUtils.join(Set.of(group1.getId(), group2.getId(), 500L), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserForGroupsDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserForGroupsDTO>>() {}).getContent();
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user1, UserForGroupsDTO.class)));
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user2, UserForGroupsDTO.class)));
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user4, UserForGroupsDTO.class)));
    }

    @Test
    public void getUsersInOneGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/groups")
                .param("ids", StringUtils.join(Set.of(group1.getId()), ","))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserForGroupsDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserForGroupsDTO>>() {}).getContent();
        assertTrue(responseUsersDTOs.contains(beanMapping.mapTo(user4, UserForGroupsDTO.class)));
    }

    @Test
    public void getUserWithAdministratorRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest)), convertJsonBytesToObject(response.getContentAsString(), UserDTO.class));
    }

    @Test
    public void getUserWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        MockHttpServletResponse response = mvc.perform(get("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest)), convertJsonBytesToObject(response.getContentAsString(), UserDTO.class));
    }

    @Test
    public void getUserWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        MockHttpServletResponse response = mvc.perform(get("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertToUserDTO(user1, Set.of(roleTrainee, roleOrganizer, roleGuest)), convertJsonBytesToObject(response.getContentAsString(), UserDTO.class));
    }

    @Test
    public void getUserNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), User.class, "id", "100", "Entity User (id: 100) not found.");

    }
    @Test
    public void getAllUsersNotInGivenGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/not-in-groups/{groupId}", group2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<UserDTO> responseUsersDTOs = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()),
                new TypeReference<PageResultResource<UserDTO>>() {}).getContent();
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user3, Set.of())));
        assertTrue(responseUsersDTOs.contains(convertToUserDTO(user4, Set.of(roleOrganizer))));
    }

    @Test
    public void getAllUsersNotInGivenGroupWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/users/not-in-groups/{groupId}", group2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getAllUsersNotInGivenGroupWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/users/not-in-groups/{groupId}", group2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteUser() throws Exception {
        Long userId = user1.getId();
        mvc.perform(delete("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    public void deleteUserWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(delete("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteUserWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(delete("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteUserNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/users/{id}", 100)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), User.class, "id", "100", "Entity User (id: 100) not found.");
    }

    @Test
    public void deleteUsers() throws Exception {
        userRepository.save(user3);

        mvc.perform(delete("/users")
                .content(convertObjectToJsonBytes(List.of(100L, user1.getId(), user3.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertFalse(userRepository.findById(user1.getId()).isPresent());
        assertFalse(userRepository.findById(user3.getId()).isPresent());
        assertFalse(userRepository.findById(100L).isPresent());
    }

    @Test
    public void deleteUsersWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(delete("/users")
                .content(convertObjectToJsonBytes(List.of(100L, user1.getId(), user3.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void deleteUsersWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(delete("/users")
                .content(convertObjectToJsonBytes(List.of(100L, user1.getId(), user3.getId())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assert exception != null;
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRolesOfUserWithAdministratorRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/{id}/roles", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> responseUsersDTOs = convertJsonBytesToObject(response.getContentAsString(),
                new TypeReference<PageResultResource<RoleDTO>>() {});
        assertTrue(responseUsersDTOs.getContent().containsAll(List.of(convertToRoleDTO(roleOrganizer), convertToRoleDTO(roleTrainee), convertToRoleDTO(roleGuest))));
    }

    @Test
    public void getRolesOfUserWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        MockHttpServletResponse response = mvc.perform(get("/users/{id}/roles", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> responseUsersDTOs = convertJsonBytesToObject(response.getContentAsString(),
                new TypeReference<PageResultResource<RoleDTO>>() {});
        assertTrue(responseUsersDTOs.getContent().containsAll(List.of(convertToRoleDTO(roleOrganizer), convertToRoleDTO(roleTrainee), convertToRoleDTO(roleGuest))));
    }

    @Test
    public void getRolesOfUserWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        MockHttpServletResponse response = mvc.perform(get("/users/{id}/roles", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> responseUsersDTOs = convertJsonBytesToObject(response.getContentAsString(),
                new TypeReference<PageResultResource<RoleDTO>>() {});
        assertTrue(responseUsersDTOs.getContent().containsAll(List.of(convertToRoleDTO(roleOrganizer), convertToRoleDTO(roleTrainee), convertToRoleDTO(roleGuest))));
    }

    @Test
    public void getRolesOfUserNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/{id}/roles", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), User.class, "id", "100", "Entity User (id: 100) not found.");

    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, String value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        assertEquals(value, entityErrorDetail.getIdentifierValue().toString());
        assertEquals(reason, entityErrorDetail.getReason());
    }
}
