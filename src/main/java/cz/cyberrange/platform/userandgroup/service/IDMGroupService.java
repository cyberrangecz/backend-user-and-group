package cz.cyberrange.platform.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Business logic for creating, updating, deleting and querying groups, and for managing which roles
 * and users belong to them.
 */
@Service
public class IDMGroupService {

  private final IDMGroupRepository groupRepository;
  private final RoleRepository roleRepository;
  private final SecurityService securityService;

  @Autowired
  public IDMGroupService(
      IDMGroupRepository idmGroupRepository,
      RoleRepository roleRepository,
      SecurityService securityService) {
    this.groupRepository = idmGroupRepository;
    this.roleRepository = roleRepository;
    this.securityService = securityService;
  }

  /**
   * Returns the group with the given id.
   *
   * @param id id of the group
   * @return the matching group
   * @throws EntityNotFoundException when no group has that id
   */
  public IDMGroup getGroupById(Long id) {
    return groupRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(IDMGroup.class, "id", id.getClass(), id)));
  }

  /**
   * Returns every group whose id is in the given list.
   *
   * @param groupIds ids to match
   * @return the matching groups; empty list when none match
   */
  public List<IDMGroup> getGroupsByIds(List<Long> groupIds) {
    return groupRepository.findByIdIn(groupIds);
  }

  /**
   * Returns the group that holds the default role of every microservice.
   *
   * @return the matching group
   * @throws EntityNotFoundException when that group does not exist
   */
  public IDMGroup getGroupForDefaultRoles() {
    return groupRepository
        .findByName(ImplicitGroupNames.DEFAULT_GROUP.getName())
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        IDMGroup.class,
                        "name",
                        String.class,
                        ImplicitGroupNames.DEFAULT_GROUP.getName())));
  }

  /**
   * Creates the given group, adding the members of each given group as its own members. The group's
   * status is always set to valid, regardless of the value carried by the given group.
   *
   * @param group group to create
   * @param groupIdsOfImportedMembers ids of the groups whose users are copied into the new group
   * @return the persisted group
   * @throws EntityConflictException when a group with that name already exists
   */
  public IDMGroup createIDMGroup(IDMGroup group, List<Long> groupIdsOfImportedMembers) {
    group.setStatus(UserAndGroupStatus.VALID);
    if (groupRepository.existsByName(group.getName())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              IDMGroup.class,
              "name",
              String.class,
              group.getName(),
              "Group with name '" + group.getName() + "' already exists."));
    }

    if (!groupIdsOfImportedMembers.isEmpty()) {
      Set<User> importedMembersFromGroups =
          groupRepository.findUsersOfGivenGroups(groupIdsOfImportedMembers);
      for (User importedUserFromGroup : importedMembersFromGroups) {
        group.addUser(importedUserFromGroup);
      }
    }
    return groupRepository.save(group);
  }

  /**
   * Updates the name, description and expiration date of the group with the given id, leaving its
   * other fields unchanged.
   *
   * @param group group carrying the id to update and the new name, description and expiration date
   * @return the updated group
   * @throws EntityNotFoundException when no group has that id
   * @throws EntityConflictException when the group is one of the groups created automatically by
   *     the service and its name is being changed
   */
  public IDMGroup updateIDMGroup(IDMGroup group) {
    IDMGroup groupInDatabase = getGroupById(group.getId());
    if (getImplicitGroupNames().contains(groupInDatabase.getName())
        && !groupInDatabase.getName().equals(group.getName())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              IDMGroup.class,
              "id",
              group.getId().getClass(),
              group.getId(),
              "Name of main group cannot be changed"));
    }
    groupInDatabase.setDescription(group.getDescription());
    groupInDatabase.setName(group.getName());
    groupInDatabase.setExpirationDate(group.getExpirationDate());
    return groupRepository.save(groupInDatabase);
  }

  /**
   * Deletes the given group.
   *
   * @param group group to delete
   * @throws EntityConflictException when the group is one of the groups created automatically by
   *     the service
   * @throws EntityConflictException when the group has any users
   */
  public void deleteIDMGroup(IDMGroup group) {
    if (getImplicitGroupNames().contains(group.getName())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              IDMGroup.class,
              "name",
              String.class,
              group.getName(),
              "This group is User and Group default group that cannot be deleted."));
    }
    if (group.getUsers() == null || !group.getUsers().isEmpty())
      throw new EntityConflictException(
          new EntityErrorDetail(
              IDMGroup.class,
              "id",
              Long.class,
              group.getId(),
              "The group must be empty (without users) before it is deleted."));
    groupRepository.delete(group);
  }

  /**
   * Returns every group that matches the given predicate.
   *
   * @param predicate filter applied to the groups
   * @param pageable page and sort request
   * @return the matching page of groups; empty page when none match
   */
  public Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable) {
    return groupRepository.findAll(predicate, pageable);
  }

  /**
   * Returns the group with the given name.
   *
   * @param name name of the group
   * @return the matching group
   * @throws EntityNotFoundException when no group has that name
   */
  public IDMGroup getIDMGroupByName(String name) {
    return groupRepository
        .findByName(name)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(IDMGroup.class, "name", name.getClass(), name)));
  }

  /**
   * Returns the group with the given name, provided it has at least one role, with its roles
   * loaded.
   *
   * @param name name of the group
   * @return the matching group
   * @throws EntityNotFoundException when no group with that name has any role
   */
  public IDMGroup getIDMGroupWithRolesByName(String name) {
    return groupRepository
        .findByNameWithRoles(name)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(IDMGroup.class, "name", name.getClass(), name)));
  }

  /**
   * Returns the roles assigned to the group with the given id.
   *
   * @param groupId id of the group
   * @param pageable page and sort request
   * @param predicate optional filter further applied to the roles
   * @return the matching page of roles; empty page when none match
   * @throws EntityNotFoundException when no group has that id
   */
  public Page<Role> getRolesOfGroup(Long groupId, Pageable pageable, Predicate predicate) {
    if (!groupRepository.existsById(groupId)) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              IDMGroup.class, "id", groupId.getClass(), groupId, "Group not found."));
    }
    return this.roleRepository.findAllOfGroup(groupId, pageable, predicate);
  }

  /**
   * Assigns the role with the given id to the group with the given id.
   *
   * @param groupId id of the group
   * @param roleId id of the role
   * @return the updated group
   * @throws EntityNotFoundException when no group has that id, or no role has that id
   */
  public IDMGroup assignRole(Long groupId, Long roleId) {
    IDMGroup group = this.getGroupById(groupId);
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            Role.class,
                            "id",
                            roleId.getClass(),
                            roleId,
                            "Role not found. Start up of the project or registering of microservice probably went wrong, please contact support.")));
    group.addRole(role);
    return groupRepository.save(group);
  }

  /**
   * Removes the role with the given id from the group with the given id.
   *
   * @param groupId id of the group
   * @param roleId id of the role
   * @return the updated group
   * @throws EntityNotFoundException when no group has that id, or the group does not have a role
   *     with that id
   * @throws EntityConflictException when the role is the main role of one of the groups created
   *     automatically by the service: the trainee role of the default group, the administrator role
   *     of the administrator group, or the power user role of the power user group
   */
  public IDMGroup removeRoleFromGroup(Long groupId, Long roleId) {
    IDMGroup group = this.getGroupById(groupId);

    for (Role role : group.getRoles()) {
      if (role.getId().equals(roleId)) {
        checkIfCanBeRemoved(group.getName(), role.getRoleType());
        group.removeRole(role);
        return groupRepository.save(group);
      }
    }
    throw new EntityNotFoundException(
        new EntityErrorDetail(
            Role.class, "id", roleId.getClass(), roleId, "Role was not found in group."));
  }

  /**
   * Blocks removal of a group's main role: the trainee role of the default group, the administrator
   * role of the administrator group, or the power user role of the power user group.
   *
   * @param groupName name of the group the role is being removed from
   * @param roleType role type of the role being removed
   * @throws EntityConflictException when the role is that group's main role
   */
  private void checkIfCanBeRemoved(String groupName, String roleType) {
    if (groupName.equals(ImplicitGroupNames.DEFAULT_GROUP.getName())
            && roleType.equals(RoleType.ROLE_USER_AND_GROUP_TRAINEE.name())
        || groupName.equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())
            && roleType.equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name())
        || groupName.equals(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName())
            && roleType.equals(RoleType.ROLE_USER_AND_GROUP_POWER_USER.name())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              Role.class,
              "roleType",
              String.class,
              roleType,
              "Main role of the group cannot be removed."));
    }
  }

  /**
   * Removes the given user from the given group.
   *
   * @param groupToUpdate group to remove the user from
   * @param user user to remove
   * @throws EntityConflictException when the group is the administrator group and the user is the
   *     user authenticated for the current request
   * @throws EntityConflictException when the group holds the default role of every microservice
   */
  //    @CacheEvict(value = AbstractCacheNames.USERS_CACHE_NAME, key = "{#user.sub+#user.iss}")
  public void removeUserFromGroup(IDMGroup groupToUpdate, User user) {
    if (groupToUpdate.getName().equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())
        && securityService.hasLoggedInUserSameLogin(user.getSub())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              IDMGroup.class,
              "name",
              String.class,
              groupToUpdate.getName(),
              "An administrator cannot remove himself from the administrator group."));
    }
    if (groupToUpdate.getName().equals(ImplicitGroupNames.DEFAULT_GROUP.getName())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              IDMGroup.class,
              "name",
              String.class,
              groupToUpdate.getName(),
              "Cannot remove user(s) from default group."));
    }
    groupToUpdate.removeUser(user);
  }

  /**
   * Adds the given user to the given group.
   *
   * @param groupToUpdate group to add the user to
   * @param userToBeAdded user to add
   * @return the updated group
   */
  //    @CacheEvict(value = AbstractCacheNames.USERS_CACHE_NAME, key =
  // "{#userToBeAdded.sub+#userToBeAdded.iss}")
  public IDMGroup addUserToGroup(IDMGroup groupToUpdate, User userToBeAdded) {
    groupToUpdate.addUser(userToBeAdded);
    return groupRepository.save(groupToUpdate);
  }

  private List<String> getImplicitGroupNames() {
    return List.of(
        ImplicitGroupNames.DEFAULT_GROUP.getName(),
        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(),
        ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName());
  }

  /**
   * Has no effect.
   *
   * @param user unused
   */
  //    @CacheEvict(AbstractCacheNames.USERS_CACHE_NAME, key = "{#user.sub+#user.iss}")
  public void evictUserFromCache(User user) {
    // evicting user from cache
  }
}
