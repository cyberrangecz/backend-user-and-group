package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class RoleRestControllerTest {

    @InjectMocks
    private RoleRestController roleRestController;
    @Mock
    private RoleFacade roleFacade;
    @Mock
    private UserFacade userFacade;
    @MockBean
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private RoleDTO adminRoleDTO, userRoleDTO;
    private UserDTO userDTO1, userDTO2;
    private int page, size;
    private PageResultResource<RoleDTO> rolePageResultResource;

    @Before
    public void setup() throws RuntimeException {
        page = 0;
        size = 10;

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

        adminRoleDTO = new RoleDTO();
        adminRoleDTO.setId(1L);
        adminRoleDTO.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name());

        userRoleDTO = new RoleDTO();
        userRoleDTO.setId(2L);
        userRoleDTO.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.name());

        userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setRoles(Set.of(adminRoleDTO));

        userDTO2 = new UserDTO();
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
        given(roleFacade.getById(adminRoleDTO.getId())).willReturn(adminRoleDTO);
        mockMvc.perform(
                get("/roles" + "/{id}", adminRoleDTO.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(adminRoleDTO)));
        then(roleFacade).should().getById(adminRoleDTO.getId());
    }

    @Test
    public void getRoleNotFoundShouldThrowException() throws Exception {
        given(roleFacade.getById(adminRoleDTO.getId())).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                get("/roles" + "/{id}", adminRoleDTO.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Role with given id " + adminRoleDTO.getId() + " could not be found", ex.getLocalizedMessage());
        then(roleFacade).should().getById(adminRoleDTO.getId());
    }

    @Test
    public void getUsersWithGivenRole() throws Exception {
        String valueTr = convertObjectToJsonBytes(new PageResultResource<>(List.of(userDTO1, userDTO2)));
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(userFacade.getUsersWithGivenRole(anyLong(), any(Pageable.class))).willReturn(new PageResultResource<>(List.of(userDTO1, userDTO2)));

        MockHttpServletResponse result = mockMvc.perform(
                get("/roles" + "/{id}/users", adminRoleDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(valueTr), result.getContentAsString());
    }

    @Test
    public void getUsersWithGivenRoleWithUserAndGroupException() throws Exception {
        given(userFacade.getUsersWithGivenRole(anyLong(), any(Pageable.class))).willThrow(new UserAndGroupFacadeException("Role cannot be found."));

        Exception exception = mockMvc.perform(
                get("/roles" + "/{id}/users", adminRoleDTO.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
