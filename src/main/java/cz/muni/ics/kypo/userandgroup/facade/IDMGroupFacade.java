package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.group.*;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.enums.dto.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.mapping.IDMGroupMapper;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapper;
import cz.muni.ics.kypo.userandgroup.service.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IDMGroupFacade {

    private final IDMGroupService groupService;
    private final UserService userService;
    private final IDMGroupMapper groupMapper;
    private final RoleMapper roleMapper;

    @Autowired
    public IDMGroupFacade(IDMGroupService groupService, UserService userService, RoleMapper roleMapper, IDMGroupMapper groupMapper) {
        this.groupService = groupService;
        this.userService = userService;
        this.groupMapper = groupMapper;
        this.roleMapper = roleMapper;
    }

    @IsAdmin
    @TransactionalWO
    public GroupDTO createGroup(NewGroupDTO newGroupDTO) {
        IDMGroup group = groupMapper.mapCreateToEntity(newGroupDTO);
        IDMGroup createdGroup = groupService.createIDMGroup(group, newGroupDTO.getGroupIdsOfImportedUsers());
        return groupMapper.mapToDTO(createdGroup);

    }

    @IsAdmin
    @TransactionalWO
    public void updateGroup(UpdateGroupDTO updateGroupDTO) {
        groupService.updateIDMGroup(groupMapper.mapUpdateToEntity(updateGroupDTO));

    }

    @IsAdmin
    @TransactionalWO
    public void removeUsers(Long groupId, List<Long> userIds) {
        IDMGroup groupToUpdate = groupService.getGroupById(groupId);
        List<User> users = userService.getUsersByIds(userIds);
        for (User user : users) {
            groupService.removeUserFromGroup(groupToUpdate, user);
        }
        groupService.updateIDMGroup(groupToUpdate);
    }

    @IsAdmin
    @TransactionalWO
    public void addUsersToGroup(Long groupId, AddUsersToGroupDTO addUsers) {
        IDMGroup groupToUpdate = groupService.getGroupById(groupId);
        addUsersWithIdsToGroup(groupToUpdate, addUsers.getIdsOfUsersToBeAdd());
        importUsersFromGroupsToGroup(groupToUpdate, addUsers.getIdsOfGroupsOfImportedUsers());
        groupService.updateIDMGroup(groupToUpdate);
    }

    private void addUsersWithIdsToGroup(IDMGroup group, List<Long> idsOfUsers) {
        if (!idsOfUsers.isEmpty()) {
            List<User> users = userService.getUsersByIds(idsOfUsers);
            for (User user : users) {
                group.addUser(user);
                groupService.evictUserFromCache(user);
            }
        }
    }

    private void importUsersFromGroupsToGroup(IDMGroup group, List<Long> idsOfGroups) {
        if (!idsOfGroups.isEmpty()) {
            List<IDMGroup> groups = groupService.getGroupsByIds(idsOfGroups);
            for (IDMGroup groupOfImportedMembers : groups) {
                groupOfImportedMembers.getUsers().forEach(group::addUser);
            }
        }
    }

    @IsAdmin
    @TransactionalWO
    public void deleteGroup(Long id) {
        IDMGroup group = groupService.getGroupById(id);
        groupService.deleteIDMGroup(group);
    }

    @IsAdmin
    @TransactionalWO
    public void deleteGroups(List<Long> ids) {
        List<IDMGroup> groupsToBeDeleted = groupService.getGroupsByIds(ids);
        groupsToBeDeleted.forEach(group -> {
            groupService.deleteIDMGroup(group);
        });
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<GroupViewDTO> getAllGroups(Predicate predicate, Pageable pageable) {
        PageResultResource<GroupViewDTO> groups = groupMapper.mapToPageResultResource(groupService.getAllIDMGroups(predicate, pageable));
        groups.getContent().forEach(groupViewDTO -> {
            if (getListOfImplicitGroups().contains(groupViewDTO.getName())) {
                groupViewDTO.setCanBeDeleted(false);
            }
        });
        return groups;
    }

    private List<String> getListOfImplicitGroups() {
        return List.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_USER.getName());
    }

    @IsAdmin
    @TransactionalRO
    public GroupDTO getGroupById(Long id) {
        GroupDTO groupDTO = groupMapper.mapToDTO(groupService.getGroupById(id));
        if (getListOfImplicitGroups().contains(groupDTO.getName())) {
            groupDTO.setCanBeDeleted(false);
        }
        return groupDTO;
    }

    public GroupWithRolesDTO getIDMGroupWithRolesByName(String groupName) {
        return groupMapper.mapToWithRolesDto(groupService.getIDMGroupWithRolesByName(groupName));
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<RoleDTO> getRolesOfGroup(Long id, Pageable pageable, Predicate predicate) {
        Page<Role> rolePage = groupService.getRolesOfGroup(id, pageable, predicate);
        return new PageResultResource<>(rolePage.map(role -> roleMapper.mapToRoleDTOWithMicroservice(role)).getContent(), roleMapper.createPagination(rolePage));
    }

    @IsAdmin
    @TransactionalWO
    public void assignRole(Long groupId, Long roleId) {
        IDMGroup idmGroup = groupService.assignRole(groupId, roleId);
        idmGroup.getUsers().forEach(user -> groupService.evictUserFromCache(user));
    }

    @IsAdmin
    @TransactionalWO
    public void removeRoleFromGroup(Long groupId, Long roleId) {
        IDMGroup idmGroup = groupService.removeRoleFromGroup(groupId, roleId);
        idmGroup.getUsers().forEach(user -> groupService.evictUserFromCache(user));
    }
}
