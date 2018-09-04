package cz.muni.ics.kypo.userandgroup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.exceptions.LoadingRolesAndUserException;
import cz.muni.ics.kypo.userandgroup.mapping.UsersAndMicroservicesWrapper;
import cz.muni.ics.kypo.userandgroup.mapping.UserWrapper;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
@PropertySource("file:${path-to-config-file}")
public class StartUpRunner implements ApplicationRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);

    public static final String NAME_OF_USER_GROUP_SERVICE = "User and Group";

    @Value("${path.to.file.with.initial.users.and.services}")
    private String pathToFileWithInitialUsersAndServices;

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;
    private MicroserviceRepository microserviceRepository;

    private Role adminRole, userRole, guestRole;

    private IDMGroup adminGroup, userGroup, guestGroup;

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
        loadMainRoles();
        loadGroupsForMainRole();

        UsersAndMicroservicesWrapper usersAndMicroservicesWrapper =
                mapper.readValue(new File(pathToFileWithInitialUsersAndServices), UsersAndMicroservicesWrapper.class);

        loadMicroservices(usersAndMicroservicesWrapper.getMicroservices());
        loadUsers(usersAndMicroservicesWrapper.getUsers());

        LOGGER.info("Users from external file were loaded and created in DB");
    }

    private void loadMicroservices(List<Microservice> microservices) {
        microserviceRepository.deleteAll();
        LOGGER.info("All microservices managed by user-and-group service were deleted from database. (Only microservices which are in the file are active.)");

        Microservice userAndGroupService = new Microservice();
        userAndGroupService.setName(NAME_OF_USER_GROUP_SERVICE);
        microserviceRepository.save(userAndGroupService);

        microservices.forEach(microservice -> {
            microserviceRepository.save(microservice);
            LOGGER.info("Microservice with name {} was registered", microservice.getName());
        });
    }

    private void loadUsers(List<UserWrapper> users) {
        users.forEach(userWrapper -> {
            Optional<User> optionalUser = userRepository.getUserByLoginWithUsers(userWrapper.getUser().getLogin());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Set<Role> rolesOfUserInDB = userRepository.getRolesOfUser(user.getId());

                if (rolesOfUserInDB.contains(userRole) && !rolesOfUserInDB.contains(adminRole)
                        && userWrapper.getRoles().contains(RoleType.ADMINISTRATOR)) {
                    user.addGroup(adminGroup);
                } else if (rolesOfUserInDB.contains(guestRole) && !rolesOfUserInDB.contains(userRole)) {
                    if (userWrapper.getRoles().contains(RoleType.ADMINISTRATOR)) {
                        user.addGroup(adminGroup);
                        user.addGroup(userGroup);
                    } else if (userWrapper.getRoles().contains(RoleType.USER)) {
                        user.addGroup(userGroup);
                    }
                }
                userRepository.save(user);
                LOGGER.info("Roles of user with screen name {} were updated.", user.getLogin());
            } else {
                User newUser = new User(userWrapper.getUser().getLogin());
                newUser.setStatus(UserAndGroupStatus.VALID);

                if (userWrapper.getRoles().contains(RoleType.ADMINISTRATOR)) {
                    newUser.addGroup(adminGroup);
                    newUser.addGroup(userGroup);
                    newUser.addGroup(guestGroup);
                } else if (userWrapper.getRoles().contains(RoleType.USER)) {
                    newUser.addGroup(userGroup);
                    newUser.addGroup(guestGroup);
                } else if (userWrapper.getRoles().contains(RoleType.GUEST) || userWrapper.getRoles().isEmpty()) {
                    newUser.addGroup(guestGroup);
                } else {
                    LOGGER.error("User cannot have roles other than these: {}, {}, {}", RoleType.ADMINISTRATOR.name(), RoleType.USER.name(), RoleType.GUEST.name());
                    throw new LoadingRolesAndUserException("User cannot have roles other than these: " + RoleType.ADMINISTRATOR.name() +
                            ", " + RoleType.USER.name() + ", " + RoleType.GUEST.name());
                }
                userRepository.save(newUser);
                LOGGER.info("User with screen name {} was created.", newUser.getLogin());
            }
        });
    }

    private void loadMainRoles() throws Exception {
        adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR)
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Administrator role was not found in database"));
        userRole = roleRepository.findByRoleType(RoleType.USER)
                .orElseThrow(() -> new Exception("Migration was not completed successfully, User role was not found in database"));
        guestRole = roleRepository.findByRoleType(RoleType.GUEST)
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Guest role was not found in database"));
    }

    private void loadGroupsForMainRole() throws Exception {
        adminGroup = groupRepository.getIDMGroupByNameWithUsers(RoleType.ADMINISTRATOR.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Group for Administrator role was not found in database"));
        userGroup = groupRepository.getIDMGroupByNameWithUsers(RoleType.USER.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Group for User role was not found in database"));
        guestGroup = groupRepository.getIDMGroupByNameWithUsers(RoleType.GUEST.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Group for Guest role was not found in database"));
    }
}
