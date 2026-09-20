package cz.cyberrange.platform.userandgroup.rest.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.group.AddUsersToGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupViewDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupWithRolesDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.mapping.IDMGroupMapper;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapper;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdmin;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalWO;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operations for creating, updating and deleting groups, and for managing the users and roles
 * assigned to each one.
 */
@Service
@Transactional
public class IDMGroupFacade {

  private final IDMGroupService groupService;
  private final UserService userService;
  private final IDMGroupMapper groupMapper;
  private final RoleMapper roleMapper;

  @Autowired
  public IDMGroupFacade(
      IDMGroupService groupService,
      UserService userService,
      RoleMapper roleMapper,
      IDMGroupMapper groupMapper) {
    this.groupService = groupService;
    this.userService = userService;
    this.groupMapper = groupMapper;
    this.roleMapper = roleMapper;
  }

  /**
   * Creates a group from the given data, copying the members of each group referenced by its
   * imported-user group ids into the new group. The returned group leaves its source unset and its
   * deletion flag at the default value of true.
   *
   * @param newGroupDTO data for the group to create, including the ids of the groups whose members
   *     are copied in
   * @return the created group
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     a group with that name already exists
   */
  @IsAdmin
  @TransactionalWO
  public GroupDTO createGroup(NewGroupDTO newGroupDTO) {
    IDMGroup group = groupMapper.mapCreateToEntity(newGroupDTO);
    IDMGroup createdGroup =
        groupService.createIDMGroup(group, newGroupDTO.getGroupIdsOfImportedUsers());
    return groupMapper.mapToDTO(createdGroup);
  }

  /**
   * Updates the name, description and expiration date of the group with the id carried by the given
   * data, leaving its other fields unchanged.
   *
   * @param updateGroupDTO id of the group to update, together with its new name, description and
   *     expiration date
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     the group is one of the groups created automatically by the service and its name is being
   *     changed
   */
  @IsAdmin
  @TransactionalWO
  public void updateGroup(UpdateGroupDTO updateGroupDTO) {
    groupService.updateIDMGroup(groupMapper.mapUpdateToEntity(updateGroupDTO));
  }

  /**
   * Removes each given user from the group with the given id. User ids that match no user are
   * ignored.
   *
   * @param groupId id of the group to update
   * @param userIds ids of the users to remove
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     the group is the administrator group and a user to remove is the user authenticated for the
   *     current request
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     the group holds the default role of every microservice
   */
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

  /**
   * Adds each user with the given ids, and every member of each group with the given ids, to the
   * group with the given id. Ids that match no user or no group are ignored.
   *
   * @param groupId id of the group to update
   * @param addUsers ids of the users to add directly, and ids of the groups whose members are
   *     copied in
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id
   */
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

  /**
   * Deletes the group with the given id.
   *
   * @param id id of the group to delete
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     the group is one of the groups created automatically by the service
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     the group has any users
   */
  @IsAdmin
  @TransactionalWO
  public void deleteGroup(Long id) {
    IDMGroup group = groupService.getGroupById(id);
    groupService.deleteIDMGroup(group);
  }

  /**
   * Deletes each group with the given ids. Ids that match no group are ignored.
   *
   * @param ids ids of the groups to delete
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     any of the groups is one of the groups created automatically by the service, or has any
   *     users
   */
  @IsAdmin
  @TransactionalWO
  public void deleteGroups(List<Long> ids) {
    List<IDMGroup> groupsToBeDeleted = groupService.getGroupsByIds(ids);
    groupsToBeDeleted.forEach(groupService::deleteIDMGroup);
  }

  /**
   * Returns every group that matches the given predicate, as basic group views. Each of the groups
   * created automatically by the service has its deletion flag set to false; every other view
   * leaves it at the default value of true. Every returned view leaves its source unset.
   *
   * @param predicate filter applied to the groups
   * @param pageable page and sort request
   * @return the matching page of group views; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<GroupViewDTO> getAllGroups(Predicate predicate, Pageable pageable) {
    PageResultResource<GroupViewDTO> groups =
        groupMapper.mapToPageResultResource(groupService.getAllIDMGroups(predicate, pageable));
    groups
        .getContent()
        .forEach(
            groupViewDTO -> {
              if (getListOfImplicitGroups().contains(groupViewDTO.getName())) {
                groupViewDTO.setCanBeDeleted(false);
              }
            });
    return groups;
  }

  private List<String> getListOfImplicitGroups() {
    return List.of(
        ImplicitGroupNames.DEFAULT_GROUP.getName(),
        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(),
        ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName());
  }

  /**
   * Returns the group with the given id. The returned group has its deletion flag set to false when
   * it is one of the groups created automatically by the service, and left at the default value of
   * true otherwise; its source stays unset.
   *
   * @param id id of the group
   * @return the matching group
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id
   */
  @IsAdmin
  @TransactionalRO
  public GroupDTO getGroupById(Long id) {
    GroupDTO groupDTO = groupMapper.mapToDTO(groupService.getGroupById(id));
    if (getListOfImplicitGroups().contains(groupDTO.getName())) {
      groupDTO.setCanBeDeleted(false);
    }
    return groupDTO;
  }

  /**
   * Returns the group with the given name, provided it has at least one role, together with its
   * roles and each role's microservice id and name. The returned group leaves its source unset and
   * its deletion flag at the default value of true.
   *
   * @param groupName name of the group
   * @return the matching group with its roles
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group with that name has any role
   */
  public GroupWithRolesDTO getIDMGroupWithRolesByName(String groupName) {
    return groupMapper.mapToWithRolesDto(groupService.getIDMGroupWithRolesByName(groupName));
  }

  /**
   * Returns the roles assigned to the group with the given id, with each role's microservice id and
   * name filled in.
   *
   * @param id id of the group
   * @param pageable page and sort request
   * @param predicate optional filter further applied to the roles
   * @return the matching page of roles; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<RoleDTO> getRolesOfGroup(
      Long id, Pageable pageable, Predicate predicate) {
    Page<Role> rolePage = groupService.getRolesOfGroup(id, pageable, predicate);
    return new PageResultResource<>(
        rolePage.map(role -> roleMapper.mapToRoleDTOWithMicroservice(role)).getContent(),
        roleMapper.createPagination(rolePage));
  }

  /**
   * Assigns the role with the given id to the group with the given id.
   *
   * @param groupId id of the group
   * @param roleId id of the role
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id, or no role has that id
   */
  @IsAdmin
  @TransactionalWO
  public void assignRole(Long groupId, Long roleId) {
    IDMGroup idmGroup = groupService.assignRole(groupId, roleId);
    idmGroup.getUsers().forEach(user -> groupService.evictUserFromCache(user));
  }

  /**
   * Removes the role with the given id from the group with the given id.
   *
   * @param groupId id of the group
   * @param roleId id of the role
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no group has that id, or the group does not have a role with that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     the role is the main role of one of the groups created automatically by the service: the
   *     trainee role of the default group, the administrator role of the administrator group, or
   *     the power user role of the power user group
   */
  @IsAdmin
  @TransactionalWO
  public void removeRoleFromGroup(Long groupId, Long roleId) {
    IDMGroup idmGroup = groupService.removeRoleFromGroup(groupId, roleId);
    idmGroup.getUsers().forEach(user -> groupService.evictUserFromCache(user));
  }
}
