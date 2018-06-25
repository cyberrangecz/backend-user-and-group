package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import cz.muni.ics.kypo.userandgroup.dbmodel.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.DTO.group.GroupForUsersDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.user.NewUserDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.user.UpdateUserDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.user.UserDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
public class UsersRestControllerTest {

    @MockBean
    private IDMGroupService groupService;

    @MockBean
    private UserService userService;

    @InjectMocks
    private UsersRestController usersRestController;

    private MockMvc mockMvc;

    @Before
    public void setup() throws  RuntimeException {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(usersRestController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        given(userService.getAllUsers()).willReturn(Arrays.asList(getUser()));
        given(userService.getUserWithGroups(anyLong())).willReturn(getUser());
        given(userService.create(any(User.class))).willReturn(getUser());
        given(userService.isUserInternal(anyLong())).willReturn(true);
        given(userService.get(anyLong())).willReturn(getUser());
        given(userService.update(any(User.class))).willReturn(getUser());
        given(userService.delete(any(User.class))).willReturn(UserDeletionStatus.SUCCESS);
        given(userService.deleteUsers(anyList())).willReturn(ImmutableMap.of(getUser(), UserDeletionStatus.SUCCESS));

        given(groupService.getIDMGroupWithUsers(anyLong())).willReturn(getGroupWithUsers());
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(usersRestController);
    }

    @Test
    public void testGetUsers() throws Exception {
        mockMvc.perform(get(ApiEndpointsUserAndGroup.USERS_URL + "/"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getUserDTO()))));
    }

    @Test
    public void testGetUsersWithErrorWhileLoading() throws Exception {
        given(userService.getAllUsers()).willThrow(new IdentityManagementException());
        Exception ex  = mockMvc.perform(get(ApiEndpointsUserAndGroup.USERS_URL + "/"))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals("Some error occurred while loading all users. Please, try it later.", ex.getMessage());
    }

    @Test
    public void testGetAllUsersNotInGivenGroup() throws Exception {
        mockMvc.perform(get(ApiEndpointsUserAndGroup.USERS_URL + "/except/in/group/{groupId}", getGroup().getId()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(convertObjectToJsonBytes(Collections.EMPTY_LIST)));
    }

    @Test
    public void testGetAllUsersNotInGivenGroupWithErrorWhileLoadingAllUsers() throws Exception {
        given(userService.getAllUsers()).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(get(ApiEndpointsUserAndGroup.USERS_URL + "/except/in/group/{groupId}", getGroup().getId()))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals("Some error occurred while loading users not in group with id: " + getGroup().getId() + ". Please, try it later.", ex.getMessage());
    }

    @Test
    public void testGetAllUsersNotInGivenGroupWithErrorWhileLoadingGroupWithUsers() throws Exception {
        given(groupService.getIDMGroupWithUsers(anyLong())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(get(ApiEndpointsUserAndGroup.USERS_URL + "/except/in/group/{groupId}", getGroup().getId()))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals("Some error occurred while loading users not in group with id: " + getGroup().getId() + ". Please, try it later.", ex.getMessage());
    }

    @Test
    public void testCreateNewUser() throws Exception {

        mockMvc.perform(post(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getNewUserDTO())))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getUserDTO())));
    }

    @Test
    public void testCreateNewUserWithNullRequestBody() throws Exception {

        mockMvc.perform(post(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateNewUserWithInvalidInformation() throws Exception {
        given(userService.create(any(User.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(post(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getNewUserDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Invalid user's information or could not be created.", ex.getMessage());
    }

    @Test
    public void testUpdateUser() throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getUpdateUserDTO())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getUserDTO())));
    }

    @Test
    public void testUpdateUserWithNullRequestBody() throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateExternalUser() throws Exception {
        given(userService.isUserInternal(anyLong())).willReturn(false);
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getUpdateUserDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("User is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void testUpdateUserWithUpdateError() throws Exception {
        given(userService.update(any(User.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getUpdateUserDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("User could not be updated", ex.getMessage());
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete(ApiEndpointsUserAndGroup.USERS_URL + "/{id}", getUser().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getUserDeletionResponseDTO())));
    }

    @Test
    public void testDeleteUserNotFound() throws Exception {
        given(userService.get(anyLong())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(delete(ApiEndpointsUserAndGroup.USERS_URL + "/{id}", getUser().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("User with id " + getUser().getId() + " could not be found.", ex.getMessage());
    }

    @Test
    public void testDeleteUserExternalValid() throws Exception {
        given(userService.delete(any(User.class))).willReturn(UserDeletionStatus.EXTERNAL_VALID);
        Exception ex = mockMvc.perform(delete(ApiEndpointsUserAndGroup.USERS_URL + "/{id}", getUser().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andReturn().getResolvedException();
        assertEquals("User with login " + getUser().getScreenName() + " cannot be deleted because is from external source and is valid user.", ex.getMessage());
    }

    @Test
    public void testDeleteUsers() throws Exception {
        mockMvc.perform(delete(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getUserDeletionResponseDTO()))));
    }

    @Test
    public void testDeleteUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(delete(ApiEndpointsUserAndGroup.USERS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testChangeAdminRole() throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.USERS_URL + "/{id}/change-admin-role", getUser().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testChangeAdminRoleWithUserNotInDB() throws Exception {
        willThrow(new IdentityManagementException()).given(userService).changeAdminRole(anyLong());
        Exception ex =  mockMvc.perform(put(ApiEndpointsUserAndGroup.USERS_URL + "/{id}/change-admin-role", getUser().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("User or role could not be found.", ex.getMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setFullName("kypo");
        user.setScreenName("KYPO LOCAL ADMIN");
        user.setMail("kypo@mail.cz");
        user.setStatus(UserAndGroupStatus.VALID);
        user.addGroup(getGroup());
        return user;
    }

    private UserDTO getUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setMail("kypo@mail.cz");
        userDTO.setLogin("KYPO LOCAL ADMIN");
        userDTO.setFullName("kypo");
        return userDTO;
    }

    private NewUserDTO getNewUserDTO() {
        NewUserDTO newUserDTO = new NewUserDTO();
        newUserDTO.setFullName("kypo");
        newUserDTO.setLogin("KYPO LOCAL ADMIN");
        newUserDTO.setMail("kypo@mail.cz");
        return newUserDTO;
    }

    private UpdateUserDTO getUpdateUserDTO() {
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setId(1L);
        updateUserDTO.setLogin("KYPO LOCAL ADMIN");
        updateUserDTO.setMail("kypo@mail.cz");
        return updateUserDTO;
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

    private IDMGroup getGroupWithUsers() {
        IDMGroup group = new IDMGroup();
        group.setId(1L);
        group.addUser(getUser());
        return group;
    }

    private UserDeletionResponseDTO getUserDeletionResponseDTO() {
        UserDeletionResponseDTO deletionResponseDTO = new UserDeletionResponseDTO();
        deletionResponseDTO.setUser(getUserDTO());
        deletionResponseDTO.setStatus(UserDeletionStatus.SUCCESS);
        return deletionResponseDTO;
    }

}

