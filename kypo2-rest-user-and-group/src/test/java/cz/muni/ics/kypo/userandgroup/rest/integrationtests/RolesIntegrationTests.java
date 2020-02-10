package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.$Gson$Preconditions;
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
@ContextConfiguration(classes = {TestDataFactory.class, RolesRestController.class})
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
        this.mvc = MockMvcBuilders.standaloneSetup(rolesRestController)
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

        group1 = testDataFactory.getTrainingOrganizerGroup();
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
        group2.setUsers(new HashSet<>(Set.of(user1, user2)));
        groupRepository.saveAll(new HashSet<>(Set.of(group1, group2)));




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
        assertEquals("Role with id 100 could not be found", getInitialExceptionMessage(exception));
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
        assertEquals("Role with id: 100 could not be found.", getInitialExceptionMessage(exception));
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

    private String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }

}
