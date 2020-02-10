package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableSpringDataWebSupport
@ContextConfiguration(classes = TestDataFactory.class)
public class GroupsRestControllerTest {

    @InjectMocks
    private GroupsRestController groupsRestController;

    @MockBean
    private IDMGroupFacade groupFacade;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    private MockMvc mockMvc;

    private GroupDTO groupDTO1, groupDTO2;
    private NewGroupDTO newGroupDTO;
    private UpdateGroupDTO updateGroupDTO;
    private PageResultResource<GroupDTO> groupPageResultResource;

    @Before
    public void setup() throws RuntimeException {
        groupDTO1 = testDataFactory.getUAGUAdminGroupDTO();
        groupDTO1.setId(1L);

        groupDTO2 = testDataFactory.getUAGUserGroupDTO();
        groupDTO2.setId(2L);

        newGroupDTO = testDataFactory.getNewGroupDTO();

        updateGroupDTO = testDataFactory.getUpdateGroupDTO();
        updateGroupDTO.setId(1L);

        groupPageResultResource = new PageResultResource<>(Arrays.asList(groupDTO1, groupDTO2));

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(groupsRestController)
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
        assertNotNull(groupsRestController);
    }

    @Test
    public void testCreateGroup() throws Exception {
        given(groupFacade.createGroup(any(NewGroupDTO.class))).willReturn(groupDTO1);
        mockMvc.perform(
                post("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newGroupDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(groupDTO1)));
        then(groupFacade).should().createGroup(any(NewGroupDTO.class));
    }

    @Test
    public void testCreateGroupWithNullRequestBody() throws Exception {
        mockMvc.perform(
                post("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    public void testUpdateGroup() throws Exception {
        mockMvc.perform(
                put("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(updateGroupDTO)))
                .andExpect(status().isNoContent());
        then(groupFacade).should().updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    public void testUpdateGroupWithNullNameAndDescription() throws Exception {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        Exception ex = mockMvc.perform(
                put("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(groupDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        then(groupFacade).should(never()).updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    public void testRemoveUsers() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/{id}" + "/users", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Collections.singletonList(1L))))
                .andExpect(status().isNoContent());
        then(groupFacade).should().removeUsers(groupDTO1.getId(), Collections.singletonList(1L));
    }

    @Test
    public void testRemoveUsersWithUserAndGroupFacadeException() throws Exception {
        willThrow(new UserAndGroupFacadeException(new UserAndGroupServiceException("No users with given ids were found", ErrorCode.RESOURCE_NOT_FOUND))).given(groupFacade).removeUsers(100L, Collections.singletonList(1L));
        Exception ex = mockMvc.perform(
                delete("/groups" + "/{id}" + "/users", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("No users with given ids were found", getInitialExceptionMessage(ex));
        then(groupFacade).should().removeUsers(100L, Collections.singletonList(1L));
    }

    @Test
    public void testRemoveUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).removeUsers(anyLong(), anyList());
    }

    @Test
    public void testAddUsers() throws Exception {
        UserForGroupsDTO user = new UserForGroupsDTO();
        user.setId(1L);
        user.setLogin("user");
        user.setFullName("user one");
        user.setMail("user.one@mail.com");
        groupDTO1.setUsers(Set.of(user));

        mockMvc.perform(
                put("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddUsersToGroupDTO())))
                .andExpect(status().isNoContent());
        then(groupFacade).should().addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testAddUsersWithNotFoundError() throws Exception {
        willThrow(new UserAndGroupFacadeException(new UserAndGroupServiceException("Given group or users could not be found", ErrorCode.RESOURCE_NOT_FOUND)))
                .given(groupFacade).addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
        Exception ex = mockMvc.perform(
                put("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddUsersToGroupDTO())))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Given group or users could not be found", getInitialExceptionMessage(ex));
        then(groupFacade).should().addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testAddUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                put("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testDeleteGroup() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/{id}", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        then(groupFacade).should().deleteGroup(groupDTO1.getId());
    }

    @Test
    public void testDeleteGroups() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Collections.singletonList(1L))))
                .andExpect(status().isOk());
        then(groupFacade).should().deleteGroups(Collections.singletonList(1L));
    }

    @Test
    public void testDeleteGroupsWithNoRequestBody() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).deleteGroups(anyList());
    }

    @Test
    public void testGetGroups() throws Exception {
        String valueAs = convertObjectToJsonBytes(groupPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
        given(groupFacade.getAllGroups(any(Predicate.class), any(Pageable.class))).willReturn(groupPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(
                get("/groups" + "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(groupPageResultResource)), result.getContentAsString());
        then(groupFacade).should().getAllGroups(any(Predicate.class), any(Pageable.class));
    }

    @Test
    public void testGetGroup() throws Exception {
        given(groupFacade.getGroupById(groupDTO1.getId())).willReturn(groupDTO1);
        mockMvc.perform(
                get("/groups" + "/{id}", groupDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(groupDTO1)));
        then(groupFacade).should().getGroupById(groupDTO1.getId());
    }

    @Test
    public void testGetGroupWithGroupNotFound() throws Exception {
        given(groupFacade.getGroupById(groupDTO1.getId())).willThrow(
                new UserAndGroupFacadeException(new UserAndGroupServiceException("Group with id " + groupDTO1.getId() + " could not be found.", ErrorCode.RESOURCE_NOT_FOUND)));
        Exception ex = mockMvc.perform(
                get("/groups" + "/{id}", groupDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Group with id " + groupDTO1.getId() + " could not be found.", getInitialExceptionMessage(ex));
    }

    @Test
    public void testGetRolesOfGroup() throws Exception {
        given(groupFacade.getRolesOfGroup(groupDTO1.getId())).willReturn(getRolesDTO());
        mockMvc.perform(
                get("/groups" + "/{id}/roles", groupDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getRolesDTO())));
        then(groupFacade).should().getRolesOfGroup(groupDTO1.getId());
    }


    @Test
    public void assignRoleToGroup() throws Exception {
        mockMvc.perform(
                put("/groups/{groupId}/roles/{roleId}", 1, 2))
                .andExpect(status().isNoContent());
        then(groupFacade).should().assignRole(1L, 2L);
    }

    @Test
    public void assignRoleToGroupWithUserAndGroupException() throws Exception {
        willThrow(new UserAndGroupFacadeException(new UserAndGroupServiceException(ErrorCode.RESOURCE_NOT_FOUND))).given(groupFacade).assignRole(1L, 2L);
        Exception exception = mockMvc.perform(
                put("/groups/{groupId}/roles/{roleId}", 1L, 2L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void testRemoveRoleFromGroup() throws Exception {
        mockMvc.perform(
                delete("/groups/{groupId}/roles/{roleId}", 1, 2))
                .andExpect(status().isNoContent());
        then(groupFacade).should().removeRoleFromGroup(1L, 2L);
    }

    @Test
    public void testRemoveRoleWithUserAndGroupException() throws Exception {
        willThrow(new UserAndGroupFacadeException(new UserAndGroupServiceException(ErrorCode.RESOURCE_NOT_FOUND))).given(groupFacade).removeRoleFromGroup(1L, 2L);
        Exception exception = mockMvc.perform(
                delete("/groups/{groupId}/roles/{roleId}", 1L, 2L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void testRemoveRoleFromGroupWithRoleCannotBeRemovedException() throws Exception {
        willThrow(new UserAndGroupFacadeException(new UserAndGroupServiceException("Cannot be removed.", ErrorCode.RESOURCE_CONFLICT))).given(groupFacade).removeRoleFromGroup(1L, 2L);
        Exception exception = mockMvc.perform(
                delete("/groups/{groupId}/roles/{roleId}", 1L, 2L))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, exception.getClass());

    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }


    private AddUsersToGroupDTO getAddUsersToGroupDTO() {
        AddUsersToGroupDTO groupDTO = new AddUsersToGroupDTO();
        groupDTO.setIdsOfGroupsOfImportedUsers(Arrays.asList(2L));
        groupDTO.setIdsOfUsersToBeAdd(Arrays.asList(3L));

        return groupDTO;
    }

    private NewGroupDTO getNewGroupDTO() {
        NewGroupDTO newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("GroupOne");
        newGroupDTO.setDescription("Group One");
        newGroupDTO.setGroupIdsOfImportedUsers(Arrays.asList(1L));
        newGroupDTO.setUsers(Set.of(getUserForGroupsDTO()));

        return newGroupDTO;
    }

    private UserForGroupsDTO getUserForGroupsDTO() {
        UserForGroupsDTO userDTO = new UserForGroupsDTO();
        userDTO.setId(1L);
        userDTO.setMail("kypo@mail.cz");
        userDTO.setFullName("KYPO LOCAL ADMIN");
        userDTO.setLogin("kypo");
        return userDTO;
    }

    private RoleDTO getAdminRoleDTO() {
        RoleDTO adminRole = new RoleDTO();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name());
        return adminRole;
    }

    private RoleDTO getGuestRoleDTO() {
        RoleDTO guestRole = new RoleDTO();
        guestRole.setId(2L);
        guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.name());
        return guestRole;
    }

    private Set<RoleDTO> getRolesDTO() {
        return Stream.of(getAdminRoleDTO(), getGuestRoleDTO()).collect(Collectors.toSet());
    }

    private String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }

}
