package cz.cyberrange.platform.userandgroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.rest.controller.GroupsRestController;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.group.AddUsersToGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupViewDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.rest.facade.IDMGroupFacade;
import cz.cyberrange.platform.userandgroup.rest.handler.CustomRestExceptionHandler;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertJsonBytesToObject;
import static cz.cyberrange.platform.userandgroup.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestDataFactory.class)
class GroupsRestControllerTest {


    @MockBean
    private IDMGroupFacade groupFacade;
    @MockBean
    private ObjectMapper objectMapper;
    private GroupsRestController groupsRestController;

    @Autowired
    private TestDataFactory testDataFactory;

    private MockMvc mockMvc;
    private AutoCloseable closeable;

    private GroupDTO groupDTO1, groupDTO2;
    private GroupViewDTO groupViewDTO1, groupViewDTO2;
    private NewGroupDTO newGroupDTO;
    private UpdateGroupDTO updateGroupDTO;
    private PageResultResource<GroupViewDTO> groupPageResultResource;

    @BeforeEach
    void setup() throws RuntimeException {
        closeable = MockitoAnnotations.openMocks(this);
        groupsRestController = new GroupsRestController(groupFacade, objectMapper);

        groupDTO1 = testDataFactory.getUAGUAdminGroupDTO();
        groupDTO1.setId(1L);

        groupDTO2 = testDataFactory.getUAGUserGroupDTO();
        groupDTO2.setId(2L);

        groupViewDTO1 = testDataFactory.getAdminGroupViewDTO();
        groupViewDTO1.setId(1L);

        groupViewDTO2 = testDataFactory.getPowerUserGroupViewDTO();
        groupViewDTO2.setId(2L);

        newGroupDTO = testDataFactory.getNewGroupDTO();

        updateGroupDTO = testDataFactory.getUpdateGroupDTO();
        updateGroupDTO.setId(1L);

        groupPageResultResource = new PageResultResource<>(Arrays.asList(groupViewDTO1, groupViewDTO2));

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

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

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    void contextLoads() {
        assertNotNull(groupsRestController);
    }

    @Test
    void testCreateGroup() throws Exception {
        given(groupFacade.createGroup(any(NewGroupDTO.class))).willReturn(groupDTO1);
        mockMvc.perform(
                post("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newGroupDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(groupDTO1)));
        then(groupFacade).should().createGroup(any(NewGroupDTO.class));
    }

    @Test
    void testCreateGroupWithNullRequestBody() throws Exception {
        mockMvc.perform(
                post("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    void testUpdateGroup() throws Exception {
        mockMvc.perform(
                put("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(updateGroupDTO)))
                .andExpect(status().isNoContent());
        then(groupFacade).should().updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    void testUpdateGroupWithNullNameAndDescription() throws Exception {
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
    void testRemoveUsers() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/{id}" + "/users", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Collections.singletonList(1L))))
                .andExpect(status().isNoContent());
        then(groupFacade).should().removeUsers(groupDTO1.getId(), Collections.singletonList(1L));
    }

    @Test
    void testRemoveUsersWithException() throws Exception {
        willThrow(new EntityNotFoundException()).given(groupFacade).removeUsers(100L, Collections.singletonList(1L));
        MockHttpServletResponse response = mockMvc.perform(
                delete("/groups" + "/{id}" + "/users", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(groupFacade).should().removeUsers(100L, Collections.singletonList(1L));
    }

    @Test
    void testRemoveUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).removeUsers(anyLong(), anyList());
    }

    @Test
    void testAddUsers() throws Exception {
        UserForGroupsDTO user = new UserForGroupsDTO();
        user.setId(1L);
        user.setSub("user");
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
    void testAddUsersWithNotFoundError() throws Exception {
        willThrow(new EntityNotFoundException())
                .given(groupFacade).addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
        MockHttpServletResponse response = mockMvc.perform(
                put("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddUsersToGroupDTO())))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(groupFacade).should().addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
    }

    @Test
    void testAddUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                put("/groups" + "/{id}/users", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).addUsersToGroup(anyLong(), any(AddUsersToGroupDTO.class));
    }

    @Test
    void testDeleteGroup() throws Exception {
        mockMvc.perform(
                delete("/groups" + "/{id}", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        then(groupFacade).should().deleteGroup(anyLong());
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
                new EntityNotFoundException());
        MockHttpServletResponse response = mockMvc.perform(
                get("/groups" + "/{id}", groupDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void testGetRolesOfGroup() throws Exception {
        given(groupFacade.getRolesOfGroup(eq(groupDTO1.getId()), any(Pageable.class), any(Predicate.class))).willReturn(getPageResultResourceRolesDTO());
        mockMvc.perform(
                get("/groups" + "/{id}/roles", groupDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getPageResultResourceRolesDTO())));
    }


    @Test
    public void assignRoleToGroup() throws Exception {
        mockMvc.perform(
                put("/groups/{groupId}/roles/{roleId}", 1, 2))
                .andExpect(status().isNoContent());
        then(groupFacade).should().assignRole(1L, 2L);
    }

    @Test
    public void assignRoleToGroupWithException() throws Exception {
        willThrow(new EntityNotFoundException()).given(groupFacade).assignRole(1L, 2L);
        MockHttpServletResponse response = mockMvc.perform(
                put("/groups/{groupId}/roles/{roleId}", 1L, 2L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
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
        willThrow(new EntityNotFoundException()).given(groupFacade).removeRoleFromGroup(1L, 2L);
        MockHttpServletResponse response = mockMvc.perform(
                delete("/groups/{groupId}/roles/{roleId}", 1L, 2L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void testRemoveRoleFromGroupWithRoleCannotBeRemovedException() throws Exception {
        willThrow(new EntityConflictException()).given(groupFacade).removeRoleFromGroup(1L, 2L);
        MockHttpServletResponse response = mockMvc.perform(
                delete("/groups/{groupId}/roles/{roleId}", 1L, 2L))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    private AddUsersToGroupDTO getAddUsersToGroupDTO() {
        AddUsersToGroupDTO groupDTO = new AddUsersToGroupDTO();
        groupDTO.setIdsOfGroupsOfImportedUsers(List.of(2L));
        groupDTO.setIdsOfUsersToBeAdd(List.of(3L));

        return groupDTO;
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
        guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_TRAINEE.name());
        return guestRole;
    }

    private PageResultResource<RoleDTO> getPageResultResourceRolesDTO() {
        return new PageResultResource(List.of(getAdminRoleDTO(), getGuestRoleDTO()));
    }

}
