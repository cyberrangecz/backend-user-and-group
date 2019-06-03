package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.MicroserviceFacade;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class MicroserviceControllerTest {

    @InjectMocks
    private MicroservicesRestController microservicesRestController;
    @MockBean
    private MicroserviceFacade microserviceFacade;
    @MockBean
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private NewMicroserviceDTO newMicroserviceDTO;
    private RoleForNewMicroserviceDTO role;

    @Before
    public void setup() throws RuntimeException {
        role = new RoleForNewMicroserviceDTO();
        role.setRoleType("ROLE_TRAINING_DESIGNER");
        role.setDescription("This role will allow you ...");
        newMicroserviceDTO = new NewMicroserviceDTO();
        newMicroserviceDTO.setName("kypo2-training");
        newMicroserviceDTO.setEndpoint("http://localhost:8080/kypo2-rest-training/api/v1");

        newMicroserviceDTO.setRoles(Set.of(role));

        MockitoAnnotations.initMocks(this);

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

    @Test
    public void contextLoads() {
        assertNotNull(microservicesRestController);
    }

    @Test
    public void testRegisterNewMicroservice() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/microservices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isCreated());
        then(microserviceFacade).should().registerNewMicroservice(any(NewMicroserviceDTO.class));
    }

    @Test
    public void testRegisterNewMicroserviceWithFacadeException() throws Exception {
        willThrow(UserAndGroupFacadeException.class).given(microserviceFacade).registerNewMicroservice(any(NewMicroserviceDTO.class));
        Exception ex = mockMvc.perform(
                MockMvcRequestBuilders.post("/microservices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Microservice cannot be created in database: null", ex.getMessage());
        assertEquals(ResourceNotCreatedException.class, ex.getClass());
    }

    @Test
    public void testRegisterNewMicroserviceWithNotValidRequestBody() throws Exception {
        willThrow(UserAndGroupFacadeException.class).given(microserviceFacade).registerNewMicroservice(any(NewMicroserviceDTO.class));
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

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
