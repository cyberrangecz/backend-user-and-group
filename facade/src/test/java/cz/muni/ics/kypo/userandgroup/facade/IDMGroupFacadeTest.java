package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.config.FacadeTestConfig;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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

import java.util.*;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
@ComponentScan(basePackages = {
        "cz.muni.ics.kypo.userandgroup.facade",
        "cz.muni.ics.kypo.userandgroup.service",
        "cz.muni.ics.kypo.userandgroup.mapping"
})
@Import(FacadeTestConfig.class)
public class IDMGroupFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private IDMGroupFacade groupFacade;

    @MockBean
    private IDMGroupService groupService;

    @MockBean
    private MicroserviceService microserviceService;

    @MockBean
    private RestTemplate restTemplate;

    private IDMGroup g1, g2;
    private GroupDTO groupDTO;
    private NewGroupDTO newGroupDTO;
    private User user1;
    private UserForGroupsDTO userForGroupsDTO;
    private Predicate predicate;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
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
        newGroupDTO.setUsers(Collections.singletonList(userForGroupsDTO));

        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.GUEST.toString());
        Role[] rolesArray = new Role[1];
        rolesArray[0] = role;
        ResponseEntity<Role[]> responseEntity = new ResponseEntity<>(rolesArray, HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Role[].class), anyLong())).willReturn(responseEntity);
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
        given(groupService.removeUsers(1L, Arrays.asList(1L))).willReturn(g2);
        groupFacade.removeUsers(1L, Arrays.asList(1L));

        assertEquals(0, groupDTO.getUsers().size());
        then(groupService).should().removeUsers(1L, Arrays.asList(1L));

    }

    @Test
    public void testRemoveUsersWithServiceExcpetion() {
        given(groupService.removeUsers(1L, Arrays.asList(1L))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.removeUsers(1L, Arrays.asList(1L));
    }

    @Test
    public void testAddUsers() {
        GroupDTO g = groupDTO;
        g.setUsers(Arrays.asList(new UserForGroupsDTO()));
        given(groupService.addUsers(1L, Arrays.asList(1L), Arrays.asList(1L))).willReturn(g1);
        groupFacade.addUsers(getaddUsersToGroupDTO());

        assertEquals(1, groupDTO.getUsers().size());
        then(groupService).should().addUsers(1L, Arrays.asList(1L), Arrays.asList(1L));

    }

    @Test
    public void testAddUsersWithServiceException() {
        given(groupService.addUsers(1L, Arrays.asList(1L), Arrays.asList(1L))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.addUsers(getaddUsersToGroupDTO());
    }

    @Test
    public void testDeleteGroup() {
        given(groupService.get(anyLong())).willReturn(g1);
        given(groupService.delete(any(IDMGroup.class))).willReturn(GroupDeletionStatus.SUCCESS);
        GroupDeletionResponseDTO groupDeletionResponseDTO = groupFacade.deleteGroup(1L);

        assertEquals(GroupDeletionStatus.SUCCESS, groupDeletionResponseDTO.getStatus());
        then(groupService).should().delete(g1);
    }

    @Test
    public void testDeleteGroupWithServiceException() {
        given(groupService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.deleteGroup(1L);
    }

    @Test
    public void testDeleteGroups() {
        Map<IDMGroup, GroupDeletionStatus> groupGroupDeletionStatusMap = new HashMap<>();
        groupGroupDeletionStatusMap.put(g1, GroupDeletionStatus.SUCCESS);

        given(groupService.deleteGroups(anyList())).willReturn(groupGroupDeletionStatusMap);
        List<GroupDeletionResponseDTO> responseDTOS = groupFacade.deleteGroups(Arrays.asList(1L));

        assertEquals(GroupDeletionStatus.SUCCESS, responseDTOS.get(0).getStatus());
        then(groupService).should().deleteGroups(Arrays.asList(1L));

    }

    @Test
    public void testGetAllGroups() {
        Role[] rolesArray = new Role[1];
        rolesArray[0] = getRole();
        mockSpringSecurityContextForGet(rolesArray);
        Page<IDMGroup> idmGroupPage = new PageImpl<>(Arrays.asList(g1));
        PageResultResource<GroupDTO> pages = new PageResultResource<>();
        pages.setContent(Arrays.asList(groupDTO));

        given(groupService.getAllIDMGroups(predicate, pageable)).willReturn(idmGroupPage);
        given(groupService.get(anyLong())).willReturn(g1);
        PageResultResource<GroupDTO> responseDTOPageResultResource = groupFacade.getAllGroups(predicate, pageable);

        assertEquals(1, responseDTOPageResultResource.getContent().size());
        then(groupService).should().getAllIDMGroups(predicate, pageable);
    }

    @Test
    public void testGetGroup() {
        Role[] rolesArray = new Role[1];
        rolesArray[0] = getRole();
        mockSpringSecurityContextForGet(rolesArray);
        given(groupService.get(anyLong())).willReturn(g1);
        GroupDTO groupDTO = groupFacade.getGroup(1L);

        assertEquals(g1.getName(), groupDTO.getName());
        then(groupService).should(times(2)).get(1L);
    }

    @Test
    public void testGetGroupWithServiceException() {
        given(groupService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.getGroup(1L);
    }

    @Test
    public void testGetRolesOfGroup() {
        Microservice m = new Microservice("training", "/training");
        Set<Role> roles = new HashSet<>();
        roles.add(getRole());
        g1.addRole(getRole());
        Role[] rolesArray = new Role[1];
        rolesArray[0] = getRole();
        mockSpringSecurityContextForGet(rolesArray);

        Set<RoleDTO> roleDTOS = new HashSet<>();
        roleDTOS.add(getRoleDTO());

        given(groupService.get(anyLong())).willReturn(g1);
        given(microserviceService.getMicroservices()).willReturn(Collections.singletonList(m));
        Set<RoleDTO> rolesDTO = groupFacade.getRolesOfGroup(1L);
        assertEquals(2, rolesDTO.size());
        assertEquals(RoleType.USER.toString(), new ArrayList<>(roleDTOS).get(0).getRoleType());
    }

    @Test
    public void testGetRolesOfGroupWithServiceThrowsException() {
        given(groupService.get(anyLong())).willThrow(UserAndGroupServiceException.class);
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.getRolesOfGroup(1L);
    }

    @Test
    public void testAssignRole() {
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleType(RoleType.USER.toString());
        g1.addRole(userRole);
        given(groupService.assignRole(anyLong(), any(RoleType.class))).willReturn(g1);
        groupFacade.assignRole(1L, RoleType.USER);

        then(groupService).should().assignRole(1L, RoleType.USER);
    }

    @Test
    public void testAssignRoleWithServiceException() {
        given(groupService.assignRole(anyLong(), any(RoleType.class))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.assignRole(1L, RoleType.USER);
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
        addUsers.setGroupId(1L);
        addUsers.setIdsOfUsersToBeAdd(Arrays.asList(1L));
        addUsers.setIdsOfGroupsOfImportedUsers(Arrays.asList(1L));
        return addUsers;
    }

    private GroupDeletionResponseDTO getGroupDeletionResponseDTO() {
        GroupDeletionResponseDTO gdrDTO = new GroupDeletionResponseDTO();
        gdrDTO.setId(1L);
        gdrDTO.setName("Group deletion response");
        return gdrDTO;
    }

    private Role getRole() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.USER.toString());
        return role;
    }

    private RoleDTO getRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType(RoleType.USER.toString());
        return roleDTO;
    }

    private void mockSpringSecurityContextForGet(Role[] rolesArray) {
        ResponseEntity<Role[]> responseEntity = new ResponseEntity<>(rolesArray, HttpStatus.NO_CONTENT);
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails auth = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getDetails()).willReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        given(auth.getTokenType()).willReturn("");
        given(auth.getTokenValue()).willReturn("");
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Role[].class), anyLong())).willReturn(responseEntity);
    }
}
