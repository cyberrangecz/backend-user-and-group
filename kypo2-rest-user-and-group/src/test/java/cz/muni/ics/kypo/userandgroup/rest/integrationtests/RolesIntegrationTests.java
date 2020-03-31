package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
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
import cz.muni.ics.kypo.userandgroup.rest.controllers.RolesRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiEntityError;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
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
import java.util.Optional;
import java.util.Set;

import static cz.muni.ics.kypo.userandgroup.rest.util.ObjectConverter.*;
import static cz.muni.ics.kypo.userandgroup.rest.util.TestAuthorityGranter.mockSpringSecurityContextForGet;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class, RolesRestController.class})
@DataJpaTest
@Import(RestConfigTest.class)
@EnableSpringDataWebSupport
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RolesIntegrationTests {

    private TestRestTemplate restTemplate = new TestRestTemplate();
    private MockMvc mvc;
    private BeanMapping beanMapping;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RolesRestController rolesRestController;
    @Autowired
    private MicroserviceRepository microserviceRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private IDMGroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TestDataFactory testDataFactory;

    private Role roleAdmin, roleGuest, roleDesigner, roleOrganizer, roleTrainee, roleUser;
    private IDMGroup group2;
    private User user1, user2, user3, user4;

    @SpringBootApplication
    static class TestConfiguration {

    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(rolesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();

        beanMapping = new BeanMappingImpl(new ModelMapper());

        Microservice microserviceUserAndGroup = testDataFactory.getKypoUaGMicroservice();
        Microservice microserviceTraining = testDataFactory.getKypoTrainingMicroservice();
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

        IDMGroup group1 = testDataFactory.getTrainingOrganizerGroup();
        group1.setRoles(new HashSet<>(Set.of(roleOrganizer)));

        group2 = testDataFactory.getUAGDefaultGroup();
        group2.setName("DEFAULT-GROUP");
        group2.setRoles(new HashSet<>(Set.of(roleOrganizer, roleTrainee, roleGuest)));
        groupRepository.saveAll(Set.of(group1, group2));

        user1 = testDataFactory.getUser1();
        user2 = testDataFactory.getUser2();
        user3 = testDataFactory.getUser3();
        user4 = testDataFactory.getUser4();
        userRepository.saveAll(new HashSet<>(Set.of(user1, user2, user3, user4)));

        group1.addUser(user4);
        group2.setUsers(new HashSet<>(Set.of(user1, user2, user3)));
        groupRepository.saveAll(new HashSet<>(Set.of(group1, group2)));

        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);
    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "role");
    }

    @Test
    public void getRoles() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles = objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>(){});
        assertTrue(roles.getContent().contains(convertToRoleDTO(roleAdmin)));
        assertTrue(roles.getContent().contains(convertToRoleDTO(roleGuest)));
        assertTrue(roles.getContent().contains(convertToRoleDTO(roleUser)));
        assertTrue(roles.getContent().contains(convertToRoleDTO(roleDesigner)));
        assertTrue(roles.getContent().contains(convertToRoleDTO(roleOrganizer)));
        assertTrue(roles.getContent().contains(convertToRoleDTO(roleTrainee)));
        assertEquals(6, roles.getPagination().getTotalElements());
        assertEquals(20, roles.getPagination().getSize());
    }

    @Test
    public void getRolesPagination() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletResponse response1 = mvc.perform(get("/roles/?page=0&size={size}", 4)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles1 = objectMapper.readValue(convertJsonBytesToObject(response1.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>(){});
        assertEquals(4,roles1.getPagination().getNumberOfElements());
        assertEquals(6, roles1.getPagination().getTotalElements());

        MockHttpServletResponse response2 = mvc.perform(get("/roles/?page={page}&size={size}", 1, 4)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles2 = objectMapper.readValue(convertJsonBytesToObject(response2.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>(){});
        assertEquals(2,roles2.getPagination().getNumberOfElements());
        assertEquals(1, roles2.getPagination().getNumber());
        assertEquals(2, roles2.getPagination().getTotalPages());
    }

    @Test
    public void getRolesWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRolesWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{id}", roleDesigner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertToRoleDTO(roleDesigner)), response.getContentAsString());
    }

    @Test
    public void getRoleNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "id", 100, "Entity Role (id: 100) not found.");
    }

    @Test
    public void getRoleWithUserRole() throws Exception{
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/roles/{id}", roleGuest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRoleWithGuestRole() throws Exception{
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        Exception exception = mvc.perform(get("/roles/{id}", roleGuest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGivenRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{roleId}/users", roleOrganizer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user1, Set.of(roleOrganizer, roleTrainee, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user2, Set.of(roleOrganizer, roleTrainee, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, Set.of(roleOrganizer)))));
    }

    @Test
    public void getUsersWithGivenRoleNotFoundRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{roleId}/users", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "roleId", 100, "Entity Role (roleId: 100) not found.");
    }

    @Test
    public void getUsersWithGivenRoleWithUserRole() throws Exception{
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/roles/{roleId}/users", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGivenRoleWithGuestRole() throws Exception{
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        Exception exception = mvc.perform(get("/roles/{roleId}/users", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGivenRoleType() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/users")
                .param("roleType", roleOrganizer.getRoleType())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString())
                .contains(convertObjectToJsonBytes(convertToUserDTO(user4, Set.of(roleOrganizer)))));
    }

    @Test
    public void getUsersWithGivenRoleTypeWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        MockHttpServletResponse response = mvc.perform(get("/roles/users")
                .param("roleType", roleOrganizer.getRoleType())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString())
                .contains(convertObjectToJsonBytes(convertToUserDTO(user4, Set.of(roleOrganizer)))));
    }

    @Test
    public void getUsersWithGivenRoleTypeWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        MockHttpServletResponse response = mvc.perform(get("/roles/users")
                .param("roleType", roleOrganizer.getRoleType())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, Set.of(roleOrganizer)))));
    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIds() throws Exception{
        MockHttpServletResponse response = mvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", roleTrainee.getRoleType())
                .param("ids", user3.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<UserDTO> users =
                objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>(){});

        assertTrue(users.getContent().contains(convertToUserDTO(user1, group2.getRoles())));
        assertTrue(users.getContent().contains(convertToUserDTO(user2, group2.getRoles())));
        assertFalse(users.getContent().contains(convertToUserDTO(user3, group2.getRoles())));
        assertEquals(2, users.getPagination().getTotalElements());
        assertEquals(20, users.getPagination().getSize());
    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIdsWithUserRole() throws Exception{
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_USER);
        MockHttpServletResponse response = mvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", roleTrainee.getRoleType())
                .param("ids", user3.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<UserDTO> users =
                objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>(){});

        assertTrue(users.getContent().contains(convertToUserDTO(user1, group2.getRoles())));
        assertTrue(users.getContent().contains(convertToUserDTO(user2, group2.getRoles())));
        assertFalse(users.getContent().contains(convertToUserDTO(user3, group2.getRoles())));
        assertEquals(2, users.getPagination().getTotalElements());
        assertEquals(20, users.getPagination().getSize());
    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIdsWithGuestRole() throws Exception{
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_GUEST);
        MockHttpServletResponse response = mvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", roleTrainee.getRoleType())
                .param("ids", user3.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<UserDTO> users =
                objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>(){});

        assertTrue(users.getContent().contains(convertToUserDTO(user1, group2.getRoles())));
        assertTrue(users.getContent().contains(convertToUserDTO(user2, group2.getRoles())));
        assertFalse(users.getContent().contains(convertToUserDTO(user3, group2.getRoles())));
        assertEquals(2, users.getPagination().getTotalElements());
        assertEquals(20, users.getPagination().getSize());
    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        assertEquals(value, entityErrorDetail.getIdentifierValue());
        assertEquals(reason, entityErrorDetail.getReason());
    }

}
