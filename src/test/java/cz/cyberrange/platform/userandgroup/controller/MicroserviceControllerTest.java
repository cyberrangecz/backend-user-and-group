package cz.cyberrange.platform.userandgroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.userandgroup.rest.controller.MicroservicesRestController;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.rest.facade.MicroserviceFacade;
import cz.cyberrange.platform.userandgroup.rest.handler.CustomRestExceptionHandler;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Optional;
import java.util.Set;

import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertJsonBytesToObject;
import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = TestDataFactory.class)
class MicroserviceControllerTest {

    private MicroservicesRestController microservicesRestController;
    @Mock
    private MicroserviceFacade microserviceFacade;
    @Mock
    private ObjectMapper objectMapper;
    @Autowired
    private TestDataFactory testDataFactory;

    private MockMvc mockMvc;
    private AutoCloseable closeable;
    private NewMicroserviceDTO newMicroserviceDTO;
    private RoleForNewMicroserviceDTO role;

    @BeforeEach
    void setup() throws RuntimeException {
        closeable = MockitoAnnotations.openMocks(this);
        role = testDataFactory.getTrainingDesignerRoleForNewMicroserviceDTO();
        newMicroserviceDTO = testDataFactory.getNewMicroserviceDTO();
        newMicroserviceDTO.setRoles(Set.of(role));


        microservicesRestController = new MicroservicesRestController(microserviceFacade, objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(microservicesRestController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()
                        )
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler())
                .build();
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    void contextLoads() {
        assertNotNull(microservicesRestController);
    }

    @Test
    void testRegisterNewMicroservice() throws Exception {
        mockMvc.perform(
                post("/microservices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isCreated());
        then(microserviceFacade).should().registerMicroservice(any(NewMicroserviceDTO.class));
    }

    @Test
    public void testRegisterNewMicroserviceWithFacadeException() throws Exception {
        willThrow(new UnprocessableEntityException()).given(microserviceFacade).registerMicroservice(any(NewMicroserviceDTO.class));
        MockHttpServletResponse response = mockMvc.perform(post("/microservices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatus());
        assertEquals("The requested data cannot be processed.", error.getMessage());
    }

    @Test
    public void testRegisterNewMicroserviceWithNotValidRequestBody() throws Exception {
        newMicroserviceDTO.setName("");
        Exception ex = mockMvc.perform(
                MockMvcRequestBuilders.post("/microservices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(ex.getMessage().contains("Validation failed for argument"));
        assertEquals(MethodArgumentNotValidException.class, ex.getClass());
    }

}
