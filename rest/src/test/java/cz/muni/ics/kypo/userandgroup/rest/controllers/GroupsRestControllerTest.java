package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import cz.muni.ics.kypo.userandgroup.dbmodel.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.DTO.Source;
import cz.muni.ics.kypo.userandgroup.rest.DTO.group.AddMembersToGroupDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.group.GroupDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.BeanMapping;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class GroupsRestControllerTest  {

    @InjectMocks
    private GroupsRestController groupsRestController;

    @Mock
    private IDMGroupService groupService;

    @Mock
    private UserService userService;

    @Mock
    private BeanMapping dtoMapper;

    private MockMvc mockMvc;

    @Before
    public void setup() throws  RuntimeException {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(groupsRestController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        when(groupService.getAllIDMGroups()).thenReturn(Arrays.asList(getGroup()));
        when(groupService.getIDMGroupWithUsers(anyLong())).thenReturn(getGroup());
        when(groupService.getIDMGroupWithUsers(anyString())).thenReturn(getGroup());
        when(groupService.update(any(IDMGroup.class))).thenReturn(getGroup());
        when(groupService.create(any(IDMGroup.class))).thenReturn(getGroup());
        when(groupService.isGroupInternal(anyLong())).thenReturn(true);
        when(groupService.getIDMGroupWithUsers(anyString())).thenReturn(getGroup());
        when(groupService.get(anyLong())).thenReturn(getGroup());
        when(groupService.delete(any(IDMGroup.class))).thenReturn(GroupDeletionStatus.SUCCESS);
        when(groupService.deleteGroups(anyList())).thenReturn(ImmutableMap.of(getGroup(),GroupDeletionStatus.SUCCESS));
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(groupsRestController);
    }

    @Test
    public void testCreateGroup() throws Exception {

        mockMvc.perform(post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testCreateGroupCouldNotBeCreated() throws Exception {
        when(groupService.create(any(IDMGroup.class))).thenThrow( new IdentityManagementException());
        Exception ex = mockMvc.perform(post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Invalid group's information or could not be created in database.", ex.getMessage());
    }

    @Test
    public void testCreateGroupWithUserNotInDB() throws Exception {
        when(userService.getUserByScreenName(anyString())).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Invalid group's information or could not be created in database.", ex.getMessage());
    }

    @Test
    public void  testCreateGroupWithNullRequestBody () throws Exception {
        mockMvc.perform(post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void  testUpdateGroup () throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getGroup())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void  testUpdateExternalGroup () throws Exception {
        when(groupService.isGroupInternal(anyLong())).thenReturn(false);
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void  testUpdateGroupNotInDB () throws Exception {
        when(groupService.getIDMGroupWithUsers(anyLong())).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void  testUpdateGroupWithUpdateError () throws Exception {
        when(groupService.update(any(IDMGroup.class))).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void  testUpdateGroupWithNullNameAndDescription () throws Exception {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(groupDTO)))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group name neither group description cannot be null.", ex.getMessage());
    }

    @Test
    public void  testRemoveMembers () throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", getGroup().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void  testRemoveMembersFromGroupNotInDB () throws Exception {
        when(groupService.getIDMGroupWithUsers(anyString())).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void  testRemoveMembersWithUpdateError () throws Exception {
        when(groupService.update(any(IDMGroup.class))).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void  testRemoveMembersWithNullRequestBody () throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void  testRemoveMembersFromExternalGroup () throws Exception {
        when(groupService.isGroupInternal(anyLong())).thenReturn(false);
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void testAddMembers() throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testAddMembersToGroupNotInDB() throws Exception {
        when(groupService.getIDMGroupWithUsers(anyString())).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testAddMembersWithMemberNotInDB() throws Exception {
        when(userService.get(anyLong())).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testAddMembersWithUpdateError() throws Exception {
        when(groupService.update(any(IDMGroup.class))).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testAddMembersWithNullRequestBody() throws Exception {
        mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddMembersToExternalGroup() throws Exception {
        when(groupService.isGroupInternal(anyLong())).thenReturn(false);
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void testDeleteGroup() throws  Exception {
        mockMvc.perform(delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDeletionResponse())));
    }

    @Test
    public void testDeleteGroupNotInDB() throws  Exception {
        when(groupService.get(anyLong())).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Group with id " + getGroup().getId() + " could not be found.", ex.getMessage());
    }

    @Test
    public void testDeleteGroupWithDeleteError() throws  Exception {
        when(groupService.delete(any(IDMGroup.class))).thenThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals(ex.getMessage(), "Some error occurred during deletion of group with name " + getGroup().getName() + ". Please, try it later.");
    }

    @Test
    public void testDeleteExternalGroup() throws  Exception {
        when(groupService.delete(any(IDMGroup.class))).thenReturn(GroupDeletionStatus.EXTERNAL_VALID);
        Exception ex = mockMvc.perform(delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andReturn().getResolvedException();
        assertEquals("Group with name " + getGroup().getName() + " cannot be deleted because is from external source and is valid group.", ex.getMessage());
    }

    @Test
    public void testDeleteGroups() throws Exception {
        mockMvc.perform(delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getGroupDeletionResponse()))));

    }

    @Test
    public void testDeleteGroupsWithNoRequestBody() throws Exception {
        mockMvc.perform(delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void testGetGroups() throws Exception {
        mockMvc.perform(get(ApiEndpointsUserAndGroup.GROUPS_URL + "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getGroupDTO()))));
    }

    @Test
    public void testGetGroupsWithErrorWhileLoading() throws Exception {
        when(groupService.getAllIDMGroups()).thenThrow( new IdentityManagementException());
        Exception ex = mockMvc.perform(get(ApiEndpointsUserAndGroup.GROUPS_URL + "/"))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals("Error while loading all groups from database.", ex.getMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public AddMembersToGroupDTO getAddMembersToGroupDTO() {
        AddMembersToGroupDTO groupDTO = new AddMembersToGroupDTO();
        groupDTO.setGroupId(1L);
        groupDTO.setIdsOfGroupsOfImportedUsers(Arrays.asList(2L));
        groupDTO.setIdsOfUsersToBeAdd(Arrays.asList(3L));

        return groupDTO;


    }
    public GroupDTO getGroupDTO() {
        GroupDTO group = new GroupDTO();
        group.setId(1L);
        group.setDescription("Testing group 1");
        group.setName("Group 1");
        group.setSource(Source.INTERNAL);
        group.setCanBeDeleted(false);
        group.setMembers(Arrays.asList(getUserForGroupsDTO()));
        group.setRoles(Collections.EMPTY_SET);

        return group;
    }

    public NewGroupDTO getNewGroupDTO() {
        NewGroupDTO newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("New group");
        newGroupDTO.setDescription("New testing group");
        newGroupDTO.setGroupIdsOfImportedMembers(Arrays.asList(1L));
        newGroupDTO.setMembers(Arrays.asList(getUserForGroupsDTO()));

        return newGroupDTO;

    }


    public GroupDeletionResponseDTO getGroupDeletionResponse() {
        GroupDeletionResponseDTO groupDeletionResponseDTO = new GroupDeletionResponseDTO();
        groupDeletionResponseDTO.setId(1L);
        groupDeletionResponseDTO.setStatus(GroupDeletionStatus.SUCCESS);
        groupDeletionResponseDTO.setName("Group 1");
        return groupDeletionResponseDTO;

    }


    public IDMGroup getGroup() {
        IDMGroup group = new IDMGroup();
        group.setId(1L);
        group.setDescription("Testing group 1");
        group.setName("Group 1");
        group.setStatus(UserAndGroupStatus.VALID);
        group.addUser(getUser());

        return group;
    }

    public User getUser() {
        User user = new User();
        user.setId(1L);
        user.setScreenName("kypo");
        user.setFullName("KYPO LOCAL ADMIN");
        user.setMail("kypo@mail.cz");
        user.setStatus(UserAndGroupStatus.VALID);

        return user;
    }

    public UserForGroupsDTO getUserForGroupsDTO() {
        UserForGroupsDTO userDTO = new UserForGroupsDTO();
        userDTO.setId(1L);
        userDTO.setMail("kypo@mail.cz");
        userDTO.setFullName("KYPO LOCAL ADMIN");
        userDTO.setLogin("kypo");
        return userDTO;

    }
}
