package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.IDMGroupMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class, IDMGroupMapperImpl.class})
@ContextConfiguration(classes = {TestDataFactory.class})
public class IDMGroupFacadeTest {

    private IDMGroupFacade groupFacade;
    @Mock
    private IDMGroupService groupService;
    @Mock
    private UserService userService;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private RoleMapperImpl roleMapper;
    @Autowired
    private IDMGroupMapperImpl groupMapper;

    private IDMGroup group1, group2;
    private NewGroupDTO newGroupDTO;
    private UpdateGroupDTO updateGroupDTO;
    private User user1, user2, user3, user4;
    private UserForGroupsDTO userForGroupsDTO;
    private Role adminRole, userRole;
    private RoleDTO adminRoleDTO, userRoleDTO;
    private GroupDTO groupDTO1, groupDTO2;
    private Predicate predicate;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        groupFacade = new IDMGroupFacadeImpl(groupService, userService, roleMapper, groupMapper);

        user1 = testDataFactory.getUser1();
        user1.setId(1L);
        user2 = testDataFactory.getUser2();
        user2.setId(2L);
        user3 = testDataFactory.getUser3();
        user3.setId(3L);
        user4 = testDataFactory.getUser4();
        user4.setId(4L);

        group1 = testDataFactory.getUAGAdminGroup();
        group1.setUsers(new HashSet<>(Set.of(user1, user2, user3, user4)));
        group1.setId(1L);
        group2 = testDataFactory.getUAGUserGroup();
        group2.setId(2L);

        groupDTO1 = testDataFactory.getUAGUAdminGroupDTO();
        groupDTO2 = testDataFactory.getUAGUserGroupDTO();

        updateGroupDTO = testDataFactory.getUpdateGroupDTO();
        updateGroupDTO.setId(10L);

        newGroupDTO = testDataFactory.getNewGroupDTO();
        userForGroupsDTO = testDataFactory.getUserForGroupsDTO1();
        newGroupDTO.setUsers(Set.of(userForGroupsDTO));

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        group1.setRoles(new HashSet<>(Set.of(adminRole)));
        userRole = testDataFactory.getUAGUserRole();
        userRole.setId(2L);

        userRoleDTO = testDataFactory.getUAGUserRoleDTO();
        userRoleDTO.setId(userRole.getId());
        adminRoleDTO = testDataFactory.getuAGAdminRoleDTO();
        adminRoleDTO.setId(adminRole.getId());
    }

    @Test
    public void testCreateGroup() {
        group1.setUsers(Set.of(user1));
        userForGroupsDTO.setId(user1.getId());
        given(groupService.createIDMGroup(any(IDMGroup.class), anyList())).willReturn(group1);

        GroupDTO groupDTO = groupFacade.createGroup(newGroupDTO);
        assertEquals(group1.getName(), groupDTO.getName());
        assertEquals(1, groupDTO.getUsers().size());
        assertTrue(groupDTO.getUsers().contains(userForGroupsDTO));
    }

    @Test
    public void testUpdateGroup() {
        IDMGroup groupToUpdate = new IDMGroup();
        groupToUpdate.setId(updateGroupDTO.getId());
        groupToUpdate.setName(updateGroupDTO.getName());
        groupToUpdate.setDescription(updateGroupDTO.getDescription());
        groupFacade.updateGroup(updateGroupDTO);
        then(groupService).should().updateIDMGroup(groupToUpdate);
    }

    @Test
    public void removeUsers() {
        List<Long> userIds = new ArrayList<>(List.of(user1.getId(), user3.getId()));
        given(groupService.getGroupById(group1.getId())).willReturn(group1);
        given(userService.getUsersByIds(userIds)).willReturn(List.of(user1, user3));

        groupFacade.removeUsers(group1.getId(), userIds);
        then(groupService).should().removeUserFromGroup(group1, user1);
        then(groupService).should().removeUserFromGroup(group1, user3);
        then(groupService).should().updateIDMGroup(group1);
    }

    @Test
    public void removeUsersNotInDB() {
        List<Long> userIds = new ArrayList<>(List.of(user1.getId(), user3.getId()));
        given(groupService.getGroupById(group1.getId())).willReturn(group1);
        given(userService.getUsersByIds(userIds)).willReturn(List.of());

        groupFacade.removeUsers(group1.getId(), userIds);

        then(groupService).should(never()).removeUserFromGroup(group1, user1);
        then(groupService).should(never()).removeUserFromGroup(group1, user3);
        then(groupService).should().updateIDMGroup(group1);
    }

    @Test
    public void addUser() {
        given(groupService.getGroupById(group2.getId())).willReturn(group2);
        given(userService.getUserById(user1.getId())).willReturn(user1);

        groupFacade.addUser(group2.getId(), user1.getId());
        then(groupService).should().addUserToGroup(group2, user1);
    }

    @Test
    public void addUsersToGroup() {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), user3.getId()));
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(group1.getId()));

        given(groupService.getGroupById(group2.getId())).willReturn(group2);
        given(userService.getUsersByIds(List.of(user2.getId(), user3.getId()))).willReturn(List.of(user2, user3));
        given(groupService.getGroupsByIds(List.of(group1.getId()))).willReturn(List.of(group1));

        groupFacade.addUsersToGroup(group2.getId(), addUsersToGroupDTO);
        then(groupService).should().updateIDMGroup(group2);
        then(groupService).should().evictUserFromCache(user2);
        then(groupService).should().evictUserFromCache(user3);
    }

    @Test
    public void addUsersToGroupWithNoUsersFound() {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfUsersToBeAdd(List.of(user2.getId(), user3.getId()));

        given(groupService.getGroupById(group2.getId())).willReturn(group2);
        given(userService.getUsersByIds(List.of(user2.getId(), user3.getId()))).willReturn(List.of());

        groupFacade.addUsersToGroup(group2.getId(), addUsersToGroupDTO);
        then(groupService).should().updateIDMGroup(group2);
        then(groupService).should(never()).evictUserFromCache(user2);
        then(groupService).should(never()).evictUserFromCache(user3);
    }

    @Test
    public void addUsersToGroupWithNoGroupsFound() {
        AddUsersToGroupDTO addUsersToGroupDTO = new AddUsersToGroupDTO();
        addUsersToGroupDTO.setIdsOfGroupsOfImportedUsers(List.of(group1.getId()));

        given(groupService.getGroupById(group2.getId())).willReturn(group2);
        given(groupService.getGroupsByIds(List.of(group1.getId()))).willReturn(List.of());

        groupFacade.addUsersToGroup(group2.getId(), addUsersToGroupDTO);
        then(groupService).should().updateIDMGroup(group2);
        then(groupService).should(never()).evictUserFromCache(user2);
        then(groupService).should(never()).evictUserFromCache(user3);
    }

    @Test
    public void testDeleteGroup() {
        given(groupService.getGroupById(group1.getId())).willReturn(group1);
        groupFacade.deleteGroup(group1.getId());
        then(groupService).should().deleteIDMGroup(group1);
    }

    @Test
    public void testDeleteGroups() {
        given(groupService.getGroupsByIds(anyList())).willReturn(List.of(group1, group2));
        groupFacade.deleteGroups(List.of(group1.getId(), group2.getId()));
        then(groupService).should().deleteIDMGroup(group1);
        then(groupService).should().deleteIDMGroup(group2);
    }

    @Test
    public void testGetAllGroups() {
        Page<IDMGroup> idmGroupPage = new PageImpl<>(List.of(group1, group2));
        groupDTO1.setId(group1.getId());
        groupDTO2.setId(group2.getId());
        given(groupService.getAllIDMGroups(predicate, pageable)).willReturn(idmGroupPage);
        PageResultResource<GroupDTO> responseDTOPageResultResource = groupFacade.getAllGroups(predicate, pageable);

        assertEquals(2, responseDTOPageResultResource.getContent().size());
        assertTrue(responseDTOPageResultResource.getContent().containsAll(List.of(groupDTO1, groupDTO2)));
        then(groupService).should().getAllIDMGroups(predicate, pageable);
    }

    @Test
    public void testGetGroup() {
        given(groupService.getGroupById(anyLong())).willReturn(group1);
        GroupDTO groupDTO = groupFacade.getGroupById(group1.getId());

        assertEquals(group1.getName(), groupDTO.getName());
        then(groupService).should(times(1)).getGroupById(group1.getId());
    }

    @Test
    public void testGetGroupWithRoles() {
        group1.addRole(userRole);

        given(groupService.getIDMGroupWithRolesByName(group1.getName())).willReturn(group1);
        GroupWithRolesDTO groupWithRolesDTO = groupFacade.getIDMGroupWithRolesByName(group1.getName());

        assertEquals(group1.getName(), groupWithRolesDTO.getName());
        assertTrue(groupWithRolesDTO.getRoles().containsAll(Set.of(userRoleDTO, adminRoleDTO)));
        then(groupService).should().getIDMGroupWithRolesByName(group1.getName());
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
    public void assignRoleToGroup() {
        given(groupService.assignRole(group1.getId(), userRole.getId())).willReturn(group1);

        groupFacade.assignRole(group1.getId(), userRole.getId());
        group1.getUsers().forEach(user -> {
            then(groupService).should().evictUserFromCache(user);
        });
    }

    @Test
    public void removeRoleFromGroup() {
        given(groupService.removeRoleFromGroup(group1.getId(), adminRole.getId())).willReturn(group1);

        groupFacade.removeRoleFromGroup(group1.getId(), adminRole.getId());
        group1.getUsers().forEach(user -> {
            then(groupService).should().evictUserFromCache(user);
        });
    }

}
