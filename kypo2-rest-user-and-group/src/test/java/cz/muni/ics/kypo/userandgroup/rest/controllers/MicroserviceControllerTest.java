package cz.muni.ics.kypo.userandgroup.rest.controllers;

import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UnprocessableEntityException;
import cz.muni.ics.kypo.userandgroup.api.facade.MicroserviceFacade;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Optional;
import java.util.Set;

import static cz.muni.ics.kypo.userandgroup.rest.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.userandgroup.rest.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestDataFactory.class)
public class MicroserviceControllerTest {

    @InjectMocks
    private MicroservicesRestController microservicesRestController;
    @MockBean
    private MicroserviceFacade microserviceFacade;
    @Autowired
    private TestDataFactory testDataFactory;

    private MockMvc mockMvc;
    private NewMicroserviceDTO newMicroserviceDTO;
    private RoleForNewMicroserviceDTO role;

    @Before
    public void setup() throws RuntimeException {
        role = testDataFactory.getTrainingDesignerRoleForNewMicroserviceDTO();
        newMicroserviceDTO = testDataFactory.getNewMicroserviceDTO();
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
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
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
