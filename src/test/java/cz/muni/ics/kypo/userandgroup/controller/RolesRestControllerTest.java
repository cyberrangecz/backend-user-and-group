package cz.muni.ics.kypo.userandgroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserBasicViewDto;
import cz.muni.ics.kypo.userandgroup.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.exceptions.errors.ApiEntityError;
import cz.muni.ics.kypo.userandgroup.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.handler.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static cz.muni.ics.kypo.userandgroup.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.userandgroup.util.ObjectConverter.convertObjectToJsonBytes;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestDataFactory.class)
public class RolesRestControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;
    private RolesRestController roleRestController;
    @Mock
    private RoleFacade roleFacade;
    @Mock
    private UserFacade userFacade;
    @Mock
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private AutoCloseable closeable;
    private RoleDTO adminRoleDTO, userRoleDTO;
    private UserDTO userDTO1, userDTO2;
    private UserBasicViewDto userBasicViewDto1, userBasicViewDto2;
    private PageResultResource<RoleDTO> rolePageResultResource;

    @BeforeEach
    public void setup() throws RuntimeException {
        closeable = MockitoAnnotations.openMocks(this);
        roleRestController = new RolesRestController(roleFacade, userFacade, objectMapper);

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

        userBasicViewDto1 = testDataFactory.getUserBasicViewDto1();
        userBasicViewDto1.setId(1L);

        userBasicViewDto2 = testDataFactory.getUserBasicViewDto2();
        userBasicViewDto2.setId(2L);

        rolePageResultResource = new PageResultResource<>(Arrays.asList(adminRoleDTO, userRoleDTO));

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
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
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
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
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
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
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getUsersWithGivenRoleTypeAndNotWithGivenIds() throws Exception {
        String valueTr = convertObjectToJsonBytes(new PageResultResource<>(List.of(userDTO1, userDTO2)));
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(userFacade.getUsers(any(), any(Pageable.class), anyString(), anySet()))
                .willReturn(new PageResultResource<>(List.of(userBasicViewDto1, userBasicViewDto2)));

        MockHttpServletResponse response = mockMvc.perform(get("/roles/users-not-with-ids")
                .param("roleType", adminRoleDTO.getRoleType())
                .param("ids", userDTO1.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTr), response.getContentAsString());
    }
}
