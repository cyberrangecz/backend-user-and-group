package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.MicroserviceFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
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
        System.out.println(convertObjectToJsonBytes(newMicroserviceDTO));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/microservices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isCreated());
        then(microserviceFacade).should().registerMicroservice(any(NewMicroserviceDTO.class));
    }

    @Test
    public void testRegisterNewMicroserviceWithFacadeException() throws Exception {
        willThrow(new UserAndGroupFacadeException(new UserAndGroupServiceException(ErrorCode.RESOURCE_NOT_CREATED))).given(microserviceFacade).registerMicroservice(any(NewMicroserviceDTO.class));
        Exception ex = mockMvc.perform(
                MockMvcRequestBuilders.post("/microservices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newMicroserviceDTO)))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotCreatedException.class, ex.getClass());
    }

    @Test
    public void testRegisterNewMicroserviceWithNotValidRequestBody() throws Exception {
        willThrow(UserAndGroupFacadeException.class).given(microserviceFacade).registerMicroservice(any(NewMicroserviceDTO.class));
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
