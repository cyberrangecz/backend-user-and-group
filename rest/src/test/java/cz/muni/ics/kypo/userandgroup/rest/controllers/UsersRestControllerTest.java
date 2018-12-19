package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.NewUserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
public class UsersRestControllerTest {

    @MockBean
    private UserFacade userFacade;

    @InjectMocks
    private UsersRestController usersRestController;

    @MockBean
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private UserDTO userDTO1, userDTO2;
    private NewUserDTO newUserDTO;
    private int page, size;
    private PageResultResource<UserDTO> userPageResultResource;

    @Before
    public void setup() throws RuntimeException {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(usersRestController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()
                        )
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setLogin("user1");
        userDTO1.setFullName("User One");
        userDTO1.setMail("user.one@mail.com");

        userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setLogin("user2");
        userDTO2.setFullName("User Two");
        userDTO2.setMail("user.two@mail.com");

        newUserDTO = new NewUserDTO();
        newUserDTO.setLogin("user1");
        newUserDTO.setFullName("User One");
        newUserDTO.setMail("user.one@mail.com");

        userPageResultResource = new PageResultResource<>(Arrays.asList(userDTO1, userDTO2));

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(usersRestController);
    }

    @Test
    public void testGetUsers() throws Exception {
        String valueAs = convertObjectToJsonBytes(userPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
        given(userFacade.getUsers(any(Predicate.class), any(Pageable.class))).willReturn(userPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(
                get("/users" + "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(userPageResultResource)), result.getContentAsString());
        then(userFacade).should().getUsers(any(Predicate.class), any(Pageable.class));
    }

    @Test
    public void testGetUser() throws Exception {
        given(userFacade.getUser(userDTO1.getId())).willReturn(userDTO1);
        mockMvc.perform(
                get("/users" + "/{id}", userDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(userDTO1)));
        then(userFacade).should().getUser(userDTO1.getId());
    }

    @Test
    public void testGetUserWithUserNotFound() throws Exception {
        given(userFacade.getUser(userDTO1.getId())).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                get("/users" + "/{id}", userDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("User with id " + userDTO1.getId() + " could not be found.", ex.getLocalizedMessage());
    }

    @Test
    public void testGetAllUsersNotInGivenGroup() throws Exception {
        String valueAs = convertObjectToJsonBytes(userPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
        given(userFacade.getAllUsersNotInGivenGroup(anyLong(), any(Pageable.class))).willReturn(userPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(
                get("/users" + "/except/in/group/{groupId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(userPageResultResource)), result.getContentAsString());
        then(userFacade).should().getAllUsersNotInGivenGroup(anyLong(), any(Pageable.class));
    }

    @Test
    public void testGetAllUsersNotInGivenGroupWithError() throws Exception {
        given(userFacade.getAllUsersNotInGivenGroup(anyLong(), any(Pageable.class))).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                get("/users" + "/except/in/group/{groupId}", getGroup().getId())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals("Some error occurred while loading users not in group with id: " + getGroup().getId() + ". Please, try it later.", ex.getLocalizedMessage());
    }

    @Test
    public void testDeleteUser() throws Exception {
        given(userFacade.deleteUser(userDTO1.getId())).willReturn(getUserDeletionResponseDTO());
        mockMvc.perform(
                delete("/users" + "/{id}", userDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getUserDeletionResponseDTO())));
        then(userFacade).should().deleteUser(userDTO1.getId());
    }

    @Test
    public void testDeleteUserNotFound() throws Exception {
        given(userFacade.deleteUser(userDTO1.getId())).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                delete("/users" + "/{id}", userDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("User with id " + userDTO1.getId() + " could not be found.", ex.getLocalizedMessage());
        then(userFacade).should().deleteUser(userDTO1.getId());
    }

    @Test
    public void testDeleteUserExternalValid() throws Exception {
        UserDeletionResponseDTO userDeletionResponseDTO = getUserDeletionResponseDTO();
        userDeletionResponseDTO.setStatus(UserDeletionStatus.EXTERNAL_VALID);
        given(userFacade.deleteUser(userDTO1.getId())).willReturn(userDeletionResponseDTO);
        Exception ex = mockMvc.perform(
                delete("/users" + "/{id}", userDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andReturn().getResolvedException();
        assertEquals("User with id " + userDTO1.getId() + " cannot be deleted because is from external source and is valid user.", ex.getLocalizedMessage());
        then(userFacade).should().deleteUser(userDTO1.getId());
    }

    @Test
    public void testDeleteUsers() throws Exception {
        UserDeletionResponseDTO deletionResponseDTO = new UserDeletionResponseDTO();
        deletionResponseDTO.setUser(userDTO2);
        deletionResponseDTO.setStatus(UserDeletionStatus.EXTERNAL_VALID);
        given(userFacade.deleteUsers(Arrays.asList(userDTO1.getId(), userDTO2.getId()))).willReturn(Arrays.asList(getUserDeletionResponseDTO(), deletionResponseDTO));
        mockMvc.perform(
                delete("/users" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(userDTO1.getId(), userDTO2.getId()))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getUserDeletionResponseDTO(), deletionResponseDTO))));
        then(userFacade).should().deleteUsers(Arrays.asList(userDTO1.getId(), userDTO2.getId()));
    }

    @Test
    public void testDeleteUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                delete("/users" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(userFacade).should(never()).deleteUsers(anyList());
    }

    @Test
    public void testGetRolesOfGroup() throws Exception {
        given(userFacade.getRolesOfUser(userDTO1.getId())).willReturn(getRolesDTO());
        mockMvc.perform(
                get("/users" + "/{id}/roles", userDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getRolesDTO())));
    }

    @Test
    public void testGetRolesOfGroupWithExceptionFromFacade() throws Exception {
        given(userFacade.getRolesOfUser(userDTO1.getId())).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                get("/users" + "/{id}/roles", userDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("User with id " + userDTO1.getId() + " could not be found.", ex.getLocalizedMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private IDMGroup getGroup() {
        IDMGroup group = new IDMGroup();
        group.setId(1L);
        group.setExternalId(2L);
        group.setDescription("Testing group 1");
        group.setName("Group 1");
        group.setStatus(UserAndGroupStatus.VALID);
        return group;
    }

    private UserDeletionResponseDTO getUserDeletionResponseDTO() {
        UserDeletionResponseDTO deletionResponseDTO = new UserDeletionResponseDTO();
        deletionResponseDTO.setUser(userDTO1);
        deletionResponseDTO.setStatus(UserDeletionStatus.SUCCESS);
        return deletionResponseDTO;
    }

    private RoleDTO getAdminRoleDTO() {
        RoleDTO adminRole = new RoleDTO();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());
        return adminRole;
    }

    private RoleDTO getGuestRoleDTO() {
        RoleDTO guestRole = new RoleDTO();
        guestRole.setId(2L);
        guestRole.setRoleType(RoleType.GUEST.name());
        return guestRole;
    }

    private Set<RoleDTO> getRolesDTO() {
        return Stream.of(getAdminRoleDTO(), getGuestRoleDTO()).collect(Collectors.toSet());
    }

}

