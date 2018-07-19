package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.DTO.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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

    @Mock
    private BeanMapping beanMapping;

    private MockMvc mockMvc;

    private Role adminRole, userRole;

    private RoleDTO adminRoleDTO, userRoleDTO;

    private int page, size;

    @Before
    public void setup() throws RuntimeException {
        page = 0;
        size = 10;

        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(roleRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());

        userRole = new Role();
        userRole.setId(2L);
        userRole.setRoleType(RoleType.USER.name());

        adminRoleDTO = new RoleDTO();
        adminRoleDTO.setId(1L);
        adminRoleDTO.setRoleType(RoleType.ADMINISTRATOR.name());

        userRoleDTO = new RoleDTO();
        userRoleDTO.setId(2L);
        userRoleDTO.setRoleType(RoleType.USER.name());

        given(beanMapping.mapTo(adminRole, RoleDTO.class)).willReturn(adminRoleDTO);
        given(beanMapping.mapTo(userRole, RoleDTO.class)).willReturn(userRoleDTO);
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(roleRestController);
    }

    @Test
    public void getRoles() throws Exception {
        given(roleService.getAllRoles(any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(adminRole, userRole)));
        mockMvc.perform(
                get(ApiEndpointsUserAndGroup.ROLES_URL + "/")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(adminRoleDTO, userRoleDTO))));
    }

    @Test
    public void getRole() throws Exception {
        given(roleService.getById(adminRole.getId())).willReturn(adminRole);
        mockMvc.perform(
                get(ApiEndpointsUserAndGroup.ROLES_URL + "/{id}", adminRole.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(adminRoleDTO)));
    }

    @Test
    public void getRoleNotFoundShouldThrowException() throws Exception {
        given(roleService.getById(adminRole.getId())).willThrow(IdentityManagementException.class);
        Exception ex = mockMvc.perform(
                get(ApiEndpointsUserAndGroup.ROLES_URL + "/{id}", adminRole.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Role with given id could not be found", ex.getMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
