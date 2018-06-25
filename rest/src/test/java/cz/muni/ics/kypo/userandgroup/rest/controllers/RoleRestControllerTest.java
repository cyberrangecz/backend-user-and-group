package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.DTO.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class RoleRestControllerTest {

    @InjectMocks
    private RoleRestController roleRestController;

    @Mock
    private RoleService roleService;

    private MockMvc mockMvc;

    private Role adminRole, userRole;

    private RoleDTO adminRoleDTO, userRoleDTO;

    @Before
    public void setup() throws  RuntimeException {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(roleRestController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ADMINISTRATOR);

        userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleType(RoleType.USER);

        adminRoleDTO = new RoleDTO();
        adminRoleDTO.setId(1L);
        adminRoleDTO.setRoleType(RoleType.ADMINISTRATOR);

        userRoleDTO = new RoleDTO();
        userRoleDTO.setId(2L);
        userRoleDTO.setRoleType(RoleType.USER);
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(roleRestController);
    }

    @Test
    public void getRoles() throws Exception {
        given(roleService.getAllRoles()).willReturn(Arrays.asList(adminRole, userRole));
        mockMvc.perform(get(ApiEndpointsUserAndGroup.ROLES_URL + "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(adminRoleDTO, userRoleDTO))));
    }

    @Test
    public void getRole() throws Exception {
        given(roleService.getById(adminRole.getId())).willReturn(adminRole);
        mockMvc.perform(get(ApiEndpointsUserAndGroup.ROLES_URL + "/{id}", adminRole.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(adminRoleDTO)));
    }

    @Test
    public void getRoleNotFoundShouldThrowException() throws Exception {
        given(roleService.getById(adminRole.getId())).willThrow(IdentityManagementException.class);
        Exception ex = mockMvc.perform(get(ApiEndpointsUserAndGroup.ROLES_URL + "/{id}", adminRole.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Role with given id could not be found", ex.getMessage());
    }

    @Test
    public void createNewRole() throws Exception {
        Role newRole = new Role();
        newRole.setRoleType(RoleType.ADMINISTRATOR);
        RoleDTO newRoleDTO = new RoleDTO();
        newRoleDTO.setRoleType(RoleType.ADMINISTRATOR);

        given(roleService.create(newRole)).willReturn(adminRole);
        mockMvc.perform(post(ApiEndpointsUserAndGroup.ROLES_URL + "/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(newRoleDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(adminRoleDTO)));
    }

    @Test
    public void createNewRoleWithNullBodyShouldThrowException() throws Exception {
        mockMvc.perform(post(ApiEndpointsUserAndGroup.ROLES_URL + "/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createNewRoleWithNullRoleTypeShouldThrowException() throws Exception {
        given(roleService.create(any(Role.class))).willThrow(IdentityManagementException.class);
        Exception ex = mockMvc.perform(post(ApiEndpointsUserAndGroup.ROLES_URL + "/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(new RoleDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Invalid role's information or could not be created.", ex.getMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
