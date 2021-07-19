package cz.muni.ics.kypo.userandgroup.startuprunners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.exceptions.LoadingRolesAndUserException;
import cz.muni.ics.kypo.userandgroup.service.impl.IdenticonService;
import cz.muni.ics.kypo.userandgroup.startuprunners.mapping.UsersWrapper;
import cz.muni.ics.kypo.userandgroup.entities.*;
import cz.muni.ics.kypo.userandgroup.entities.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.entities.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class is annotated with {@link Component}, so its mark as candidates for auto-detection when using annotation-based configuration and classpath scanning.
 * This class is responsible for loading main microservice <strong>User-and-group<strong/>, main roles for this microservice and users are listed in the
 * configuration file. All of these actions are performed during the start of the application.
 */
@Component
@Transactional
public class StartUpRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);
    private static final int ICON_WIDTH = 75;
    private static final int ICON_HEIGHT = 75;

    @Value("${path.to.initial.users}")
    private String pathToFileWithInitialUsers;

    @Value("${service.name}")
    private String nameOfUserAndGroupService;

    @Value("${server.servlet.context-path}")
    private String contextPathOfUserAndGroupService;

    @Value("${server.port}")
    private String portOfUserAndGroupService;


    private final UserRepository userRepository;
    private final IDMGroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final MicroserviceRepository microserviceRepository;
    private final IdenticonService identiconService;

    private Role adminRole, userRole, guestRole;
    private IDMGroup adminGroup, userGroup, defaultGroup;
    private Microservice mainMicroservice;

    @Autowired
    public StartUpRunner(UserRepository userRepository, IDMGroupRepository groupRepository,
                         RoleRepository roleRepository, MicroserviceRepository microserviceRepository,
                         IdenticonService identiconService) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        this.microserviceRepository = microserviceRepository;
        this.identiconService = identiconService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        loadOrCreateMainMicroservice();
        loadOrCreateMainRoles();
        loadOrCreateGroupsForMainRole();
        loadOrCreateUsers();
        LOGGER.info("Users from external file were loaded and created in DB");
    }

    private void loadOrCreateMainMicroservice() {
        mainMicroservice = microserviceRepository.findByName(nameOfUserAndGroupService)
                .orElseGet(() -> {
                    mainMicroservice = new Microservice(nameOfUserAndGroupService, "/");
                    return microserviceRepository.save(mainMicroservice);
                });
        mainMicroservice.setEndpoint("BASE_URL:" + portOfUserAndGroupService + contextPathOfUserAndGroupService);
        LOGGER.info("The main microservice for users and groups was registered under the name '{}'.", mainMicroservice.getName());
    }

    private void loadOrCreateMainRoles() {
        loadAdminRole();
        loadUserRole();
        loadGuestRole();
    }

    private void loadAdminRole() {
        adminRole = roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString())
                .orElseGet(() -> {
                    adminRole = new Role();
                    adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
                    adminRole.setMicroservice(mainMicroservice);
                    adminRole.setDescription("This role will allow you to create, edit, delete and manage users, groups and roles in KYPO.");
                    return roleRepository.save(adminRole);
                });
        adminRole.setMicroservice(mainMicroservice);
    }

    private void loadUserRole() {
        userRole = roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString())
                .orElseGet(() -> {
                    userRole = new Role();
                    userRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
                    userRole.setMicroservice(mainMicroservice);
                    userRole.setDescription("This role is user role.");
                    return roleRepository.save(userRole);
                });
        userRole.setMicroservice(mainMicroservice);
    }

    private void loadGuestRole() {
        guestRole = roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString())
                .orElseGet(() -> {
                    guestRole = new Role();
                    guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
                    guestRole.setMicroservice(mainMicroservice);
                    guestRole.setDescription("Default role for user and group microservice. It is usually required, e.g., to see basic data about user.");
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
        adminGroup = groupRepository.getIDMGroupByNameWithUsers(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())
                .orElseGet(() -> {
                    adminGroup = new IDMGroup();
                    adminGroup.setDescription("Initial group for users with ADMINISTRATOR role");
                    adminGroup.setStatus(UserAndGroupStatus.VALID);
                    adminGroup.setName(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName());
                    adminGroup.setRoles(Set.of(adminRole));
                    return groupRepository.save(adminGroup);
                });
    }

    private void loadUserGroup() {
        userGroup = groupRepository.getIDMGroupByNameWithUsers(ImplicitGroupNames.USER_AND_GROUP_USER.getName())
                .orElseGet(() -> {
                    userGroup = new IDMGroup();
                    userGroup.setDescription("Initial group for users with USER role");
                    userGroup.setStatus(UserAndGroupStatus.VALID);
                    userGroup.setName(ImplicitGroupNames.USER_AND_GROUP_USER.getName());
                    userGroup.setRoles(Set.of(userRole));
                    return groupRepository.save(userGroup);
                });
    }

    private void loadDefaultGroup() {
        defaultGroup = groupRepository.getIDMGroupByNameWithUsers(ImplicitGroupNames.DEFAULT_GROUP.getName())
                .orElseGet(() -> {
                    defaultGroup = new IDMGroup();
                    defaultGroup.setDescription("Group for users with default roles");
                    defaultGroup.setStatus(UserAndGroupStatus.VALID);
                    defaultGroup.setName(ImplicitGroupNames.DEFAULT_GROUP.getName());
                    defaultGroup.setRoles(Set.of(guestRole));
                    return groupRepository.save(defaultGroup);
                });
    }

    private void loadOrCreateUsers() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        UsersWrapper[] usersWrapper = mapper.readValue(new File(pathToFileWithInitialUsers), UsersWrapper[].class);

        for (UsersWrapper userWrapper : usersWrapper) {
            Optional<User> optionalUser = userRepository.findBySubAndIss(userWrapper.getUser().getSub(), userWrapper.getUser().getIss());
            if (optionalUser.isPresent()) {
                updateUserBaseRoles(userWrapper, optionalUser.get());
            } else {
                createUserWithPredefinedRoles(userWrapper);
            }
        }
    }

    private void updateUserBaseRoles(UsersWrapper usersWrapper, User user) {
        fillNewUserData(user);
        if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)) {
            adminGroup.addUser(user);
            userGroup.addUser(user);
        } else if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_USER)) {
            userGroup.addUser(user);
        }
        defaultGroup.addUser(user);
        userRepository.save(user);
        LOGGER.info("Roles of user with screen name {} were updated.", user.getSub());
    }

    private void createUserWithPredefinedRoles(UsersWrapper usersWrapper) {
        User newUser = usersWrapper.getUser();
        fillNewUserData(newUser);
        fillUserGroups(newUser, usersWrapper);
        userRepository.save(newUser);
        LOGGER.info("User with screen name {} was created.", newUser.getSub());
    }

    private void fillNewUserData(User loadedUser) {
        loadedUser.setGivenName(loadedUser.getGivenName() != null ? loadedUser.getGivenName() : "");
        loadedUser.setFamilyName(loadedUser.getFamilyName() != null ? loadedUser.getFamilyName() : "");
        loadedUser.setFullName(loadedUser.getFullName() != null ? loadedUser.getFullName() : "");
        loadedUser.setStatus(UserAndGroupStatus.VALID);
        if(loadedUser.getPicture() == null) {
            loadedUser.setPicture(identiconService.generateIdenticons(loadedUser.getSub() + loadedUser.getIss(), ICON_WIDTH, ICON_HEIGHT));
        }
    }

    private User fillUserGroups(User newUser, UsersWrapper usersWrapper) {
        Set<RoleType> usersWrapperRoles = usersWrapper.getRoles();
        if (usersWrapperRoles.contains(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)) {
            newUser.setGroups(Set.of(adminGroup, userGroup, defaultGroup));
        } else if (usersWrapperRoles.contains(RoleType.ROLE_USER_AND_GROUP_USER)) {
            newUser.setGroups(Set.of(userGroup, defaultGroup));
        } else if (usersWrapperRoles.contains(RoleType.ROLE_USER_AND_GROUP_GUEST) || usersWrapperRoles.isEmpty()) {
            newUser.setGroups(Set.of(defaultGroup));
        } else {
            LOGGER.error("User cannot have roles other than these: {}, {}, {}", RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(), RoleType.ROLE_USER_AND_GROUP_USER.name(), RoleType.ROLE_USER_AND_GROUP_GUEST.name());
            throw new LoadingRolesAndUserException("User cannot have roles other than these: " + RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name() +
                    ", " + RoleType.ROLE_USER_AND_GROUP_USER.name() + ", " + RoleType.ROLE_USER_AND_GROUP_GUEST.name());
        }
        return newUser;
    }

}
