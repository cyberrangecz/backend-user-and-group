package cz.cyberrange.platform.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.FileNotFoundException;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.QUser;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Business logic for creating, updating, deleting and querying users and their group and role
 * membership.
 */
@Service
public class UserService {

  private static final Logger LOG = LoggerFactory.getLogger(UserService.class.getName());

  private final UserRepository userRepository;
  private final IDMGroupRepository groupRepository;
  private final RoleRepository roleRepository;

  @Autowired
  public UserService(
      UserRepository userRepository,
      IDMGroupRepository groupRepository,
      RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.roleRepository = roleRepository;
  }

  /**
   * Returns the user with the given id.
   *
   * @param id id of the user
   * @return the matching user
   * @throws EntityNotFoundException when no user has that id
   */
  public User getUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(User.class, "id", id.getClass(), id)));
  }

  /**
   * Returns the user with the given sub and issuer.
   *
   * @param sub subject identifier to match
   * @param iss issuer to match
   * @return the matching user, or an empty Optional when no user has that sub and issuer
   */
  public Optional<User> getUserBySubAndIss(String sub, String iss) {
    return userRepository.findBySubAndIss(sub, iss);
  }

  /**
   * Returns every user that matches the given predicate.
   *
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  public Page<User> getAllUsers(Predicate predicate, Pageable pageable) {
    return userRepository.findAll(predicate, pageable);
  }

  /**
   * Returns every user whose id is in the given list and that also matches the given predicate.
   *
   * @param ids ids to match
   * @param pageable page and sort request
   * @param predicate further filter applied to the users
   * @return the matching page of users; empty page when none match
   */
  public Page<User> getUsersWithGivenIds(List<Long> ids, Pageable pageable, Predicate predicate) {
    Predicate finalPredicate = QUser.user.id.in(ids).and(predicate);
    return userRepository.findAll(finalPredicate, pageable);
  }

  /**
   * Returns every user whose id is in the given list.
   *
   * @param userIds ids to match
   * @return the matching users; empty list when none match
   */
  public List<User> getUsersByIds(List<Long> userIds) {
    return userRepository.findByIdIn(userIds);
  }

  /**
   * Deletes the given user, first removing it from each of its groups.
   *
   * @param user user to delete
   */
  public void deleteUser(User user) {
    for (IDMGroup group : user.getGroups()) {
      group.removeUser(user);
    }
    userRepository.delete(user);
    LOG.debug("IDM user with id: {} was successfully deleted.", user.getId());
  }

  /**
   * Persists the given user.
   *
   * @param user user to create
   * @return the persisted user
   */
  public User createUser(User user) {
    return userRepository.saveAndFlush(user);
  }

  /**
   * Persists every given user.
   *
   * @param users users to create
   * @return the persisted users
   */
  public List<User> createUsers(Set<User> users) {
    return userRepository.saveAllAndFlush(users);
  }

  /**
   * Persists the given user's current field values.
   *
   * @param user user with the values to persist
   * @return the persisted user
   */
  public User updateUser(User user) {
    return userRepository.saveAndFlush(user);
  }

  /**
   * Toggles the membership of the user with the given id in the administrator group: removes the
   * user when already a member, adds it otherwise.
   *
   * @param id id of the user
   * @throws EntityNotFoundException when no user has that id, or the administrator group does not
   *     exist
   */
  public void changeAdminRole(Long id) {
    User user = this.getUserById(id);
    Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
    IDMGroup administratorGroup =
        optionalAdministratorGroup.orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        IDMGroup.class,
                        "name",
                        String.class,
                        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())));
    if (user.getGroups().contains(administratorGroup)) {
      administratorGroup.removeUser(user);
    } else {
      administratorGroup.addUser(user);
    }
    userRepository.save(user);
  }

  /**
   * Checks whether the user with the given id belongs to the administrator group.
   *
   * @param id id of the user
   * @return true when the user belongs to the administrator group, false otherwise
   * @throws EntityNotFoundException when no user has that id, or the administrator group does not
   *     exist
   */
  public boolean isUserAdmin(Long id) {
    User user = getUserById(id);
    Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
    IDMGroup administratorGroup =
        optionalAdministratorGroup.orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        IDMGroup.class,
                        "name",
                        String.class,
                        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())));
    return user.getGroups().contains(administratorGroup);
  }

  /**
   * Returns every user, other than those with the given ids, assigned a role with the given role
   * type.
   *
   * @param roleType role type to match
   * @param userIds ids excluded from the result
   * @param predicate further filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  public Page<User> getUsersWithGivenRoleAndNotWithGivenIds(
      String roleType, Set<Long> userIds, Predicate predicate, Pageable pageable) {
    return userRepository.findAllByRoleAndNotWithIds(predicate, pageable, roleType, userIds);
  }

  /**
   * Returns every user that does not belong to the group with the given id.
   *
   * @param groupId id of the group to exclude
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  public Page<User> getAllUsersNotInGivenGroup(
      Long groupId, Predicate predicate, Pageable pageable) {
    return userRepository.usersNotInGivenGroup(groupId, predicate, pageable);
  }

  /**
   * Returns every user that belongs to any of the given groups.
   *
   * @param groupsIds ids of the groups to match
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  public Page<User> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
    return userRepository.usersInGivenGroups(groupsIds, predicate, pageable);
  }

  /**
   * Returns the user with the given id, with its groups loaded.
   *
   * @param id id of the user
   * @return the matching user
   * @throws EntityNotFoundException when no user has that id
   */
  public User getUserWithGroups(Long id) {
    return userRepository
        .getUserByIdWithGroups(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(User.class, "id", id.getClass(), id)));
  }

  /**
   * Returns the user with the given sub and issuer, with its groups loaded.
   *
   * @param sub subject identifier to match
   * @param iss issuer to match
   * @return the matching user
   * @throws EntityNotFoundException when no user has that sub and issuer
   */
  public User getUserWithGroups(String sub, String iss) {
    return userRepository
        .getUserBySubWithGroups(sub, iss)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(User.class, "sub", sub.getClass(), sub)));
  }

  /**
   * Returns every role the user with the given id holds through any of their groups.
   *
   * @param id id of the user
   * @return the matching roles; empty set when the user has none
   * @throws EntityNotFoundException when no user has that id
   */
  public Set<Role> getRolesOfUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new EntityNotFoundException(new EntityErrorDetail(User.class, "id", id.getClass(), id));
    }
    return userRepository.getRolesOfUser(id);
  }

  /**
   * Returns the roles assigned to a group the user with the given id belongs to.
   *
   * @param id id of the user
   * @param pageable page and sort request
   * @param predicate optional filter further applied to the roles
   * @return the matching page of roles; empty page when none match
   * @throws EntityNotFoundException when no user has that id
   */
  public Page<Role> getRolesOfUserWithPagination(Long id, Pageable pageable, Predicate predicate) {
    if (!userRepository.existsById(id)) {
      throw new EntityNotFoundException(new EntityErrorDetail(User.class, "id", id.getClass(), id));
    }
    return roleRepository.findAllOfUser(id, pageable, predicate);
  }

  /**
   * Returns every user assigned, through any of their groups, the role with the given id.
   *
   * @param roleId id of the role
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   * @throws EntityNotFoundException when no role has that id
   */
  public Page<User> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
    if (!roleRepository.existsById(roleId)) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(Role.class, "roleId", roleId.getClass(), roleId));
    }
    return userRepository.findAllByRoleId(roleId, predicate, pageable);
  }

  /**
   * Returns every user assigned, through any of their groups, a role with the given role type.
   *
   * @param roleType role type to match
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   * @throws EntityNotFoundException when no role has that type
   */
  public Page<User> getUsersWithGivenRoleType(
      String roleType, Predicate predicate, Pageable pageable) {
    if (!roleRepository.existsByRoleType(roleType)) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(Role.class, "roleType", roleType.getClass(), roleType));
    }
    return userRepository.findAllByRoleType(roleType, predicate, pageable);
  }

  /**
   * Returns the raw bytes of the file configured as the source of initial OIDC users.
   *
   * @return the file's bytes
   * @throws FileNotFoundException when the configured file does not exist, or reading it raises an
   *     IOException
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileCannotReadException when
   *     the configured file cannot be read
   */
  public byte[] getInitialOIDCUsers() {
    try {
      return userRepository.getInitialOIDCUsers();
    } catch (IOException e) {
      throw new FileNotFoundException(e);
    }
  }
}
