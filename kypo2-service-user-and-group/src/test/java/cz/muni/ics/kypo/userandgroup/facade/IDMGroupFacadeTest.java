package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.config.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.IDMGroupMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class, IDMGroupMapperImpl.class})
public class IDMGroupFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private IDMGroupFacade groupFacade;
    @Mock
    private IDMGroupService groupService;
    @Mock
    private MicroserviceService microserviceService;
    @Mock
    private RestTemplate restTemplate;

    @Autowired
    private RoleMapperImpl roleMapper;
    @Autowired
    private IDMGroupMapperImpl groupMapper;

    private IDMGroup g1, g2;
    private Role adminRole, userRole;
    private GroupDTO groupDTO;
    private NewGroupDTO newGroupDTO;
    private User user1;
    private UserForGroupsDTO userForGroupsDTO;
    private Microservice microservice;
    private Predicate predicate;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        groupFacade = new IDMGroupFacadeImpl(groupService, microserviceService, roleMapper, groupMapper);

        user1 = new User();
        user1.setId(1L);
        user1.setLogin("user1");
        user1.setFullName("User 1");

        userForGroupsDTO = new UserForGroupsDTO();
        userForGroupsDTO.setId(1L);
        userForGroupsDTO.setLogin("user1");
        userForGroupsDTO.setFullName("User 1");

        g1 = new IDMGroup();
        g1.setId(1L);
        g1.setName("Group 1");
        g1.setStatus(UserAndGroupStatus.VALID);
        g1.addUser(user1);

        g2 = new IDMGroup();
        g2.setId(2L);
        g2.setName("Group 2");
        g2.setStatus(UserAndGroupStatus.VALID);

        groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        groupDTO.setName("Group 1");

        newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("Group 1");
        newGroupDTO.setUsers(Set.of(userForGroupsDTO));

        microservice = new Microservice("training", "www.ttt.com/trainings");

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setMicroservice(microservice);
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name());

        userRole = new Role();
        userRole.setId(2L);
        userRole.setMicroservice(microservice);
        userRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.name());

        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
        Role[] rolesArray = new Role[1];
        rolesArray[0] = role;

        mockAuthentication();
    }

    @Test
    public void testCreateGroup() {
        given(groupService.create(any(IDMGroup.class), anyList())).willReturn(g1);
        GroupDTO groupDTO = groupFacade.createGroup(newGroupDTO);

        assertEquals(g1.getName(), groupDTO.getName());
        assertEquals(1, groupDTO.getUsers().size());
        assertTrue(groupDTO.getUsers().contains(userForGroupsDTO));
        then(groupService).should().create(any(IDMGroup.class), anyList());

    }

    @Test
    public void testUpdateGroup() {
        UpdateGroupDTO updatedGroupDTO = new UpdateGroupDTO();
        updatedGroupDTO.setId(g2.getId());
        updatedGroupDTO.setName(g2.getName());
        updatedGroupDTO.setDescription(g2.getDescription());
        groupFacade.updateGroup(updatedGroupDTO);
        then(groupService).should().update(g2);
    }

    @Test
    public void testRemoveUsers() {
        given(groupService.removeUsers(1L, Collections.singletonList(1L))).willReturn(g2);
        groupFacade.removeUsers(1L, Collections.singletonList(1L));

        assertEquals(0, groupDTO.getUsers().size());
        then(groupService).should().removeUsers(1L, Collections.singletonList(1L));

    }

    @Test
    public void testRemoveUsersWithServiceException() {
        given(groupService.removeUsers(1L, Collections.singletonList(1L))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.removeUsers(1L, Collections.singletonList(1L));
    }

    @Test
    public void testAddUsers() {
        GroupDTO g = groupDTO;
        g.setUsers(Set.of(new UserForGroupsDTO()));
        given(groupService.addUsers(1L, Collections.singletonList(1L), Collections.singletonList(1L))).willReturn(g1);
        groupFacade.addUsers(1L, getaddUsersToGroupDTO());

        assertEquals(1, groupDTO.getUsers().size());
        then(groupService).should().addUsers(1L, Collections.singletonList(1L), Collections.singletonList(1L));

    }

    @Test
    public void testAddUsersWithServiceException() {
        given(groupService.addUsers(1L, Collections.singletonList(1L), Collections.singletonList(1L))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.addUsers(1L, getaddUsersToGroupDTO());
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testDeleteGroupWithPersonsThrows() {
        // group g1 contains persons thus it is no possible to remove this group
        given(groupService.get(anyLong())).willReturn(g1);
        groupFacade.deleteGroup(1L);
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testDeleteGroupsWithPersonsThrows() {
        // group g1 contains persons thus it is no possible to remove this group
        given(groupService.get(anyLong())).willReturn(g1);
        List<GroupDeletionResponseDTO> responseDTOS = groupFacade.deleteGroups(Collections.singletonList(1L));
    }

    @Test
    public void testGetAllGroups() {
        RoleDTO[] rolesArray = new RoleDTO[1];
        rolesArray[0] = getRoleDTO();
        mockSpringSecurityContextForGet(rolesArray);
        Page<IDMGroup> idmGroupPage = new PageImpl<>(Collections.singletonList(g1));
        PageResultResource<GroupDTO> pages = new PageResultResource<>();
        pages.setContent(Collections.singletonList(groupDTO));

        given(groupService.getAllIDMGroups(predicate, pageable)).willReturn(idmGroupPage);
        given(groupService.get(anyLong())).willReturn(g1);
        PageResultResource<GroupDTO> responseDTOPageResultResource = groupFacade.getAllGroups(predicate, pageable);

        assertEquals(1, responseDTOPageResultResource.getContent().size());
        then(groupService).should().getAllIDMGroups(predicate, pageable);
    }

    @Test
    public void testGetGroup() {
        given(groupService.get(anyLong())).willReturn(g1);
        GroupDTO groupDTO = groupFacade.getGroup(1L);

        assertEquals(g1.getName(), groupDTO.getName());
        then(groupService).should(times(1)).get(1L);
        then(groupService).should().getRolesOfGroup(1L);
    }

    @Test
    public void testGetGroupWithServiceException() {
        given(groupService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.getGroup(1L);
    }

    @Test
    public void testGetRolesOfGroup() {

        given(groupService.getRolesOfGroup(anyLong())).willReturn(Set.of(adminRole, userRole));
        Set<RoleDTO> rolesDTO = groupFacade.getRolesOfGroup(1L);
        assertEquals(2, rolesDTO.size());
        assertTrue(rolesDTO.stream().anyMatch(r -> r.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name())));
        assertTrue(rolesDTO.stream().anyMatch(r -> r.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_USER.name())));
    }

    @Test
    public void testGetRolesOfGroupWithServiceThrowsException() {
        given(groupService.getRolesOfGroup(anyLong())).willThrow(UserAndGroupServiceException.class);
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.getRolesOfGroup(1L);
    }

    @Test
    public void isGroupInternal() {
        given(groupService.isGroupInternal(g1.getId())).willReturn(true);
        assertTrue(groupFacade.isGroupInternal(g1.getId()));
        then(groupService).should().isGroupInternal(g1.getId());
    }

    @Test
    public void isGroupExternal() {
        g1.setExternalId(1L);
        given(groupService.isGroupInternal(g1.getId())).willReturn(false);
        assertFalse(groupFacade.isGroupInternal(g1.getId()));
        then(groupService).should().isGroupInternal(g1.getId());
    }

    @Test
    public void isGroupInternalWhenServiceThrowsException() {
        given(groupService.isGroupInternal(g1.getId())).willThrow(UserAndGroupServiceException.class);
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.isGroupInternal(g1.getId());
    }

    private UpdateGroupDTO getUpdateGroupDTO() {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(2L);
        updateGroupDTO.setName("Group 2");
        return updateGroupDTO;
    }


    private AddUsersToGroupDTO getaddUsersToGroupDTO() {
        AddUsersToGroupDTO addUsers = new AddUsersToGroupDTO();
        addUsers.setIdsOfUsersToBeAdd(Collections.singletonList(1L));
        addUsers.setIdsOfGroupsOfImportedUsers(Collections.singletonList(1L));
        return addUsers;
    }

    private GroupDeletionResponseDTO getGroupDeletionResponseDTO() {
        GroupDeletionResponseDTO gdrDTO = new GroupDeletionResponseDTO();
        gdrDTO.setId(1L);
        return gdrDTO;
    }

    private Role getRole() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
        return role;
    }

    private RoleDTO getRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
        return roleDTO;
    }

    private void mockAuthentication() {
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails auth = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getDetails()).willReturn(auth);
        given(auth.getTokenType()).willReturn("");
        given(auth.getTokenValue()).willReturn("");
    }

    private void mockSpringSecurityContextForGet(RoleDTO[] rolesArray) {
        ResponseEntity<RoleDTO[]> responseEntity = new ResponseEntity<>(rolesArray, HttpStatus.NO_CONTENT);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleDTO[].class), anyLong())).willReturn(responseEntity);
    }
}
