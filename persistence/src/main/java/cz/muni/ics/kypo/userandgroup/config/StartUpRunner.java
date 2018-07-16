package cz.muni.ics.kypo.userandgroup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.dbmodel.*;
import cz.muni.ics.kypo.userandgroup.exceptions.LoadingRolesAndUserException;
import cz.muni.ics.kypo.userandgroup.mapping.UsersAndMicroservicesWrapper;
import cz.muni.ics.kypo.userandgroup.mapping.userswrappers.UserWrapper;
import cz.muni.ics.kypo.userandgroup.persistence.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.persistence.RoleRepository;
import cz.muni.ics.kypo.userandgroup.persistence.UserRepository;
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

    @Value("${path.to.file.with.initial.users}")
    private String pathToFileWithInitialUsers;

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;

    private Role adminRole;
    private Role userRole;
    private Role guestRole;

    private IDMGroup adminGroup;
    private IDMGroup userGroup;
    private IDMGroup guestGroup;

    @Autowired
    public StartUpRunner(UserRepository userRepository, IDMGroupRepository groupRepository,
                         RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        loadMainRoles();
        loadGroupsForMainRole();

        UsersAndMicroservicesWrapper usersAndMicroservicesWrapper =
                mapper.readValue(new File(pathToFileWithInitialUsers), UsersAndMicroservicesWrapper.class);
        usersAndMicroservicesWrapper.getUsers().forEach(userWrapper -> System.out.println(userWrapper.getUser().toString()));

        loadUsers(usersAndMicroservicesWrapper.getUsers());

        LOGGER.info("Users from external file were loaded and created in DB");
    }

    private void loadUsers(List<UserWrapper> users) {
        users.forEach(userWrapper -> {
            Optional<User> optionalUser = userRepository.getUserByScreenNameWithUsers(userWrapper.getUser().getScreenName());
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
                LOGGER.info("Roles of user with screen name {} were updated.", user.getScreenName());
            } else {
                User newUser = new User(userWrapper.getUser().getScreenName());
                newUser.setFullName(userWrapper.getUser().getFullName());
                newUser.setMail(userWrapper.getUser().getMail());
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
                LOGGER.info("User with screen name {} was created.", newUser.getScreenName());
            }
        });
    }

    private void loadMainRoles() throws Exception {
        adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Administrator role was not found in database"));
        userRole = roleRepository.findByRoleType(RoleType.USER.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, User role was not found in database"));
        guestRole = roleRepository.findByRoleType(RoleType.GUEST.name())
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
