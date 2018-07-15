package cz.muni.ics.kypo.userandgroup.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.dbmodel.*;
import cz.muni.ics.kypo.userandgroup.exceptions.LoadingRolesAndUserException;
import cz.muni.ics.kypo.userandgroup.mapping.RolesAndUsersWrapper;
import cz.muni.ics.kypo.userandgroup.mapping.roleswrappers.RoleWrapper;
import cz.muni.ics.kypo.userandgroup.mapping.roleswrappers.ServiceAndRoles;
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
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@PropertySource("file:${path-to-config-file}")
public class StartUpRunner implements ApplicationRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);

    @Value("${path.to.file.with.initial.roles.and.users}")
    private String pathToFileWithInitialRolesAndUsers;

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;

    private List<Role> alreadyAddedRoles = new ArrayList<>();

    private Role adminRole;
    private Role userRole;
    private Role guestRole;

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
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, false);

        RolesAndUsersWrapper rolesAndUsersWrapper = mapper.readValue(new File(pathToFileWithInitialRolesAndUsers), RolesAndUsersWrapper.class);

        rolesAndUsersWrapper.getRoles().forEach(rolesMapping -> {
            System.out.println("---------------------------------------------------------------------------------------------");
            System.out.println("Service: " + rolesMapping.getService());
            rolesMapping.getRoles().forEach(roleWrapper -> {
                System.out.println("***************************************************************************************************");
                System.out.println("RoleType: " + roleWrapper.getRoleType() + "\n Children:");
                roleWrapper.getChildren().forEach(System.out::println);
            });
        });

//        rolesAndUsersWrapper.getUsers().forEach(userWrapper -> {
//            System.out.println(userWrapper.getUser().getFullName());
//            System.out.println(userWrapper.getUser().getMail());
//            System.out.println(userWrapper.getUser().getScreenName());
//            userWrapper.getRoles().forEach(System.out::println);
//        });

        LOGGER.info("YEA");
        loadMainRoles();
        loadRoles(rolesAndUsersWrapper.getRoles());
//        loadUsers(initialData.get("users"));
        LOGGER.info("Roles and users from external file were loaded and created in DB");
    }

    private void loadRoles(List<ServiceAndRoles> allRoles) {
        Queue<RoleWrapper> allRoleWrappers = getRoleWrappersFromServiceAndRoles(allRoles);

        while (!allRoleWrappers.isEmpty()) {
            RoleWrapper roleWrapper = allRoleWrappers.poll();
            if (roleRepository.existsByRoleType(roleWrapper.getRoleType())) {
                LOGGER.info("IS ALREADY IN DB");
            } else {
                boolean areAllChildrenInDB = true;
                for (RoleWrapper childRole : roleWrapper.getChildren()) {
                    if (!roleRepository.existsByRoleType(childRole.getRoleType())) {
                        if (allRoleWrappers.contains(childRole)) {
                            if (childRole.getChildren().contains(roleWrapper)) {
                                LOGGER.error("Between roles {} and {} exists cyclic dependency - first remove it.", childRole.getRoleType(), roleWrapper.getRoleType());
                                throw new LoadingRolesAndUserException("Between roles " + roleWrapper.getRoleType() + " and " + childRole.getRoleType() +
                                        " exists cyclic dependency - first remove it.");
                            }
                            allRoleWrappers.add(roleWrapper);
                            areAllChildrenInDB = false;
                            break;
                        } else {
                            LOGGER.error("Role with type {} is child of role {} but it does not have definition in file and is not saved in database.",
                                    childRole.getRoleType(), roleWrapper.getRoleType());
                            throw new LoadingRolesAndUserException("Role with type " + childRole.getRoleType() + " is child of role " + roleWrapper.getRoleType() +
                                    " but it does not have definition in file and is not saved in database.");
                        }
                    }
                }
                if (areAllChildrenInDB) {
                    Role newRole = new Role();
                    newRole.setRoleType(roleWrapper.getRoleType());
                    for (RoleWrapper childWrapper : roleWrapper.getChildren()) {
                        Role child = roleRepository.findByRoleType(childWrapper.getRoleType())
                                .orElseThrow(() -> new LoadingRolesAndUserException("Role " + childWrapper.getRoleType() + " could not be found in database. Please try it again"));
                        newRole.addChildRole(child);
                    }
                    Role addedRole = roleRepository.save(newRole);
                    alreadyAddedRoles.add(addedRole);
                    LOGGER.info("Role {} was saved in database", newRole.getRoleType());
                }
            }
        }
    }

    private Queue<RoleWrapper> getRoleWrappersFromServiceAndRoles(List<ServiceAndRoles> serviceAndRolesList) {
        Queue<RoleWrapper> roleWrappers = new LinkedList<>();
        serviceAndRolesList.forEach(serviceAndRoles -> roleWrappers.addAll(serviceAndRoles.getRoles()));
        return roleWrappers;
    }

//    private void loadUsers(JsonNode initialUsers) throws IOException {
//        Iterator<Map.Entry<String, JsonNode>> rolesWithUsersIterator = initialUsers.fields();
//        while (rolesWithUsersIterator.hasNext()) {
//            Map.Entry<String, JsonNode> usersOfRole = rolesWithUsersIterator.next();
//            if (roleRepository.existsByRoleType(usersOfRole.getKey())) {
//                LOGGER.info("Assigning role {} to users", usersOfRole.getKey());
//                Optional<IDMGroup> optionalGroup = groupRepository.getIDMGroupByNameWithUsers(usersOfRole.getKey());
//                IDMGroup groupForRole = optionalGroup.orElseThrow(IOException::new);
//                JsonNode usersJson = usersOfRole.getValue();
//
//                Iterator<JsonNode> usersJsonIterator = usersJson.elements();
//                while (usersJsonIterator.hasNext()) {
//                    JsonNode userJson = usersJsonIterator.next();
//                    Optional<User> optionalUser = userRepository.findByScreenName(userJson.get("screenName").asText());
//                    if (!optionalUser.isPresent()) {
//                        User user = new User(userJson.get("screenName").asText());
//                        user.setFullName(userJson.get("fullName").asText());
//                        user.setMail(userJson.get("mail").asText());
//                        user.setStatus(UserAndGroupStatus.VALID);
//                        userRepository.saveAndFlush(user);
//                        LOGGER.warn("User with screeName {} was created and assigned role {}.", user.getScreenName(), usersOfRole.getKey());
//                        groupForRole.addUser(user);
//                    } else {
//                        User user = optionalUser.get();
//                        if (!groupForRole.getUsers().contains(user)) {
//                            groupForRole.addUser(user);
//                            LOGGER.warn("User with screeName {} was assigned role {}.", user.getScreenName(), usersOfRole.getKey());
//                        } else {
//                            LOGGER.warn("SKIPPED, User with screeName {} already has role {}.", user.getScreenName(), usersOfRole.getKey());
//                        }
//                    }
//                }
//                groupRepository.save(groupForRole);
//                LOGGER.warn("{} role was given to users.", usersOfRole.getKey());
//            } else {
//                LOGGER.error("SKIPPED, Users with role {} were not loaded, the role does not exist.", usersOfRole.getKey());
//                throw new IOException("SKIPPED, Users with role " + usersOfRole.getKey() + " were not loaded, the role does not exist.");
//            }
//        }
//    }

    private void loadMainRoles() throws Exception {
        adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Administrator role was not found"));
        userRole = roleRepository.findByRoleType(RoleType.USER.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, User role was not found"));
        guestRole = roleRepository.findByRoleType(RoleType.GUEST.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Guest role was not found"));
    }

    private void removeAlreadyAddedRole() {
        alreadyAddedRoles.forEach(rolesMapping -> {
            roleRepository.delete(roleRepository.findByRoleType(rolesMapping.getRoleType()).get());
        });
    }

//    private void loadRoles(JsonNode initialRoles) throws IOException {
//        JsonNode rolesOfServices = initialRoles.get("services");
//
//        Iterator<JsonNode> services = rolesOfServices.elements();
//        while (services.hasNext()) {
//            JsonNode service = services.next();
//            if (service.has("name") && service.has("roles")) {
//                JsonNode roles = service.get("roles");
//                roles.forEach(roleName -> {
//                    if (!roleRepository.existsByRoleType(roleName.asText())) {
//                        Role newRole = new Role();
//                        newRole.setRoleType(roleName.asText());
//                        roleRepository.save(newRole);
//                        LOGGER.info("Role with name {} was created", roleName.asText());
//
//                        IDMGroup groupForRole = new IDMGroup(roleName.asText(),
//                                "Group of all users wiht role " + roleName.asText().toLowerCase());
//                        groupForRole.addRole(newRole);
//                        groupForRole.setStatus(UserAndGroupStatus.VALID);
//                        groupRepository.save(groupForRole);
//                        LOGGER.info("Group for role with name {} was created", roleName.asText());
//                    } else {
//                        LOGGER.warn("SKIPPED, Role with name {} already exists.", roleName.asText());
//                    }
//                });
//            } else {
//                LOGGER.error("At least one service does not have name or roles parameter, please fix it and try it again");
//                throw new IOException("At least one service does not have name or roles parameter, please fix it and try it again");
//            }
//        }
//    }
}
