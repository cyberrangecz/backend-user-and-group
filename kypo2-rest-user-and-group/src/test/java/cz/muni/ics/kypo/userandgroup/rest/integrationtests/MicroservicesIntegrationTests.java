package cz.muni.ics.kypo.userandgroup.rest.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.rest.controllers.MicroservicesRestController;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.DBTestUtil;
import cz.muni.ics.kypo.userandgroup.rest.integrationtests.config.RestConfigTest;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
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
@ContextConfiguration(classes = {TestDataFactory.class, MicroservicesRestController.class})
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
    @Autowired
    private TestDataFactory testDataFactory;

    private NewMicroserviceDTO newMicroserviceDTO;
    private Microservice microserviceUserAndGroup;
    private Role adminRole, guestRole;
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

        defaultGroup = testDataFactory.getUAGDefaultGroup();
        guestRole = defaultGroup.getRoles().iterator().next();

        adminRole = testDataFactory.getUAGAdminRole();
        microserviceUserAndGroup = adminRole.getMicroservice();

        guestRole.setMicroservice(microserviceUserAndGroup);
        microserviceRepository.save(microserviceUserAndGroup);
        roleRepository.saveAll(Set.of(adminRole, guestRole));

        roleAdminDTO = testDataFactory.getTrainingAdminRoleForNewMicroserviceDTO();
        roleOrganizerDTO = testDataFactory.getTrainingOrganizerRoleForNewMicroserviceDTO();
        roleDesignerDTO = testDataFactory.getTrainingDesignerRoleForNewMicroserviceDTO();

        roleTraineeDTO = testDataFactory.getTrainingTraineeRoleForNewMicroserviceDTO();
        roleTraineeDTO.setDefault(true);
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
        IDMGroup defaultGroup = groupRepository.findByName("DEFAULT-GROUP").orElseGet(null);
        assertNotNull(defaultGroup);
        assertTrue(defaultGroup.getRoles().stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_TRAINEE")));

        Set<Role> roles = roleRepository.getAllRolesByMicroserviceName("kypo2-training");
        assertEquals(4, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_ADMIN")));
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

        assertTrue(getInitialExceptionMessage(exception).contains("Role with given role type: " + roleTraineeDTO.getRoleType() + " already exist. Please name the role with different role type.") &&
                getInitialExceptionMessage(exception).contains("Please name the role with different role type."));
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
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName(newMicroserviceDTO.getName()).isPresent());
        assertTrue(roleRepository.existsByRoleType(roleDesignerDTO.getRoleType()));
        assertTrue(roleRepository.existsByRoleType(roleTraineeDTO.getRoleType()));
    }

    @Test
    public void registerNewMicroserviceWithMultipleDefaultRoles() throws Exception {
        roleDesignerDTO.setDefault(true);
        Exception exception = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());
        assertEquals("Microservice which you are trying to register cannot have more than 1 default role.", getInitialExceptionMessage(exception));
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }

}

