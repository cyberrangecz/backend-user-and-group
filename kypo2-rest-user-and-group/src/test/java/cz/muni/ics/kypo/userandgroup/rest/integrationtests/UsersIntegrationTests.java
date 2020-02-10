package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.UsersRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

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
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user1, List.of(roleTrainee, roleOrganizer, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user2, List.of(roleTrainee, roleOrganizer, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user3, List.of()))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, List.of(roleOrganizer)))));

    }

    @Test
    public void getUsersWithGivenIds() throws Exception {
        userRepository.saveAll(Set.of(user1, user2, user3, user4));
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", Set.of(user2.getId(), user4.getId()).toString()
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertFalse(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user1, List.of(roleTrainee, roleOrganizer, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user2, List.of(roleTrainee, roleOrganizer, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, List.of(roleOrganizer)))));
    }

    @Test
    public void getUsersWithGivenIdsNotInDB() throws Exception {
        userRepository.saveAll(Set.of(user1, user2, user3, user4));
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", Set.of(100, 200).toString()
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("[]"));
    }

    @Test
    public void getUsersWithGivenIdsEmptyListOfIds() throws Exception {
        userRepository.saveAll(Set.of());
        MockHttpServletResponse response = mvc.perform(get("/users/ids")
                .param("ids", Set.of().toString()
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains("[]"));
    }

    @Test
    public void getUserInfo() throws Exception {
        mockSpringSecurityContextForGet(List.of());
        MockHttpServletResponse response = mvc.perform(get("/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertToUserDTO(user1, List.of(roleTrainee, roleOrganizer, roleGuest))), response.getContentAsString());
    }

    @Test
    public void getUsersInTwoGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/groups")
                .param("ids", convertObjectToJsonBytes(Set.of(group1.getId(), group2.getId()))
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(beanMapping.mapTo(user1, UserForGroupsDTO.class))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(beanMapping.mapTo(user2, UserForGroupsDTO.class))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(beanMapping.mapTo(user4, UserForGroupsDTO.class))));
    }

    @Test
    public void getUsersInOneGroups() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/groups")
                .param("ids", convertObjectToJsonBytes(Set.of(group1.getId()))
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(beanMapping.mapTo(user4, UserForGroupsDTO.class))));
    }

    @Test
    public void getUser() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertToUserDTO(user1, List.of(roleTrainee, roleOrganizer, roleGuest))), response.getContentAsString());
    }

    @Test
    public void getUserNotFound() throws Exception {
        Exception exception = mvc.perform(get("/users/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals("User with id 100 could not be found.", getInitialExceptionMessage(exception));

    }
    @Test
    public void getAllUsersNotInGivenGroup() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/not-in-groups/{groupId}", group2.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user3, List.of()))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, List.of(roleOrganizer)))));
    }

    @Test
    public void deleteUser() throws Exception {
        mvc.perform(delete("/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserNotFound() throws Exception {

        Exception exception = mvc.perform(delete("/users/{id}", 100)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals("User with id 100 could not be found.", getInitialExceptionMessage(exception));
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
    public void getRolesOfUser() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/users/{id}/roles", user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(roleOrganizer))));
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(roleTrainee))));
        assertTrue(response.getContentAsString().contains(convertObjectToJsonBytes(convertToRoleDTO(roleGuest))));
    }

    @Test
    public void getRolesOfUserNotFound() throws Exception {
        Exception exception = mvc.perform(get("/users/{id}/roles", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals("User with id 100 could not be found.", getInitialExceptionMessage(exception));

    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO  = beanMapping.mapTo(role, RoleDTO.class);
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        return roleDTO;
    }

    private UserDTO convertToUserDTO(User user, List<Role> roles) {
        UserDTO userDTO = beanMapping.mapTo(user, UserDTO.class);
        Set<RoleDTO> rolesDTO = new HashSet<>();
        for(Role role : roles) {
            rolesDTO.add(convertToRoleDTO(role));
        }
        userDTO.setRoles(rolesDTO);
        return userDTO;
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private static String convertJsonBytesToObject(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }

    private void mockSpringSecurityContextForGet(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), user1.getLogin());
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), user1.getFullName());
        sub.addProperty(AuthenticatedUserOIDCItems.GIVEN_NAME.getName(), user1.getGivenName());
        sub.addProperty(AuthenticatedUserOIDCItems.FAMILY_NAME.getName(), user1.getFamilyName());
        sub.addProperty(AuthenticatedUserOIDCItems.ISS.getName(), user1.getIss());
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(auth);
        given(auth.getUserAuthentication()).willReturn(auth);
        given(auth.getCredentials()).willReturn(sub);
        given(auth.getAuthorities()).willReturn(authorities);
        given(authentication.getDetails()).willReturn(auth);
    }

    private String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }
}
