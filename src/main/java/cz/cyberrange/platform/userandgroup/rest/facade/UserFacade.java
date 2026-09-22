package cz.cyberrange.platform.userandgroup.rest.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.UsersImportDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserBasicViewDto;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserCreateDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapper;
import cz.cyberrange.platform.userandgroup.api.mapping.UserMapper;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UagAccessForbiddenException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdmin;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdminOrPowerUser;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsTrainee;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalWO;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.IdenticonService;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import cz.cyberrange.platform.userandgroup.service.SecurityService;
import cz.cyberrange.platform.userandgroup.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Operations for querying, creating from OIDC login data, importing and deleting users, and for
 * managing the roles assigned to them.
 */
@Service
@Transactional
public class UserFacade {

  private static final int ICON_WIDTH = 75;
  private static final int ICON_HEIGHT = 75;

  private final UserService userService;
  private final IDMGroupService idmGroupService;
  private final SecurityService securityService;
  private final RoleService roleService;
  private final IdenticonService identiconService;
  private final UserMapper userMapper;
  private final RoleMapper roleMapper;

  @Autowired
  public UserFacade(
      UserService userService,
      IDMGroupService idmGroupService,
      SecurityService securityService,
      RoleService roleService,
      IdenticonService identiconService,
      UserMapper userMapper,
      RoleMapper roleMapper) {
    this.userService = userService;
    this.idmGroupService = idmGroupService;
    this.securityService = securityService;
    this.roleService = roleService;
    this.identiconService = identiconService;
    this.userMapper = userMapper;
    this.roleMapper = roleMapper;
  }

  /**
   * Returns every user that matches the given predicate, as basic views.
   *
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of basic user views; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<UserBasicViewDto> getUsers(Predicate predicate, Pageable pageable) {
    return userMapper.mapToPageUserBasicViewDto(userService.getAllUsers(predicate, pageable));
  }

  /**
   * Returns every user, other than those with the given ids, assigned a role with the given role
   * type, as basic views.
   *
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @param roleType role type to match
   * @param userIds ids excluded from the result
   * @return the matching page of basic user views; empty page when none match
   */
  @IsAdminOrPowerUser
  @TransactionalRO
  public PageResultResource<UserBasicViewDto> getUsers(
      Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds) {
    return userMapper.mapToPageUserBasicViewDto(
        userService.getUsersWithGivenRoleAndNotWithGivenIds(
            roleType, userIds, predicate, pageable));
  }

  /**
   * Returns every user whose id is in the given list and that matches the given predicate, as basic
   * views. A caller without the administrator or power user authority receives every other user's
   * full name, sub, given name, family name and mail replaced by a placeholder; the caller's own
   * record, and every user's id, issuer and picture, stay unmasked.
   *
   * @param ids ids to match
   * @param pageable page and sort request
   * @param predicate further filter applied to the users
   * @return the matching page of basic user views; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     the caller lacks the administrator or power user authority and no user matches the current
   *     request's sub and issuer
   */
  @IsTrainee
  @TransactionalRO
  public PageResultResource<UserBasicViewDto> getUsersWithGivenIds(
      List<Long> ids, Pageable pageable, Predicate predicate) {
    if (securityService.canRetrieveAnyInformation()) {
      return userMapper.mapToPageUserBasicViewDto(
          userService.getUsersWithGivenIds(ids, pageable, predicate));
    }
    return userMapper.mapToPageUserBasicViewDTOAnonymize(
        userService.getUsersWithGivenIds(ids, pageable, predicate),
        securityService.getLoggedInUser().getId());
  }

  /**
   * Returns the user with the given id, with the roles held through any of their groups filled in.
   *
   * @param id id of the user
   * @return the matching user
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no user has that id, or when no user matches the current request's sub and issuer
   * @throws UagAccessForbiddenException when the caller lacks the administrator or power user
   *     authority and is retrieving information about a user other than itself
   */
  @IsTrainee
  @TransactionalRO
  public UserDTO getUserById(Long id) {
    if (securityService.canRetrieveAnyInformation()) {
      return userMapper.mapToUserDTOWithRoles(userService.getUserById(id));
    }

    User loggedInUser = securityService.getLoggedInUser();
    // user can always retrieve himself
    if (Objects.equals(loggedInUser.getId(), id)) {
      return userMapper.mapToUserDTOWithRoles(loggedInUser);
    }
    throw new UagAccessForbiddenException(
        "Cannot retrieve information about another user with current authorization.");
  }

  /**
   * Returns the user authenticated for the current request, with the roles held through any of
   * their groups filled in.
   *
   * @return the authenticated user
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no user matches the current request's sub and issuer
   */
  @IsTrainee
  @TransactionalRO
  public UserDTO getUserInfo() {
    return userMapper.mapToUserDTOWithRoles(securityService.getLoggedInUser());
  }

  /**
   * Finds the user matching the given login data by subject and issuer, creating one when none
   * matches. An existing user has its name and mail fields overwritten with the given values when
   * any of them differ; its picture is left unchanged. A newly created user is given a generated
   * identicon picture derived from its subject and issuer in place of any picture carried by the
   * given data, and is added to the group that holds every microservice's default role. The
   * returned user carries every role held through any of its groups, with each role's microservice
   * id and name filled in.
   *
   * @param oidcUserDTO login data of the user to find, update or create
   * @return the matching, updated or newly created user
   * @throws IllegalArgumentException when oidcUserDTO is null
   * @throws UnprocessableEntityException when the given data carries no given name, full name or
   *     family name
   */
  // if creation of user fail because of DataIntegrityViolationException, method is repeated one
  // more time which cause that user is updated not created
  @Retryable(
      value = {DataIntegrityViolationException.class},
      maxAttempts = 2)
  public UserDTO createOrUpdateOrGetOIDCUser(UserCreateDTO oidcUserDTO) {
    Assert.notNull(
        oidcUserDTO,
        "In method createOrUpdateOrGetOIDCUser(userInfo) the input userInfo must not be null.");

    if (oidcUserDTO.getGivenName() == null
        || oidcUserDTO.getFullName() == null
        || oidcUserDTO.getFamilyName() == null) {
      throw new UnprocessableEntityException(
          new EntityErrorDetail(UserCreateDTO.class, "User must provide access to their name."));
    }

    Optional<User> user =
        userService.getUserBySubAndIss(oidcUserDTO.getSub(), oidcUserDTO.getIss());
    if (user.isPresent()) {
      return this.updateExistingUserInfo(user.get(), oidcUserDTO);
    } else {
      UserDTO userDTO = this.createNewUserWithDefaultGroupRoles(oidcUserDTO);
      userDTO.setRoles(
          roleMapper.mapToSetDTO(
              idmGroupService
                  .getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName())
                  .getRoles()));
      return userDTO;
    }
  }

  private UserDTO createNewUserWithDefaultGroupRoles(UserCreateDTO oidcUserDTO) {
    User userToCreate = userMapper.mapToEntity(oidcUserDTO);
    userToCreate.setPicture(
        identiconService.generateIdenticons(
            oidcUserDTO.getSub() + oidcUserDTO.getIss(), ICON_WIDTH, ICON_HEIGHT));

    User newlyCreatedUser = userService.createUser(userToCreate);
    idmGroupService
        .getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName())
        .addUser(newlyCreatedUser);
    return userMapper.mapToDTO(newlyCreatedUser);
  }

  private UserDTO updateExistingUserInfo(User user, UserCreateDTO oidcUserDTO) {
    if (user.getFullName() == null
        || !user.getFullName().equals(oidcUserDTO.getFullName())
        || user.getGivenName() == null
        || !user.getGivenName().equals(oidcUserDTO.getGivenName())
        || user.getFamilyName() == null
        || !user.getFamilyName().equals(oidcUserDTO.getFamilyName())
        || user.getMail() == null
        || !user.getMail().equals(oidcUserDTO.getMail())) {

      user.setGivenName(oidcUserDTO.getGivenName());
      user.setFamilyName(oidcUserDTO.getFamilyName());
      user.setFullName(oidcUserDTO.getFullName());
      user.setMail(oidcUserDTO.getMail());
      userService.updateUser(user);
    }
    UserDTO updatedUser = userMapper.mapToDTO(user);
    updatedUser.setRoles(
        roleMapper.mapToSetDTO(
            user.getGroups().stream()
                .flatMap(group -> group.getRoles().stream())
                .collect(Collectors.toCollection(HashSet::new))));
    return updatedUser;
  }

  /**
   * Deletes the user with the given id, first removing it from each of its groups.
   *
   * @param id id of the user to delete
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no user has that id
   */
  @IsAdmin
  @TransactionalWO
  public void deleteUser(Long id) {
    User user = userService.getUserById(id);
    userService.deleteUser(user);
  }

  /**
   * Returns every user that does not belong to the group with the given id, with each user's roles
   * filled in from every group it belongs to and each role's microservice id and name filled in.
   *
   * @param groupId id of the group to exclude
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(
      Long groupId, Predicate predicate, Pageable pageable) {
    PageResultResource<UserDTO> users =
        userMapper.mapToPageResultResource(
            userService.getAllUsersNotInGivenGroup(groupId, predicate, pageable));
    List<UserDTO> usersWithRoles =
        users.getContent().stream()
            .map(
                userDTO -> {
                  userDTO.setRoles(this.getRolesOfUser(userDTO.getId()));
                  return userDTO;
                })
            .collect(Collectors.toCollection(ArrayList::new));
    users.setContent(usersWithRoles);
    return users;
  }

  /**
   * Returns every user that belongs to any of the given groups.
   *
   * @param groupsIds ids of the groups to match
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<UserForGroupsDTO> getUsersInGroups(
      Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
    return userMapper.mapToPageResultResourceForGroups(
        userService.getUsersInGroups(groupsIds, predicate, pageable));
  }

  /**
   * Deletes each user with the given ids, first removing each one from its groups. Ids that match
   * no user are ignored.
   *
   * @param userIds ids of the users to delete
   */
  @IsAdmin
  @TransactionalWO
  public void deleteUsers(List<Long> userIds) {
    List<User> usersToBeDeleted = userService.getUsersByIds(userIds);
    usersToBeDeleted.forEach(userService::deleteUser);
  }

  /**
   * Returns every role the user with the given id holds through any of their groups, with each
   * role's microservice id and name filled in.
   *
   * @param id id of the user
   * @return the matching roles; empty set when the user has none
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no user has that id
   */
  @IsAdminOrPowerUser
  @TransactionalRO
  public Set<RoleDTO> getRolesOfUser(Long id) {
    Set<Role> roles = userService.getRolesOfUser(id);
    return roles.stream()
        .map(roleMapper::mapToRoleDTOWithMicroservice)
        .collect(Collectors.toCollection(HashSet::new));
  }

  /**
   * Returns the roles assigned to a group the user with the given id belongs to, with each role's
   * microservice id and name filled in.
   *
   * @param id id of the user
   * @param pageable page and sort request
   * @param predicate optional filter further applied to the roles
   * @return the matching page of roles; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no user has that id
   */
  @IsAdminOrPowerUser
  @TransactionalRO
  public PageResultResource<RoleDTO> getRolesOfUserWithPagination(
      Long id, Pageable pageable, Predicate predicate) {
    Page<Role> rolePage = userService.getRolesOfUserWithPagination(id, pageable, predicate);
    return new PageResultResource<>(
        rolePage.map(roleMapper::mapToRoleDTOWithMicroservice).getContent(),
        roleMapper.createPagination(rolePage));
  }

  /**
   * Returns every user assigned, through any of their groups, the role with the given id, with each
   * user's roles filled in from every group it belongs to and each role's microservice id and name
   * filled in.
   *
   * @param roleId id of the role
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no role has that id
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<UserDTO> getUsersWithGivenRole(
      Long roleId, Predicate predicate, Pageable pageable) {
    return userMapper.mapToPageResultResource(
        userService.getUsersWithGivenRole(roleId, predicate, pageable));
  }

  /**
   * Returns every user assigned, through any of their groups, a role with the given role type, with
   * each user's roles filled in from every group it belongs to and each role's microservice id and
   * name filled in.
   *
   * @param roleType role type to match
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no role has that type
   */
  @IsAdminOrPowerUser
  @TransactionalRO
  public PageResultResource<UserDTO> getUsersWithGivenRoleType(
      String roleType, Predicate predicate, Pageable pageable) {
    return userMapper.mapToPageResultResource(
        userService.getUsersWithGivenRoleType(roleType, predicate, pageable));
  }

  /**
   * Returns the raw bytes of the file configured as the source of initial OIDC users.
   *
   * @return the file's bytes
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileNotFoundException when
   *     the configured file does not exist, or reading it raises an IOException
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileCannotReadException when
   *     the configured file cannot be read
   */
  @IsAdmin
  public byte[] getInitialOIDCUsers() {
    return userService.getInitialOIDCUsers();
  }

  /**
   * Creates the users carried by the given data and adds each one to the group that holds every
   * microservice's default role, and also to a new group with the given name when one is given. Two
   * entries with the same subject and issuer collapse into a single created user. Each created user
   * leaves its external id and mail unset, and is given a generated identicon picture derived from
   * its subject and issuer.
   *
   * @param usersImportDTO users to import, and the optional name of a group to add them to
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException when
   *     a group with the given name already exists
   */
  @IsAdmin
  @TransactionalWO
  public void importUsers(UsersImportDTO usersImportDTO) {
    Set<User> importedUsers = userMapper.mapUsersImportToSet(usersImportDTO.getUsers());
    for (User user : importedUsers) {
      user.setPicture(
          identiconService.generateIdenticons(
              user.getSub() + user.getIss(), ICON_WIDTH, ICON_HEIGHT));
    }
    Set<User> storedUsers = new HashSet<>(userService.createUsers(importedUsers));
    // add users to default group
    IDMGroup defaultGroup = idmGroupService.getGroupForDefaultRoles();
    storedUsers.forEach(defaultGroup::addUser);

    if (usersImportDTO.getGroupName() != null && !usersImportDTO.getGroupName().isBlank()) {
      IDMGroup group = new IDMGroup();
      group.setName(usersImportDTO.getGroupName());
      group.setDescription("No description");
      group.setStatus(UserAndGroupStatus.VALID);
      idmGroupService.createIDMGroup(group, new ArrayList<>());
      storedUsers.forEach(group::addUser);
    }
  }
}
