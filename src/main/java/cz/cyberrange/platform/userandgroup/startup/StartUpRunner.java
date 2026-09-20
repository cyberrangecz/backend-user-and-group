package cz.cyberrange.platform.userandgroup.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.cyberrange.platform.userandgroup.definition.exceptions.LoadingRolesAndUserException;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.RoleType;
import cz.cyberrange.platform.userandgroup.persistence.enums.UserAndGroupStatus;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.UserRepository;
import cz.cyberrange.platform.userandgroup.service.IdenticonService;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once when the application starts, loading the main microservice, its administrator, power
 * user and trainee roles, their groups, and the initial users listed in the file configured as
 * their source. Creates whichever of these does not already exist. On a later run it still
 * re-attaches an existing role to the main microservice and adds a matching user to the groups
 * implied by its current roles, but an already existing group is left unchanged.
 */
@Component
@Transactional
public class StartUpRunner implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);
  private static final int ICON_WIDTH = 75;
  private static final int ICON_HEIGHT = 75;
  private final UserRepository userRepository;
  private final IDMGroupRepository groupRepository;
  private final RoleRepository roleRepository;
  private final MicroserviceRepository microserviceRepository;
  private final IdenticonService identiconService;

  @Value("${path.to.initial.users}")
  private String pathToFileWithInitialUsers;

  @Value("${service.name}")
  private String nameOfUserAndGroupService;

  @Value("${server.servlet.context-path}")
  private String contextPathOfUserAndGroupService;

  @Value("${server.port}")
  private String portOfUserAndGroupService;

  private Role adminRole, userRole, guestRole;
  private IDMGroup adminGroup, userGroup, defaultGroup;
  private Microservice mainMicroservice;

  @Autowired
  public StartUpRunner(
      UserRepository userRepository,
      IDMGroupRepository groupRepository,
      RoleRepository roleRepository,
      MicroserviceRepository microserviceRepository,
      IdenticonService identiconService) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.roleRepository = roleRepository;
    this.microserviceRepository = microserviceRepository;
    this.identiconService = identiconService;
  }

  /**
   * Loads or creates, in order, the main microservice, its main roles, their groups and the initial
   * users.
   *
   * @param args unused
   * @throws IOException when the file with the initial users cannot be read or parsed
   * @throws LoadingRolesAndUserException when a loaded user's roles match none of the recognized
   *     role types
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {
    loadOrCreateMainMicroservice();
    loadOrCreateMainRoles();
    loadOrCreateGroupsForMainRole();
    loadOrCreateUsers();
    LOGGER.info("Users from external file were loaded and created in DB");
  }

  /**
   * Finds the main microservice by its configured name, creating it when none exists, then sets its
   * endpoint from the current port and context path either way.
   */
  private void loadOrCreateMainMicroservice() {
    mainMicroservice =
        microserviceRepository
            .findByName(nameOfUserAndGroupService)
            .orElseGet(
                () -> {
                  mainMicroservice = new Microservice(nameOfUserAndGroupService, "/");
                  return microserviceRepository.save(mainMicroservice);
                });
    mainMicroservice.setEndpoint(
        "BASE_URL:" + portOfUserAndGroupService + contextPathOfUserAndGroupService);
    LOGGER.info(
        "The main microservice for users and groups was registered under the name '{}'.",
        mainMicroservice.getName());
  }

  private void loadOrCreateMainRoles() {
    loadAdminRole();
    loadUserRole();
    loadGuestRole();
  }

  private void loadAdminRole() {
    adminRole =
        roleRepository
            .findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString())
            .orElseGet(
                () -> {
                  adminRole = new Role();
                  adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
                  adminRole.setMicroservice(mainMicroservice);
                  adminRole.setDescription(
                      "This role will allow you to create, edit, delete and manage users, groups and roles in CyberRangeCZ Platform.");
                  return roleRepository.save(adminRole);
                });
    adminRole.setMicroservice(mainMicroservice);
  }

  private void loadUserRole() {
    userRole =
        roleRepository
            .findByRoleType(RoleType.ROLE_USER_AND_GROUP_POWER_USER.toString())
            .orElseGet(
                () -> {
                  userRole = new Role();
                  userRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_POWER_USER.toString());
                  userRole.setMicroservice(mainMicroservice);
                  userRole.setDescription(
                      "This role will allow you to view data about other users.");
                  return roleRepository.save(userRole);
                });
    userRole.setMicroservice(mainMicroservice);
  }

  private void loadGuestRole() {
    guestRole =
        roleRepository
            .findByRoleType(RoleType.ROLE_USER_AND_GROUP_TRAINEE.toString())
            .orElseGet(
                () -> {
                  guestRole = new Role();
                  guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_TRAINEE.toString());
                  guestRole.setMicroservice(mainMicroservice);
                  guestRole.setDescription(
                      "Default role for user and group microservice. This role will allow you to view data about yourself.");
                  return roleRepository.save(guestRole);
                });
    guestRole.setMicroservice(mainMicroservice);
  }

  private void loadOrCreateGroupsForMainRole() {
    loadAdminGroup();
    loadUserGroup();
    loadDefaultGroup();
  }

  private void loadAdminGroup() {
    adminGroup =
        groupRepository
            .getIDMGroupByNameWithUsers(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())
            .orElseGet(
                () -> {
                  adminGroup = new IDMGroup();
                  adminGroup.setDescription("Initial group for users with ADMINISTRATOR role");
                  adminGroup.setStatus(UserAndGroupStatus.VALID);
                  adminGroup.setName(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName());
                  adminGroup.setRoles(Set.of(adminRole));
                  return groupRepository.save(adminGroup);
                });
  }

  private void loadUserGroup() {
    userGroup =
        groupRepository
            .getIDMGroupByNameWithUsers(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName())
            .orElseGet(
                () -> {
                  userGroup = new IDMGroup();
                  userGroup.setDescription("Initial group for users with USER role");
                  userGroup.setStatus(UserAndGroupStatus.VALID);
                  userGroup.setName(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName());
                  userGroup.setRoles(Set.of(userRole));
                  return groupRepository.save(userGroup);
                });
  }

  private void loadDefaultGroup() {
    defaultGroup =
        groupRepository
            .getIDMGroupByNameWithUsers(ImplicitGroupNames.DEFAULT_GROUP.getName())
            .orElseGet(
                () -> {
                  defaultGroup = new IDMGroup();
                  defaultGroup.setDescription("Group for users with default roles");
                  defaultGroup.setStatus(UserAndGroupStatus.VALID);
                  defaultGroup.setName(ImplicitGroupNames.DEFAULT_GROUP.getName());
                  defaultGroup.setRoles(Set.of(guestRole));
                  return groupRepository.save(defaultGroup);
                });
  }

  /**
   * Loads the initial users from the file configured as their source and, for each one, updates the
   * matching existing user or creates a new one.
   *
   * @throws IOException when the file cannot be read or parsed
   */
  private void loadOrCreateUsers() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    UsersWrapper[] usersWrapper =
        mapper.readValue(new File(pathToFileWithInitialUsers), UsersWrapper[].class);

    for (UsersWrapper userWrapper : usersWrapper) {
      Optional<User> optionalUser =
          userRepository.findBySubAndIss(
              userWrapper.getUser().getSub(), userWrapper.getUser().getIss());
      if (optionalUser.isPresent()) {
        updateUserBaseRoles(userWrapper, optionalUser.get());
      } else {
        createUserWithPredefinedRoles(userWrapper);
      }
    }
  }

  /**
   * Refills the given existing user's base fields and adds it to the groups implied by the given
   * roles, on top of any groups it already belongs to, then persists it.
   *
   * @param usersWrapper user data and roles to apply
   * @param user existing user to update
   */
  private void updateUserBaseRoles(UsersWrapper usersWrapper, User user) {
    fillNewUserData(user);
    if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)) {
      adminGroup.addUser(user);
      userGroup.addUser(user);
    } else if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_POWER_USER)) {
      userGroup.addUser(user);
    }
    defaultGroup.addUser(user);
    userRepository.save(user);
    LOGGER.info("Roles of user with screen name {} were updated.", user.getSub());
  }

  /**
   * Fills the wrapped user's base fields, assigns it the groups implied by its roles, and persists
   * it as a new user.
   *
   * @param usersWrapper user to create together with its roles
   */
  private void createUserWithPredefinedRoles(UsersWrapper usersWrapper) {
    User newUser = usersWrapper.getUser();
    fillNewUserData(newUser);
    fillUserGroups(newUser, usersWrapper);
    userRepository.save(newUser);
    LOGGER.info("User with screen name {} was created.", newUser.getSub());
  }

  /**
   * Fills the given user's given, family and full name with an empty string wherever they are null,
   * sets its status to valid regardless of the current value, and generates its picture from its
   * subject and issuer when it does not already have one.
   *
   * @param loadedUser user to fill
   */
  private void fillNewUserData(User loadedUser) {
    loadedUser.setGivenName(loadedUser.getGivenName() != null ? loadedUser.getGivenName() : "");
    loadedUser.setFamilyName(loadedUser.getFamilyName() != null ? loadedUser.getFamilyName() : "");
    loadedUser.setFullName(loadedUser.getFullName() != null ? loadedUser.getFullName() : "");
    loadedUser.setStatus(UserAndGroupStatus.VALID);
    if (loadedUser.getPicture() == null) {
      loadedUser.setPicture(
          identiconService.generateIdenticons(
              loadedUser.getSub() + loadedUser.getIss(), ICON_WIDTH, ICON_HEIGHT));
    }
  }

  /**
   * Replaces the given user's groups with the ones implied by the given roles: the administrator,
   * power user and default groups for an administrator, the power user and default groups for a
   * power user, and only the default group for a trainee or when no role is set.
   *
   * @param newUser user whose groups are replaced
   * @param usersWrapper roles that determine the groups
   * @return the same user, with its groups replaced
   * @throws LoadingRolesAndUserException when the roles match none of the recognized role types
   */
  private User fillUserGroups(User newUser, UsersWrapper usersWrapper) {
    Set<RoleType> usersWrapperRoles = usersWrapper.getRoles();
    if (usersWrapperRoles.contains(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)) {
      newUser.setGroups(Set.of(adminGroup, userGroup, defaultGroup));
    } else if (usersWrapperRoles.contains(RoleType.ROLE_USER_AND_GROUP_POWER_USER)) {
      newUser.setGroups(Set.of(userGroup, defaultGroup));
    } else if (usersWrapperRoles.contains(RoleType.ROLE_USER_AND_GROUP_TRAINEE)
        || usersWrapperRoles.isEmpty()) {
      newUser.setGroups(Set.of(defaultGroup));
    } else {
      LOGGER.error(
          "User cannot have roles other than these: {}, {}, {}",
          RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(),
          RoleType.ROLE_USER_AND_GROUP_POWER_USER.name(),
          RoleType.ROLE_USER_AND_GROUP_TRAINEE.name());
      throw new LoadingRolesAndUserException(
          "User cannot have roles other than these: "
              + RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name()
              + ", "
              + RoleType.ROLE_USER_AND_GROUP_POWER_USER.name()
              + ", "
              + RoleType.ROLE_USER_AND_GROUP_TRAINEE.name());
    }
    return newUser;
  }
}
