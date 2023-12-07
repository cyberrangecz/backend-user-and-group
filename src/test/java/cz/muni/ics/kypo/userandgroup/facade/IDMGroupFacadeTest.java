package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.group.*;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.mapping.IDMGroupMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.service.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.UserService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = { TestDataFactory.class, RoleMapperImpl.class, IDMGroupMapperImpl.class })
public class IDMGroupFacadeTest {

    private IDMGroupFacade groupFacade;
    @MockBean
    private IDMGroupService groupService;
    @MockBean
    private UserService userService;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private RoleMapperImpl roleMapper;
    @Autowired
    private IDMGroupMapperImpl groupMapper;

    private AutoCloseable closeable;
    private IDMGroup group1, group2;
    private NewGroupDTO newGroupDTO;
    private UpdateGroupDTO updateGroupDTO;
    private User user1, user2, user3, user4;
    private Microservice userAndGroupMicroservice;
    private Predicate predicate;
    private UserForGroupsDTO userForGroupsDTO;
    private Role adminRole, powerUserRole;
    private RoleDTO adminRoleDTO, powerUserRoleDTO;
    private GroupDTO groupDTO1, groupDTO2;
    private GroupViewDTO groupViewDTO1, groupViewDTO2;
    private Pageable pageable;

    @BeforeEach
    public void init() {
        closeable = MockitoAnnotations.openMocks(this);
        groupFacade = new IDMGroupFacade(groupService, userService, roleMapper, groupMapper);

        user1 = testDataFactory.getUser1();
        user1.setId(1L);
        user2 = testDataFactory.getUser2();
        user2.setId(2L);
        user3 = testDataFactory.getUser3();
        user3.setId(3L);
        user4 = testDataFactory.getUser4();
        user4.setId(4L);

        userAndGroupMicroservice = testDataFactory.getKypoUaGMicroservice();
        userAndGroupMicroservice.setId(1L);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        adminRole.setMicroservice(userAndGroupMicroservice);
        powerUserRole = testDataFactory.getUAGPowerUserRole();
        powerUserRole.setMicroservice(userAndGroupMicroservice);
        powerUserRole.setId(2L);

        group1 = testDataFactory.getUAGAdminGroup();
        group1.setUsers(new HashSet<>(Set.of(user1, user2, user3, user4)));
        group1.setRoles(new HashSet<>(Set.of(powerUserRole, adminRole)));
        group1.setId(1L);
        group2 = testDataFactory.getUAGPowerUserGroup();
        group2.setId(2L);

        groupDTO1 = testDataFactory.getUAGUAdminGroupDTO();
        groupDTO2 = testDataFactory.getUAGUserGroupDTO();

        groupViewDTO1 = testDataFactory.getAdminGroupViewDTO();
        groupViewDTO1.setId(1L);
        groupViewDTO2 = testDataFactory.getPowerUserGroupViewDTO();
        groupViewDTO2.setId(2L);

        updateGroupDTO = testDataFactory.getUpdateGroupDTO();
        updateGroupDTO.setId(10L);

        newGroupDTO = testDataFactory.getNewGroupDTO();
        userForGroupsDTO = testDataFactory.getUserForGroupsDTO1();
        newGroupDTO.setUsers(Set.of(userForGroupsDTO));


        powerUserRoleDTO = testDataFactory.getUAGPowerUserRoleDTO();
        powerUserRoleDTO.setId(powerUserRole.getId());
        adminRoleDTO = testDataFactory.getUAGAdminRoleDTO();
        adminRoleDTO.setId(adminRole.getId());
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
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
        PageResultResource<GroupViewDTO> responseDTOPageResultResource = groupFacade.getAllGroups(predicate, pageable);

        assertEquals(2, responseDTOPageResultResource.getContent().size());

        assertTrue(responseDTOPageResultResource.getContent().containsAll(List.of(groupViewDTO1, groupViewDTO2)));
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
        group1.addRole(powerUserRole);

        given(groupService.getIDMGroupWithRolesByName(group1.getName())).willReturn(group1);
        GroupWithRolesDTO groupWithRolesDTO = groupFacade.getIDMGroupWithRolesByName(group1.getName());

        assertEquals(group1.getName(), groupWithRolesDTO.getName());
        assertTrue(groupWithRolesDTO.getRoles().containsAll(Set.of(powerUserRoleDTO, adminRoleDTO)));
        then(groupService).should().getIDMGroupWithRolesByName(group1.getName());
    }

    @Test
    public void testGetRolesOfGroup() {
        given(groupService.getRolesOfGroup(anyLong(), eq(pageable), eq(predicate))).willReturn(new PageImpl<>(List.of(adminRole, powerUserRole)));
        PageResultResource<RoleDTO> rolesDTO = groupFacade.getRolesOfGroup(1L, pageable, predicate);

        assertEquals(2, rolesDTO.getContent().size());
        assertTrue(rolesDTO.getContent().stream().anyMatch(r -> r.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name())));
        assertTrue(rolesDTO.getContent().stream().anyMatch(r -> r.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_POWER_USER.name())));
    }

    @Test
    public void assignRoleToGroup() {
        given(groupService.assignRole(group1.getId(), powerUserRole.getId())).willReturn(group1);

        groupFacade.assignRole(group1.getId(), powerUserRole.getId());
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
