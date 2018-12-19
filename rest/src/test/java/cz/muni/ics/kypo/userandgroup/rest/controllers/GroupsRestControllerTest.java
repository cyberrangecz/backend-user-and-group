package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.exception.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.rest.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.api.dto.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
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
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
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
    private IDMGroupFacade groupFacade;

    @MockBean
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private GroupDTO groupDTO1, groupDTO2;
    private NewGroupDTO newGroupDTO;
    private UpdateGroupDTO updateGroupDTO;
    private int page, size;
    private PageResultResource<GroupDTO> groupPageResultResource;

    @Before
    public void setup() throws RuntimeException {
        page = 0;
        size = 10;

        groupDTO1 = new GroupDTO();
        groupDTO1.setId(1L);
        groupDTO1.setName("GroupOne");
        groupDTO1.setDescription("Group one");
        groupDTO1.setSource(Source.INTERNAL);

        groupDTO2 = new GroupDTO();
        groupDTO2.setId(2L);
        groupDTO2.setName("GroupTwo");
        groupDTO2.setDescription("Group two");
        groupDTO2.setSource(Source.INTERNAL);

        newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("GroupOne");
        newGroupDTO.setDescription("Group one");

        updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(1L);
        updateGroupDTO.setName("GroupOne");
        updateGroupDTO.setDescription("Group one");

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
    public void testCreateGroupCouldNotBeCreated() throws Exception {
        given(groupFacade.createGroup(any(NewGroupDTO.class))).willThrow(new UserAndGroupFacadeException());
        Exception ex = mockMvc.perform(
                post("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(convertObjectToJsonBytes(getNewGroupDTO())))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Some of given groups could not be found in database.", ex.getLocalizedMessage());
        then(groupFacade).should().createGroup(any(NewGroupDTO.class));
    }

    @Test
    public void testCreateGroupWithNullRequestBody() throws Exception {
        mockMvc.perform(
                post("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).isGroupInternal(anyLong());
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
    public void testUpdateExternalGroup() throws Exception {
        willThrow(ExternalSourceException.class).given(groupFacade).updateGroup(any(UpdateGroupDTO.class));
        Exception ex = mockMvc.perform(
                put("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(updateGroupDTO)))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore they could not be updated", ex.getLocalizedMessage());
        then(groupFacade).should().updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    public void testUpdateGroupWithNullNameAndDescription() throws Exception {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        Exception ex = mockMvc.perform(
                put("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(groupDTO)))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException();
        assertEquals("Group name neither group description cannot be null.", ex.getLocalizedMessage());
        then(groupFacade).should(never()).updateGroup(any(UpdateGroupDTO.class));
    }

    @Test
    public void testRemoveUsers() throws Exception {
        mockMvc.perform(
                put("/groups" + "/{id}/removeUsers", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Collections.singletonList(1L))))
                .andExpect(status().isNoContent());
        then(groupFacade).should().removeUsers(groupDTO1.getId(), Collections.singletonList(1L));
    }

    @Test
    public void testRemoveUsersWithUserAndGroupFacadeException() throws Exception {
        willThrow(UserAndGroupFacadeException.class).given(groupFacade).removeUsers(100L, Collections.singletonList(1L));
        Exception ex = mockMvc.perform(
                put("/groups" + "/{id}/removeUsers", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Given group or users could not be found", ex.getLocalizedMessage());
        then(groupFacade).should().removeUsers(100L, Collections.singletonList(1L));
    }

    @Test
    public void testRemoveUsersWithExternalSourceException() throws Exception {
        willThrow(ExternalSourceException.class).given(groupFacade).removeUsers(100L, Collections.singletonList(1L));
        Exception ex = mockMvc.perform(
                put("/groups" + "/{id}/removeUsers", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(1L))))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore it could not be edited", ex.getLocalizedMessage());
        then(groupFacade).should().removeUsers(100L, Collections.singletonList(1L));
    }

    @Test
    public void testRemoveUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                put("/groups" + "/{id}/removeUsers", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).isGroupInternal(anyLong());
        then(groupFacade).should(never()).removeUsers(anyLong(), anyList());
    }

    @Test
    public void testAddUsers() throws Exception {
        UserForGroupsDTO user = new UserForGroupsDTO();
        user.setId(1L);
        user.setLogin("user");
        user.setFullName("user one");
        user.setMail("user.one@mail.com");
        groupDTO1.setUsers(Collections.singletonList(user));

        mockMvc.perform(
                put("/groups" + "/addUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddUsersToGroupDTO())))
                .andExpect(status().isNoContent());
        then(groupFacade).should().addUsers(any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testAddUsersWithNotFoundError() throws Exception {
        willThrow(new UserAndGroupFacadeException()).given(groupFacade).addUsers(any(AddUsersToGroupDTO.class));
        Exception ex = mockMvc.perform(
                put("/groups" + "/addUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddUsersToGroupDTO())))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Given group or users could not be found", ex.getLocalizedMessage());
        then(groupFacade).should().addUsers(any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testAddUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                put("/groups" + "/addUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(groupFacade).should(never()).isGroupInternal(anyLong());
        then(groupFacade).should(never()).addUsers(any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testAddUsersWithExternalSourceException() throws Exception {
        willThrow(ExternalSourceException.class).given(groupFacade).addUsers(any(AddUsersToGroupDTO.class));
        Exception ex = mockMvc.perform(
                put("/groups" + "/addUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(getAddUsersToGroupDTO())))
                .andExpect(status().isNotModified())
                .andReturn().getResolvedException();
        assertEquals("Group is external therefore it could not be edited", ex.getLocalizedMessage());
        then(groupFacade).should().addUsers(any(AddUsersToGroupDTO.class));
    }

    @Test
    public void testDeleteGroup() throws Exception {
        given(groupFacade.deleteGroup(groupDTO1.getId())).willReturn(getGroupDeletionResponse());
        mockMvc.perform(
                delete("/groups" + "/{id}", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getGroupDeletionResponse())));
        then(groupFacade).should().deleteGroup(groupDTO1.getId());
    }

    @Test
    public void testDeleteExternalGroup() throws Exception {
        GroupDeletionResponseDTO deletionResponseDTO = getGroupDeletionResponse();
        deletionResponseDTO.setStatus(GroupDeletionStatus.EXTERNAL_VALID);
        given(groupFacade.deleteGroup(groupDTO1.getId())).willReturn(deletionResponseDTO);
        Exception ex = mockMvc.perform(
                delete("/groups" + "/{id}", groupDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andReturn().getResolvedException();
        assertEquals("Group with id " + groupDTO1.getId() + " cannot be deleted because is from external source and is valid group.", ex.getLocalizedMessage());
        then(groupFacade).should().deleteGroup(groupDTO1.getId());
    }

    @Test
    public void testDeleteGroups() throws Exception {
        given(groupFacade.deleteGroups(Collections.singletonList(1L))).willReturn(Collections.singletonList(getGroupDeletionResponse()));
        mockMvc.perform(
                delete("/groups" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Collections.singletonList(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(Collections.singletonList(getGroupDeletionResponse()))));
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
        given(groupFacade.getGroup(groupDTO1.getId())).willReturn(groupDTO1);
        mockMvc.perform(
                get("/groups" + "/{id}", groupDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(groupDTO1)));
        then(groupFacade).should().getGroup(groupDTO1.getId());
    }

    @Test
    public void testGetGroupWithGroupNotFound() throws Exception {
        given(groupFacade.getGroup(groupDTO1.getId())).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                get("/groups" + "/{id}", groupDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Group with id " + groupDTO1.getId() + " could not be found.", ex.getLocalizedMessage());
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
    public void testGetRolesOfGroupWithExceptionFromFacade() throws Exception {
        given(groupFacade.getRolesOfGroup(groupDTO1.getId())).willThrow(UserAndGroupFacadeException.class);
        Exception ex = mockMvc.perform(
                get("/groups" + "/{id}/roles", groupDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals("Group with id " + groupDTO1.getId() + " could not be found or some of microservice did not return status code 2xx.", ex.getLocalizedMessage());
        then(groupFacade).should().getRolesOfGroup(groupDTO1.getId());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }


    private AddUsersToGroupDTO getAddUsersToGroupDTO() {
        AddUsersToGroupDTO groupDTO = new AddUsersToGroupDTO();
        groupDTO.setGroupId(1L);
        groupDTO.setIdsOfGroupsOfImportedUsers(Arrays.asList(2L));
        groupDTO.setIdsOfUsersToBeAdd(Arrays.asList(3L));

        return groupDTO;
    }

    private NewGroupDTO getNewGroupDTO() {
        NewGroupDTO newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("GroupOne");
        newGroupDTO.setDescription("Group One");
        newGroupDTO.setGroupIdsOfImportedUsers(Arrays.asList(1L));
        newGroupDTO.setUsers(Arrays.asList(getUserForGroupsDTO()));

        return newGroupDTO;
    }

    private GroupDeletionResponseDTO getGroupDeletionResponse() {
        GroupDeletionResponseDTO groupDeletionResponseDTO = new GroupDeletionResponseDTO();
        groupDeletionResponseDTO.setId(1L);
        groupDeletionResponseDTO.setStatus(GroupDeletionStatus.SUCCESS);
        return groupDeletionResponseDTO;
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
