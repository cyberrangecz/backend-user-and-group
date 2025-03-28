package cz.cyberrange.platform.userandgroup.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.userandgroup.rest.controller.RolesRestController;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.rest.handler.CustomRestExceptionHandler;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.UserRepository;
import cz.cyberrange.platform.userandgroup.util.ObjectConverter;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertJsonBytesToObject;
import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertToRoleDTO;
import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertToUserDTO;
import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.getInitialExceptionMessage;
import static cz.cyberrange.platform.userandgroup.util.TestAuthorityGranter.mockSpringSecurityContextForGet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = { IntegrationTestApplication.class, RolesRestController.class })
@TestPropertySource("classpath:application.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class RolesIntegrationTests {

    private MockMvc mvc;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TestDataFactory testDataFactory;

    private Role roleAdmin, roleGuest, roleDesigner, roleOrganizer, roleTrainee, roleUser;
    private IDMGroup group2, group3;
    private User user1, user2, user3, user4;

    @BeforeEach
    void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(rolesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();


        Microservice microserviceUserAndGroup = testDataFactory.getCrczpUaGMicroservice();
        Microservice microserviceTraining = testDataFactory.getCrczpTrainingMicroservice();
        microserviceRepository.saveAll(new HashSet<>(Set.of(microserviceTraining, microserviceUserAndGroup)));

        roleAdmin = testDataFactory.getUAGAdminRole();
        roleAdmin.setMicroservice(microserviceUserAndGroup);
        roleGuest = testDataFactory.getUAGTraineeRole();
        roleGuest.setMicroservice(microserviceUserAndGroup);
        roleUser = testDataFactory.getUAGPowerUserRole();
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
        
        group3 = testDataFactory.getTrainingDesignerGroup();
        group3.setRoles(new HashSet<>(Set.of(roleAdmin)));
        
        groupRepository.saveAll(Set.of(group1, group2, group3));

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

    @Test
    void getRoles() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles = objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>() {
        });
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
    void getRolesPagination() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletResponse response1 = mvc.perform(get("/roles/?page=0&size={size}", 4)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles1 = objectMapper.readValue(convertJsonBytesToObject(response1.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>() {
        });
        assertEquals(4, roles1.getPagination().getNumberOfElements());
        assertEquals(6, roles1.getPagination().getTotalElements());

        MockHttpServletResponse response2 = mvc.perform(get("/roles/?page={page}&size={size}", 1, 4)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles2 = objectMapper.readValue(convertJsonBytesToObject(response2.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>() {
        });
        assertEquals(2, roles2.getPagination().getNumberOfElements());
        assertEquals(1, roles2.getPagination().getNumber());
        assertEquals(2, roles2.getPagination().getTotalPages());
    }

    @Test
    void getAllRolesNotInGivenGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/not-in-group/{groupId}", group3.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<RoleDTO> roles = objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<RoleDTO>>() {
        });
        Set<Role> adminRolesNotInGroup = new HashSet<>(testDataFactory.getUAGAdminGroup().getRoles());
        adminRolesNotInGroup.removeAll(group3.getRoles());
        
        assertTrue(adminRolesNotInGroup.stream().allMatch(role -> roles.getContent().stream().anyMatch(r -> r.getRoleType().equals(role.getRoleType()))));
        assertTrue(roles.getContent().stream().allMatch(role -> group3.getRoles().stream().noneMatch(r -> r.getRoleType().equals(role.getRoleType()))));
    }

    @Test
    void getRolesWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    void getRolesWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(get("/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    void getRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{id}", roleDesigner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertToRoleDTO(roleDesigner), convertJsonBytesToObject(response.getContentAsString(), RoleDTO.class));
    }

    @Test
    public void getRoleNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{id}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "id", -1, "Entity Role (id: -1) not found.");
    }

    @Test
    public void getRoleWithUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(get("/roles/{id}", roleGuest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getRoleWithGuestRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        Exception exception = mvc.perform(get("/roles/{id}", roleGuest.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", ObjectConverter.getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGivenRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{roleId}/users", roleOrganizer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>() {
        })
                .getContent().contains(convertToUserDTO(user1, Set.of(roleOrganizer, roleTrainee, roleGuest))));
        assertTrue(convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>() {
        })
                .getContent().contains(convertToUserDTO(user2, Set.of(roleOrganizer, roleTrainee, roleGuest))));
        assertTrue(convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>() {
        })
                .getContent().contains(convertToUserDTO(user4, Set.of(roleOrganizer))));
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
    public void getUsersWithGivenRoleWithPowerUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        Exception exception = mvc.perform(get("/roles/{roleId}/users", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();
        assertNotNull(exception);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals("Access is denied", getInitialExceptionMessage(exception));
    }

    @Test
    public void getUsersWithGivenRoleWithTraineeRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
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
        assertTrue(convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>() {})
                .getContent().contains(convertToUserDTO(user4, Set.of(roleOrganizer))));
    }

    @Test
    public void getUsersWithGivenRoleTypeWithPowerUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        MockHttpServletResponse response = mvc.perform(get("/roles/users")
                        .param("roleType", roleOrganizer.getRoleType())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<UserDTO>>() {})
                .getContent().contains(convertToUserDTO(user4, Set.of(roleOrganizer))));

    }

    @Test
    public void getUsersWithGivenRoleTypeWithTraineeRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        mvc.perform(get("/roles/users")
                        .param("roleType", roleOrganizer.getRoleType())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIds() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", roleTrainee.getRoleType())
                .param("ids", user3.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        PageResultResource<UserDTO> users = objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<>() {});

        assertTrue(users.getContent().contains(convertToUserDTO(user1, group2.getRoles())));
        assertTrue(users.getContent().contains(convertToUserDTO(user2, group2.getRoles())));
        assertFalse(users.getContent().contains(convertToUserDTO(user3, group2.getRoles())));
        assertEquals(2, users.getPagination().getTotalElements());
        assertEquals(20, users.getPagination().getSize());

    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIdsWithPowerUserRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        MockHttpServletResponse response = mvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", roleTrainee.getRoleType())
                .param("ids", user3.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        PageResultResource<UserDTO> users = objectMapper.readValue(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<>() {});

        assertTrue(users.getContent().contains(convertToUserDTO(user1, group2.getRoles())));
        assertTrue(users.getContent().contains(convertToUserDTO(user2, group2.getRoles())));
        assertFalse(users.getContent().contains(convertToUserDTO(user3, group2.getRoles())));
        assertEquals(2, users.getPagination().getTotalElements());
        assertEquals(20, users.getPagination().getSize());

    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIdsWithTraineeRole() throws Exception {
        mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        mvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", roleTrainee.getRoleType())
                .param("ids", user3.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        assertEquals(value, entityErrorDetail.getIdentifierValue());
        assertEquals(reason, entityErrorDetail.getReason());
    }

    @SpringBootApplication
    static class TestConfiguration {

    }

}
