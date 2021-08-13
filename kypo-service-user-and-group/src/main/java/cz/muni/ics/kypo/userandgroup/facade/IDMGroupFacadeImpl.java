package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.IDMGroupMapper;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class IDMGroupFacadeImpl implements IDMGroupFacade {

    @Value("${service.name}")
    private String nameOfUserAndGroupService;

    private IDMGroupService groupService;
    private UserService userService;
    private IDMGroupMapper groupMapper;
    private RoleMapper roleMapper;

    @Autowired
    public IDMGroupFacadeImpl(IDMGroupService groupService, UserService userService, RoleMapper roleMapper, IDMGroupMapper groupMapper) {
        this.groupService = groupService;
        this.userService = userService;
        this.groupMapper = groupMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public GroupDTO createGroup(NewGroupDTO newGroupDTO) {
        IDMGroup group = groupMapper.mapCreateToEntity(newGroupDTO);
        IDMGroup createdGroup = groupService.createIDMGroup(group, newGroupDTO.getGroupIdsOfImportedUsers());
        return groupMapper.mapToDTO(createdGroup);

    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void updateGroup(UpdateGroupDTO updateGroupDTO) {
        groupService.updateIDMGroup(groupMapper.mapUpdateToEntity(updateGroupDTO));

    }

    @Override
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

    @Override
    public void addUser(Long groupId, Long userId) {
        IDMGroup groupToUpdate = groupService.getGroupById(groupId);
        User userToBeAdded = userService.getUserById(userId);
        groupService.addUserToGroup(groupToUpdate, userToBeAdded);
    }

    @Override
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
                groupOfImportedMembers.getUsers().forEach(user -> {
                    if (!group.getUsers().contains(user)) {
                        group.addUser(user);
                    }
                });
            }
        }
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void deleteGroup(Long id) {
        IDMGroup group = groupService.getGroupById(id);
        groupService.deleteIDMGroup(group);
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void deleteGroups(List<Long> ids) {
        List<IDMGroup> groupsToBeDeleted = groupService.getGroupsByIds(ids);
        groupsToBeDeleted.forEach(group -> {
            groupService.deleteIDMGroup(group);
        });
    }

    @Override
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

    @Override
    @IsAdmin
    @TransactionalRO
    public GroupDTO getGroupById(Long id) {
        GroupDTO groupDTO = groupMapper.mapToDTO(groupService.getGroupById(id));
        if (getListOfImplicitGroups().contains(groupDTO.getName())) {
            groupDTO.setCanBeDeleted(false);
        }
        return groupDTO;
    }

    @Override
    public GroupWithRolesDTO getIDMGroupWithRolesByName(String groupName) {
        return groupMapper.mapToWithRolesDto(groupService.getIDMGroupWithRolesByName(groupName));
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public PageResultResource<RoleDTO> getRolesOfGroup(Long id, Pageable pageable, Predicate predicate) {
        Page<Role> rolePage = groupService.getRolesOfGroup(id, pageable, predicate);
        return new PageResultResource<>(rolePage.map(role ->  roleMapper.mapToRoleDTOWithMicroservice(role)).getContent(), roleMapper.createPagination(rolePage));
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void assignRole(Long groupId, Long roleId) {
        IDMGroup idmGroup = groupService.assignRole(groupId, roleId);
        idmGroup.getUsers().forEach(user -> groupService.evictUserFromCache(user));
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void removeRoleFromGroup(Long groupId, Long roleId) {
        IDMGroup idmGroup = groupService.removeRoleFromGroup(groupId, roleId);
        idmGroup.getUsers().forEach(user -> groupService.evictUserFromCache(user));
    }
}
