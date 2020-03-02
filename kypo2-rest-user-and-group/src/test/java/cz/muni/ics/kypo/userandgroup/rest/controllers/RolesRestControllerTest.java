package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static cz.muni.ics.kypo.userandgroup.rest.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.userandgroup.rest.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestDataFactory.class)
public class RolesRestControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @InjectMocks
    private RolesRestController roleRestController;
    @Mock
    private RoleFacade roleFacade;
    @Mock
    private UserFacade userFacade;
    @MockBean
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private RoleDTO adminRoleDTO, userRoleDTO;
    private UserDTO userDTO1, userDTO2;
    private PageResultResource<RoleDTO> rolePageResultResource;

    @Before
    public void setup() throws RuntimeException {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(roleRestController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()
                        )
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        adminRoleDTO = testDataFactory.getuAGAdminRoleDTO();
        adminRoleDTO.setId(1L);

        userRoleDTO = testDataFactory.getUAGUserRoleDTO();
        userRoleDTO.setId(2L);

        userDTO1 = testDataFactory.getUser1DTO();
        userDTO1.setId(1L);
        userDTO1.setRoles(Set.of(adminRoleDTO));

        userDTO2 = testDataFactory.getUser2DTO();
        userDTO2.setId(2L);
        userDTO2.setRoles(Set.of(userRoleDTO, adminRoleDTO));

        rolePageResultResource = new PageResultResource<>(Arrays.asList(adminRoleDTO, userRoleDTO));

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(roleRestController);
    }

    @Test
    public void getRoles() throws Exception {
        String valueAs = convertObjectToJsonBytes(rolePageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
        given(roleFacade.getAllRoles(any(Predicate.class), any(Pageable.class))).willReturn(rolePageResultResource);

        MockHttpServletResponse result = mockMvc.perform(
                get("/roles" + "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(rolePageResultResource)), result.getContentAsString());
        then(roleFacade).should().getAllRoles(any(Predicate.class), any(Pageable.class));
    }

    @Test
    public void getRole() throws Exception {
        given(roleFacade.getRoleById(adminRoleDTO.getId())).willReturn(adminRoleDTO);
        mockMvc.perform(
                get("/roles" + "/{id}", adminRoleDTO.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(adminRoleDTO)));
        then(roleFacade).should().getRoleById(adminRoleDTO.getId());
    }

    @Test
    public void getRoleNotFoundShouldThrowException() throws Exception {
        given(roleFacade.getRoleById(adminRoleDTO.getId())).willThrow(new EntityNotFoundException());
        MockHttpServletResponse response = mockMvc.perform(
                get("/roles" + "/{id}", adminRoleDTO.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(roleFacade).should().getRoleById(adminRoleDTO.getId());
    }

    @Test
    public void getUsersWithGivenRole() throws Exception {
        String valueTr = convertObjectToJsonBytes(new PageResultResource<>(List.of(userDTO1, userDTO2)));
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(userFacade.getUsersWithGivenRole(anyLong(), any(Predicate.class), any(Pageable.class))).willReturn(new PageResultResource<>(List.of(userDTO1, userDTO2)));

        MockHttpServletResponse result = mockMvc.perform(
                get("/roles" + "/{id}/users", adminRoleDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTr), result.getContentAsString());
    }

    @Test
    public void getUsersWithGivenRoleWithException() throws Exception {
        given(userFacade.getUsersWithGivenRole(anyLong(), any(Predicate.class), any(Pageable.class))).willThrow(new EntityNotFoundException());

        MockHttpServletResponse response = mockMvc.perform(
                get("/roles" + "/{id}/users", adminRoleDTO.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getUsersWithGivenRoleType() throws Exception {
        String valueTr = convertObjectToJsonBytes(new PageResultResource<>(List.of(userDTO1, userDTO2)));
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(userFacade.getUsersWithGivenRoleType(any(String.class), any(), any(Pageable.class)))
                .willReturn(new PageResultResource<>(List.of(userDTO1, userDTO2)));
        MockHttpServletResponse result = mockMvc.perform(
                get("/roles/users")
                        .param("roleType", adminRoleDTO.getRoleType()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTr), result.getContentAsString());
    }

    @Test
    public void getUsersWithGivenRoleTypeWithException() throws Exception {
        given(userFacade.getUsersWithGivenRoleType(anyString(), any(), any())).willThrow(new EntityNotFoundException());

        MockHttpServletResponse response = mockMvc.perform(
                get("/roles/users").param("roleType", adminRoleDTO.getRoleType()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIds() throws Exception {
        String valueTr = convertObjectToJsonBytes(new PageResultResource<>(List.of(userDTO1, userDTO2)));
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(userFacade.getUsers(any(), any(Pageable.class), anyString(), anySet()))
                .willReturn(new PageResultResource<>(List.of(userDTO1, userDTO2)));

        MockHttpServletResponse response = mockMvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", adminRoleDTO.getRoleType())
                .param("ids", userDTO1.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTr), response.getContentAsString());
    }
}
