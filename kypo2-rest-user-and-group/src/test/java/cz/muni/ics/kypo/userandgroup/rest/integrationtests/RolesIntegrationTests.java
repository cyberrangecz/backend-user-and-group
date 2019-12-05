package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.userandgroup.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.RolesRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RolesRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RolesIntegrationTests {

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

    private Microservice microserviceUserAndGroup, microserviceTraining;
    private Role roleAdmin, roleGuest, roleDesigner, roleOrganizer, roleTrainee, roleUser;
    private IDMGroup group1, group2;
    private User user1, user2, user3, user4;

    @SpringBootApplication
    static class TestConfiguration {

    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(rolesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        beanMapping = new BeanMappingImpl(new ModelMapper());

        microserviceUserAndGroup = new Microservice();
        microserviceUserAndGroup.setName("kypo2-user-and-group");
        microserviceUserAndGroup.setEndpoint("kypo2-rest-user-and-group/api/v1");

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

        user1 = new User();
        user1.setFullName("Garfield Pokorny");
        user1.setLogin("852374@muni.cz");
        user1.setMail("852374@mail.muni.cz");
        user1.setStatus(UserAndGroupStatus.VALID);
        user1.setIss("https://oidc.muni.cz/oidc/");

        user2 = new User();
        user2.setFullName("Marcel Watchman");
        user2.setLogin("632145@muni.cz");
        user2.setMail("632145@mail.muni.cz");
        user2.setStatus(UserAndGroupStatus.VALID);
        user2.setIss("https://oidc.muni.cz/oidc/");

        user3 = new User();
        user3.setFullName("Drew Coyer");
        user3.setLogin("77863@muni.cz");
        user3.setMail("77863@mail.muni.cz");
        user3.setStatus(UserAndGroupStatus.VALID);
        user3.setIss("https://oidc.muni.cz/oidc/");

        user4 = new User();
        user4.setFullName("Garret Cull");
        user4.setLogin("794254@muni.cz");
        user4.setMail("794254@mail.muni.cz");
        user4.setStatus(UserAndGroupStatus.VALID);
        user4.setIss("https://oidc.muni.cz/oidc/");

        userRepository.saveAll(new HashSet<>(Set.of(user1, user2, user3, user4)));

        group1 = new IDMGroup();
        group1.setName("Organizers group");
        group1.setDescription("Group for users");
        group1.setStatus(UserAndGroupStatus.VALID);
        group1.addRole(roleOrganizer);
        group1.addUser(user4);

        group2 = new IDMGroup();
        group2.setName("DEFAULT-GROUP");
        group2.setDescription("Group for all default roles");
        group2.setStatus(UserAndGroupStatus.VALID);
        group2.setRoles(Set.of(roleOrganizer, roleTrainee, roleGuest));
        group2.setUsers(Set.of(user1, user2));

        groupRepository.saveAll(Set.of(group1, group2));

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
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToRoleDTO(roleAdmin))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToRoleDTO(roleGuest))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToRoleDTO(roleUser))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToRoleDTO(roleDesigner))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToRoleDTO(roleOrganizer))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToRoleDTO(roleTrainee))));
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
        Exception exception = mvc.perform(get("/roles/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals("Role with given id 100 could not be found", exception.getMessage());
    }

    @Test
    public void getUsersWithGivenRole() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/roles/{roleId}/users", roleOrganizer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user1, List.of(roleOrganizer, roleTrainee, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user2, List.of(roleOrganizer, roleTrainee, roleGuest)))));
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, List.of(roleOrganizer)))));
    }

    @Test
    public void getUsersWithGivenRoleNotFoundRole() throws Exception {
        Exception exception = mvc.perform(get("/roles/{roleId}/users", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
        assertEquals("Role with id: 100 could not be found.", exception.getMessage());
    }

    @Test
    public void getUsersWithGivenRoleType() throws Exception {

        MockHttpServletResponse response = mvc.perform(get("/roles/users")
                .param("roleType", roleOrganizer.getRoleType())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertTrue(convertJsonBytesToObject(response.getContentAsString()).contains(convertObjectToJsonBytes(convertToUserDTO(user4, List.of(roleOrganizer)))));
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

}
