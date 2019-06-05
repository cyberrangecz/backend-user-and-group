package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.MicroservicesRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MicroservicesRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MicroservicesIntegrationTests {

    private MockMvc mvc;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MicroservicesRestController microservicesRestController;
    @Autowired
    private MicroserviceRepository microserviceRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private IDMGroupRepository groupRepository;

    private NewMicroserviceDTO newMicroserviceDTO;
    private Microservice microserviceUserAndGroup;
    private Role roleAdmin, roleGuest;
    private RoleForNewMicroserviceDTO roleAdminDTO, roleDesignerDTO, roleOrganizerDTO, roleTraineeDTO;
    private IDMGroup defaultGroup;

    @SpringBootApplication
    static class TestConfiguration {

    }

    @Before
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(microservicesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        microserviceUserAndGroup = new Microservice();
        microserviceUserAndGroup.setName("kypo2-user-and-group");
        microserviceUserAndGroup.setEndpoint("kypo2-rest-user-and-group/api/v1");
        microserviceRepository.save(microserviceUserAndGroup);

        roleAdmin = new Role();
        roleAdmin.setMicroservice(microserviceUserAndGroup);
        roleAdmin.setRoleType("ROLE_USER_AND_GROUP_ADMINISTRATOR");

        roleGuest = new Role();
        roleGuest.setMicroservice(microserviceUserAndGroup);
        roleGuest.setRoleType("ROLE_USER_AND_GROUP_GUEST");

        roleRepository.saveAll(Set.of(roleAdmin, roleGuest));

        roleAdminDTO = new RoleForNewMicroserviceDTO();
        roleAdminDTO.setRoleType("ROLE_TRAINING_ADMINISTRATOR");

        roleOrganizerDTO = new RoleForNewMicroserviceDTO();
        roleOrganizerDTO.setRoleType("ROLE_TRAINING_ORGANIZER");

        roleDesignerDTO = new RoleForNewMicroserviceDTO();
        roleDesignerDTO.setRoleType("ROLE_TRAINING_DESIGNER");

        roleTraineeDTO = new RoleForNewMicroserviceDTO();
        roleTraineeDTO.setRoleType("ROLE_TRAINING_TRAINEE");
        roleTraineeDTO.setDefault(true);

        defaultGroup = new IDMGroup();
        defaultGroup.addRole(roleGuest);
        defaultGroup.setDescription("Default group for users");
        defaultGroup.setStatus(UserAndGroupStatus.VALID);
        defaultGroup.setName("DEFAULT_GROUP");
        groupRepository.save(defaultGroup);

        newMicroserviceDTO = new NewMicroserviceDTO();
        newMicroserviceDTO.setName("kypo2-training");
        newMicroserviceDTO.setRoles(Set.of(roleAdminDTO, roleDesignerDTO, roleOrganizerDTO, roleTraineeDTO));
        newMicroserviceDTO.setEndpoint("/kypo2-training/api/v1");

    }

    @After
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "microservice");
    }

    @Test
    public void registerNewMicroservice() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("kypo2-training").isPresent());
        IDMGroup defaultGroup = groupRepository.findByName("DEFAULT_GROUP").orElseGet(null);
        assertNotNull(defaultGroup);
        assertTrue(defaultGroup.getRoles().stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_TRAINEE")));

        Set<Role> roles = roleRepository.getAllRolesByMicroserviceName("kypo2-training");
        assertEquals(4, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_ADMINISTRATOR")));
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_DESIGNER")));
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_ORGANIZER")));
    }

    @Test
    public void registerNewMicroserviceWhichIsAlreadyRegistered() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("kypo2-training").isPresent());

        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void registerNewMicroserviceWithChangedEndpoint() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("kypo2-training").isPresent());

        newMicroserviceDTO.setEndpoint("kypot2-rest-training/api/v1");
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void registerNewMicroservicesWithSameRoles() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("kypo2-training").isPresent());

        newMicroserviceDTO.setName("kypo2-topology");
        newMicroserviceDTO.setEndpoint("kypot2-topology/api/v1");
        Exception exception = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertTrue(exception.getMessage().contains("Microservice cannot be created in database") &&
                exception.getMessage().contains("Please name the role with different role type."));
        assertEquals(ResourceNotCreatedException.class, exception.getClass());
    }

    @Test
    public void registerSameMicroserviceWithDifferentRoles() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("kypo2-training").isPresent());

        newMicroserviceDTO.setRoles(Set.of(roleDesignerDTO, roleTraineeDTO));
        Exception exception = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertTrue(exception.getMessage().contains("Microservice cannot be created in database") &&
                exception.getMessage().contains("Microservice which you are trying register is not same as " +
                        "microservice in DB. Change name or roles or contact administrator."));
        assertEquals(ResourceNotCreatedException.class, exception.getClass());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}

