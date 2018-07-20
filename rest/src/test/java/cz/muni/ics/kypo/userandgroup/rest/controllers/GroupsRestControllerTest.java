package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.api.dto.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.group.AddMembersToGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.NewGroupDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@EnableSpringDataWebSupport
public class GroupsRestControllerTest {

    @InjectMocks
    private GroupsRestController groupsRestController;

    @MockBean
    private IDMGroupService groupService;

    @MockBean
    private UserService userService;

    @MockBean
    private BeanMapping beanMapping;

    private MockMvc mockMvc;

    private int page, size;

    @Before
    public void setup() throws RuntimeException {
        page = 0;
        size = 10;

        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(groupsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        given(groupService.getAllIDMGroups(any(Pageable.class))).willReturn(new PageImpl<>(Arrays.asList(getGroup())));
        given(groupService.getIDMGroupWithUsers(anyLong())).willReturn(getGroup());
        given(groupService.getIDMGroupWithUsers(anyString())).willReturn(getGroup());
        given(groupService.update(any(IDMGroup.class))).willReturn(getGroup());
        given(groupService.create(any(IDMGroup.class))).willReturn(getGroup());
        given(groupService.isGroupInternal(anyLong())).willReturn(true);
        given(groupService.getIDMGroupWithUsers(anyString())).willReturn(getGroup());
        given(groupService.get(anyLong())).willReturn(getGroup());
        given(groupService.delete(any(IDMGroup.class))).willReturn(GroupDeletionStatus.SUCCESS);
        given(groupService.deleteGroups(anyList())).willReturn(ImmutableMap.of(getGroup(), GroupDeletionStatus.SUCCESS));

        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDTO.class))).willReturn(getGroupDTO());
        given(beanMapping.mapTo(any(NewGroupDTO.class), eq(IDMGroup.class))).willReturn(getGroup());
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDeletionResponseDTO.class))).willReturn(getGroupDeletionResponse());

        given(beanMapping.mapTo(any(User.class), eq(UserForGroupsDTO.class))).willReturn(getUserForGroupsDTO());

        given(beanMapping.mapTo(getAdminRole(), RoleDTO.class)).willReturn(getAdminRoleDTO());
        given(beanMapping.mapTo(getGuestRole(), RoleDTO.class)).willReturn(getGuestRoleDTO());
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(groupsRestController);
    }

    @Test
    public void testCreateGroup() throws Exception {
        mockMvc.perform(
                post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testCreateGroupCouldNotBeCreated() throws Exception {
        given(groupService.create(any(IDMGroup.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Invalid group's information or could not be created in database.", ex.getMessage());
    }

    @Test
    public void testCreateGroupWithUserNotInDB() throws Exception {
        given(userService.getUserByLogin(anyString())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Invalid group's information or could not be created in database.", ex.getMessage());
    }

    @Test
    public void testCreateGroupWithNullRequestBody() throws Exception {
        mockMvc.perform(
                post(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateGroup() throws Exception {
        mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getGroup())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testUpdateExternalGroup() throws Exception {
        given(groupService.isGroupInternal(anyLong())).willReturn(false);
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void testUpdateGroupNotInDB() throws Exception {
        given(groupService.getIDMGroupWithUsers(anyLong())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testUpdateGroupWithUpdateError() throws Exception {
        given(groupService.update(any(IDMGroup.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(getGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testUpdateGroupWithNullNameAndDescription() throws Exception {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(groupDTO)))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group name neither group description cannot be null.", ex.getMessage());
    }

    @Test
    public void testRemoveMembers() throws Exception {
        mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", getGroup().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testRemoveMembersFromGroupNotInDB() throws Exception {
        given(groupService.getIDMGroupWithUsers(anyString())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testRemoveMembersWithUpdateError() throws Exception {
        given(groupService.update(any(IDMGroup.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testRemoveMembersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRemoveMembersFromExternalGroup() throws Exception {
        given(groupService.isGroupInternal(anyLong())).willReturn(false);
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/removeMembers", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void testAddMembers() throws Exception {
        mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testAddMembersToGroupNotInDB() throws Exception {
        given(groupService.getIDMGroupWithUsers(anyString())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testAddMembersWithMemberNotInDB() throws Exception {
        given(userService.get(anyLong())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testAddMembersWithUpdateError() throws Exception {
        given(groupService.update(any(IDMGroup.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group could not be modified.", ex.getMessage());
    }

    @Test
    public void testAddMembersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddMembersToExternalGroup() throws Exception {
        given(groupService.isGroupInternal(anyLong())).willReturn(false);
        Exception ex = mockMvc.perform(
                put(ApiEndpointsUserAndGroup.GROUPS_URL + "/addMembers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddMembersToGroupDTO())))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getMessage());
    }

    @Test
    public void testDeleteGroup() throws Exception {
        mockMvc.perform(
                delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDeletionResponse())));
    }

    @Test
    public void testDeleteGroupNotInDB() throws Exception {
        given(groupService.get(anyLong())).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Group with id " + getGroup().getId() + " could not be found.", ex.getMessage());
    }

    @Test
    public void testDeleteGroupWithDeleteError() throws Exception {
        given(groupService.delete(any(IDMGroup.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals(ex.getMessage(), "Some error occurred during deletion of group with name " + getGroup().getName() + ". Please, try it later.");
    }

    @Test
    public void testDeleteExternalGroup() throws Exception {
        given(groupService.delete(any(IDMGroup.class))).willReturn(GroupDeletionStatus.EXTERNAL_VALID);
        Exception ex = mockMvc.perform(
                delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", getGroup().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andReturn().getResolvedException();
        assertEquals("Group with name " + getGroup().getName() + " cannot be deleted because is from external source and is valid group.", ex.getMessage());
    }

    @Test
    public void testDeleteGroups() throws Exception {
        mockMvc.perform(
                delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getGroupDeletionResponse()))));

    }

    @Test
    public void testDeleteGroupsWithNoRequestBody() throws Exception {
        mockMvc.perform(
                delete(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void testGetGroups() throws Exception {
        mockMvc.perform(
                get(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Arrays.asList(getGroupDTO()))));
    }

    @Test
    public void testGetGroupsWithErrorWhileLoading() throws Exception {
        given(groupService.getAllIDMGroups(any(Pageable.class))).willThrow(new IdentityManagementException());
        Exception ex = mockMvc.perform(
                get(ApiEndpointsUserAndGroup.GROUPS_URL + "/")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals("Error while loading all groups from database.", ex.getMessage());
    }

    @Test
    public void testGetGroup() throws Exception {
        IDMGroup group = getGroup();
        given(groupService.get(group.getId())).willReturn(group);
        mockMvc.perform(
                get(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}", group.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDTO())));
    }

    @Test
    public void testGetRolesOfGroup() throws Exception {
        IDMGroup g = getGroup();
        given(groupService.getRolesOfGroup(g.getId())).willReturn(getRoles());
        mockMvc.perform(
                get(ApiEndpointsUserAndGroup.GROUPS_URL + "/{id}/roles", g.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getRolesDTO())));
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private AddMembersToGroupDTO getAddMembersToGroupDTO() {
        AddMembersToGroupDTO groupDTO = new AddMembersToGroupDTO();
        groupDTO.setGroupId(1L);
        groupDTO.setIdsOfGroupsOfImportedUsers(Arrays.asList(2L));
        groupDTO.setIdsOfUsersToBeAdd(Arrays.asList(3L));

        return groupDTO;


    }

    private GroupDTO getGroupDTO() {
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

    private NewGroupDTO getNewGroupDTO() {
        NewGroupDTO newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("New group");
        newGroupDTO.setDescription("New testing group");
        newGroupDTO.setGroupIdsOfImportedMembers(Arrays.asList(1L));
        newGroupDTO.setMembers(Arrays.asList(getUserForGroupsDTO()));

        return newGroupDTO;

    }


    private GroupDeletionResponseDTO getGroupDeletionResponse() {
        GroupDeletionResponseDTO groupDeletionResponseDTO = new GroupDeletionResponseDTO();
        groupDeletionResponseDTO.setId(1L);
        groupDeletionResponseDTO.setStatus(GroupDeletionStatus.SUCCESS);
        groupDeletionResponseDTO.setName("Group 1");
        return groupDeletionResponseDTO;

    }


    private IDMGroup getGroup() {
        IDMGroup group = new IDMGroup();
        group.setId(1L);
        group.setDescription("Testing group 1");
        group.setName("Group 1");
        group.setStatus(UserAndGroupStatus.VALID);
        group.addUser(getUser());

        return group;
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setLogin("kypo");
        user.setFullName("KYPO LOCAL ADMIN");
        user.setMail("kypo@mail.cz");
        user.setStatus(UserAndGroupStatus.VALID);

        return user;
    }

    private UserForGroupsDTO getUserForGroupsDTO() {
        UserForGroupsDTO userDTO = new UserForGroupsDTO();
        userDTO.setId(1L);
        userDTO.setMail("kypo@mail.cz");
        userDTO.setFullName("KYPO LOCAL ADMIN");
        userDTO.setLogin("kypo");
        return userDTO;

    }

    private Role getAdminRole() {
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());
        return adminRole;
    }

    private RoleDTO getAdminRoleDTO() {
        RoleDTO adminRole = new RoleDTO();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());
        return adminRole;
    }

    private Role getGuestRole() {
        Role guestRole = new Role();
        guestRole.setId(2L);
        guestRole.setRoleType(RoleType.GUEST.name());
        return guestRole;
    }

    private RoleDTO getGuestRoleDTO() {
        RoleDTO guestRole = new RoleDTO();
        guestRole.setId(2L);
        guestRole.setRoleType(RoleType.GUEST.name());
        return guestRole;
    }

    private Set<Role> getRoles() {
        return Stream.of(getAdminRole(), getGuestRole()).collect(Collectors.toSet());
    }

    private Set<RoleDTO> getRolesDTO() {
        return Stream.of(getAdminRoleDTO(), getGuestRoleDTO()).collect(Collectors.toSet());
    }
}
