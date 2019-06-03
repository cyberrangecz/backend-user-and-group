package cz.muni.ics.kypo.userandgroup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.exceptions.LoadingRolesAndUserException;
import cz.muni.ics.kypo.userandgroup.mapping.UsersWrapper;
import cz.muni.ics.kypo.userandgroup.model.*;
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
import java.util.*;

/**
 * @author Jan Duda
 * @author Pavel Seda
 */
@Component
@Transactional
public class StartUpRunner implements ApplicationRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);

    @Value("${path.to.file.with.initial.users.and.services}")
    private String pathToFileWithInitialUsers;

    @Value("${service.name}")
    private String nameOfUserAndGroupService;

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;
    private MicroserviceRepository microserviceRepository;

    private Role adminRole, userRole, guestRole;
    private IDMGroup adminGroup, userGroup, defaultGroup;
    private Microservice mainMicroservice;

    @Autowired
    public StartUpRunner(UserRepository userRepository, IDMGroupRepository groupRepository,
                         RoleRepository roleRepository, MicroserviceRepository microserviceRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        this.microserviceRepository = microserviceRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        UsersWrapper[] usersWrapper =
                mapper.readValue(new File(pathToFileWithInitialUsers), UsersWrapper[].class);

        loadMainMicroservice();
        loadMainRoles();
        loadGroupsForMainRole();
        loadUsers(Arrays.asList(usersWrapper));

        LOGGER.info("Users from external file were loaded and created in DB");
    }

    private void loadMainMicroservice() {
        mainMicroservice = microserviceRepository.findByName(nameOfUserAndGroupService)
                .orElseGet(() -> {
                    mainMicroservice = new Microservice();
                    mainMicroservice.setEndpoint("/");
                    mainMicroservice.setName(nameOfUserAndGroupService);
                    return microserviceRepository.save(mainMicroservice);
                });
        LOGGER.info("Main microservice for users and groups was registered", mainMicroservice.getName());
    }

    private void loadUsers(List<UsersWrapper> users) {
        users.forEach(usersWrapper -> {
            Optional<User> optionalUser = userRepository.getUserByLoginWithGroups(usersWrapper.getUser().getLogin());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)) {
                    user.addGroup(adminGroup);
                    user.addGroup(userGroup);
                } else if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_USER)) {
                    user.addGroup(userGroup);
                }
                user.addGroup(defaultGroup);
                userRepository.save(user);
                LOGGER.info("Roles of user with screen name {} were updated.", user.getLogin());
            } else {
                User newUser = new User(usersWrapper.getUser().getLogin());
                newUser.setStatus(UserAndGroupStatus.VALID);
                userRepository.save(newUser);
                LOGGER.info("User with screen name {} was created.", newUser.getLogin());

                if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR)) {
                    newUser.setGroups(Set.of(adminGroup, userGroup, defaultGroup));
                } else if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_USER)) {
                    newUser.setGroups(Set.of(userGroup, defaultGroup));
                } else if (usersWrapper.getRoles().contains(RoleType.ROLE_USER_AND_GROUP_GUEST) || usersWrapper.getRoles().isEmpty()) {
                    newUser.setGroups(Set.of(defaultGroup));
                } else {
                    LOGGER.error("User cannot have roles other than these: {}, {}, {}", RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name(), RoleType.ROLE_USER_AND_GROUP_USER.name(), RoleType.ROLE_USER_AND_GROUP_GUEST.name());
                    throw new LoadingRolesAndUserException("User cannot have roles other than these: " + RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name() +
                            ", " + RoleType.ROLE_USER_AND_GROUP_USER.name() + ", " + RoleType.ROLE_USER_AND_GROUP_GUEST.name());
                }
                LOGGER.info("User with screen name {} was created.", newUser.getLogin());
            }
        });
    }

    private void loadMainRoles() {
        adminRole = roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString())
                .orElseGet(() -> {
                    adminRole = new Role();
                    adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
                    adminRole.setMicroservice(mainMicroservice);
                    return roleRepository.save(adminRole);
                });
        adminRole.setMicroservice(mainMicroservice);
        userRole = roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString())
                .orElseGet(() -> {
                    userRole = new Role();
                    userRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
                    userRole.setMicroservice(mainMicroservice);
                    return roleRepository.save(userRole);
                });
        userRole.setMicroservice(mainMicroservice);
        guestRole = roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString())
                .orElseGet(() -> {
                    guestRole = new Role();
                    guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
                    guestRole.setMicroservice(mainMicroservice);
                    return roleRepository.save(guestRole);
                });
        guestRole.setMicroservice(mainMicroservice);
    }

    private void loadGroupsForMainRole() {
        adminGroup = groupRepository.getIDMGroupByNameWithUsers("USER_AND_GROUP_ADMINISTRATOR")
                .orElseGet(() -> {
                    adminGroup = new IDMGroup();
                    adminGroup.setDescription("Initial group for users with ADMINISTRATOR role");
                    adminGroup.setStatus(UserAndGroupStatus.VALID);
                    adminGroup.setName("USER_AND_GROUP_ADMINISTRATOR");
                    adminGroup.setRoles(Set.of(adminRole));
                    return groupRepository.save(adminGroup);
                });
        userGroup = groupRepository.getIDMGroupByNameWithUsers("USER_AND_GROUP_USER")
                .orElseGet(() -> {
                    userGroup = new IDMGroup();
                    userGroup.setDescription("Initial group for users with USER role");
                    userGroup.setStatus(UserAndGroupStatus.VALID);
                    userGroup.setName("USER_AND_GROUP_USER");
                    userGroup.setRoles(Set.of(userRole));
                    return groupRepository.save(userGroup);
                });
        defaultGroup = groupRepository.getIDMGroupByNameWithUsers("DEFAULT_GROUP")
                .orElseGet(() -> {
                    defaultGroup = new IDMGroup();
                    defaultGroup.setDescription("Group for users with default roles");
                    defaultGroup.setStatus(UserAndGroupStatus.VALID);
                    defaultGroup.setName("DEFAULT_GROUP");
                    defaultGroup.setRoles(Set.of(guestRole));
                    return groupRepository.save(defaultGroup);
                });
    }
}
