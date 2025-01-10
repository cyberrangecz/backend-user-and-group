package cz.cyberrange.platform.userandgroup.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.cyberrange.platform.userandgroup.rest.controller.MicroservicesRestController;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.rest.handler.CustomRestExceptionHandler;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import cz.cyberrange.platform.userandgroup.util.TestAuthorityGranter;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertJsonBytesToObject;
import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        IntegrationTestApplication.class,
        MicroservicesRestController.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class MicroservicesIntegrationTests {

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
    private MicroserviceDTO microserviceUserAndGroupDTO, microserviceTrainingDTO;
    private Microservice microserviceUserAndGroup, microserviceTraining;
    private Role adminRole, guestRole, organizerRole, designerRole;
    private RoleForNewMicroserviceDTO roleAdminDTO, roleDesignerDTO, roleOrganizerDTO, roleTraineeDTO;
    private IDMGroup defaultGroup;

    @BeforeEach
    void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(microservicesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();

        defaultGroup = testDataFactory.getUAGDefaultGroup();
        guestRole = defaultGroup.getRoles().iterator().next();
        adminRole = testDataFactory.getUAGAdminRole();
        organizerRole = testDataFactory.getTrainingOrganizerRole();
        designerRole = testDataFactory.getTrainingDesignerRole();

        microserviceTraining = testDataFactory.getCrczpTrainingMicroservice();
        microserviceUserAndGroup = adminRole.getMicroservice();

        guestRole.setMicroservice(microserviceUserAndGroup);
        organizerRole.setMicroservice(microserviceTraining);
        designerRole.setMicroservice(microserviceTraining);
        microserviceRepository.save(microserviceUserAndGroup);
        roleRepository.saveAll(Set.of(adminRole, guestRole));

        roleAdminDTO = testDataFactory.getTrainingAdminRoleForNewMicroserviceDTO();
        roleOrganizerDTO = testDataFactory.getTrainingOrganizerRoleForNewMicroserviceDTO();
        roleDesignerDTO = testDataFactory.getTrainingDesignerRoleForNewMicroserviceDTO();

        roleTraineeDTO = testDataFactory.getTrainingTraineeRoleForNewMicroserviceDTO();
        roleTraineeDTO.setDefault(true);
        groupRepository.save(defaultGroup);

        newMicroserviceDTO = new NewMicroserviceDTO();
        newMicroserviceDTO.setName("training");
        newMicroserviceDTO.setRoles(Set.of(roleAdminDTO, roleDesignerDTO, roleOrganizerDTO, roleTraineeDTO));
        newMicroserviceDTO.setEndpoint("/training/api/v1");

        microserviceUserAndGroupDTO = testDataFactory.getMicroserviceUserAndGroupDTO();
        microserviceUserAndGroupDTO.setId(microserviceUserAndGroup.getId());
        microserviceTrainingDTO = testDataFactory.getMicroserviceTrainingDTO();

        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR);
    }

    @Test
    void registerNewMicroservice() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("training").isPresent());
        IDMGroup defaultGroup = groupRepository.findByName("DEFAULT-GROUP").orElseGet(null);
        assertNotNull(defaultGroup);
        assertTrue(defaultGroup.getRoles().stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_TRAINEE")));

        Set<Role> roles = roleRepository.getAllRolesByMicroserviceName("training");
        assertEquals(4, roles.size());
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_ADMIN")));
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_DESIGNER")));
        assertTrue(roles.stream().anyMatch(role -> role.getRoleType().equals("ROLE_TRAINING_ORGANIZER")));
    }

    @Test
    void registerNewMicroserviceWhichIsAlreadyRegistered() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("training").isPresent());

        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void registerNewMicroserviceWithChangedEndpoint() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("training").isPresent());

        newMicroserviceDTO.setEndpoint("training/api/v1");
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void registerNewMicroservicesWithSameRoles() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("training").isPresent());

        newMicroserviceDTO.setName("topology");
        newMicroserviceDTO.setEndpoint("topology/api/v1");
        MockHttpServletResponse response = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "roleType", roleTraineeDTO.getRoleType(),
                "Role already exist. Please name the role with different role type.");
    }

    @Test
    void registerNewMicroservicesWithDefaultRoleNotFound() throws Exception {
        newMicroserviceDTO.setRoles(Set.of(roleAdminDTO, roleDesignerDTO, roleTraineeDTO));
        roleTraineeDTO.setDefault(false);
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("training").isPresent());

        roleOrganizerDTO.setDefault(true);
        newMicroserviceDTO.setRoles(Set.of(roleAdminDTO, roleDesignerDTO, roleOrganizerDTO, roleTraineeDTO));
        MockHttpServletResponse response = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("Default role of microservice could not be found", error.getMessage());
        assertEntityDetailError(error.getEntityErrorDetail(), Role.class, "microserviceName", newMicroserviceDTO.getName(), "Default role of microservice could not be found");
    }

    @Test
    void registerNewMicroservicesWithDefaultGroupNotFound() throws Exception {
        groupRepository.deleteById(defaultGroup.getId());
        MockHttpServletResponse response = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("Entity IDMGroup (name: DEFAULT-GROUP) not found.", error.getMessage());
        assertEntityDetailError(error.getEntityErrorDetail(), IDMGroup.class, "name", "DEFAULT-GROUP", "Entity IDMGroup (name: DEFAULT-GROUP) not found.");
    }

    @Test
    void registerSameMicroserviceWithDifferentRoles() throws Exception {
        mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertTrue(microserviceRepository.findByName("training").isPresent());

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
        MockHttpServletResponse response = mvc.perform(post("/microservices")
                .content(convertObjectToJsonBytes(newMicroserviceDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), Microservice.class, null, null,
                "Microservice which you are trying to register cannot have more than 1 default role.");
    }

    @Test
    public void getMicroservices() throws Exception {
        microserviceRepository.save(microserviceTraining);
        roleRepository.saveAll(Set.of(organizerRole, designerRole));
        microserviceTrainingDTO.setId(microserviceTraining.getId());

        MockHttpServletResponse response = mvc.perform(get("/microservices")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        List<MicroserviceDTO> microserviceResponseDTO = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), new TypeReference<PageResultResource<MicroserviceDTO>>() {
        }).getContent();
        assertTrue(microserviceResponseDTO.containsAll(Set.of(microserviceTrainingDTO, microserviceUserAndGroupDTO)));
    }

    @Test
    public void getMicroservicesWithUserRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_POWER_USER);
        MockHttpServletResponse response = mvc.perform(get("/microservices")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals("Access is denied", error.getMessage());
    }

    @Test
    public void getMicroservicesWithGuestRole() throws Exception {
        TestAuthorityGranter.mockSpringSecurityContextForGet(RoleType.ROLE_USER_AND_GROUP_TRAINEE);
        MockHttpServletResponse response = mvc.perform(get("/microservices")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals("Access is denied", error.getMessage());
    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, String value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        assertEquals(value, entityErrorDetail.getIdentifierValue());
        assertEquals(reason, entityErrorDetail.getReason());
    }

    @SpringBootApplication
    static class TestConfiguration {

    }

}

