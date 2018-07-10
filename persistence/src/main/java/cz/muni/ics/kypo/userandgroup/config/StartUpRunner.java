package cz.muni.ics.kypo.userandgroup.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.dbmodel.*;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
@PropertySource("file:${path-to-config-file}")
public class StartUpRunner implements ApplicationRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);

    @Value("${path.to.file.with.initial.roles.and.users}")
    private String pathToFileWithInitialRolesAndUsers;

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;

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
        JsonNode initialData = mapper.readTree(new File(pathToFileWithInitialRolesAndUsers));
        loadRoles(initialData.get("roles"));
        loadUsers(initialData.get("users"));
        LOGGER.info("Roles and users from external file were loaded and created in DB");
    }

    private void loadUsers(JsonNode initialUsers) throws IOException {
        Iterator<Map.Entry<String, JsonNode>> rolesWithUsersIterator = initialUsers.fields();
        while (rolesWithUsersIterator.hasNext()) {
            Map.Entry<String, JsonNode> usersOfRole = rolesWithUsersIterator.next();
            if (roleRepository.existsByRoleType(usersOfRole.getKey())) {
                LOGGER.info("Assigning role {} to users", usersOfRole.getKey());
                Optional<IDMGroup> optionalGroup = groupRepository.getIDMGroupByNameWithUsers(usersOfRole.getKey());
                IDMGroup groupForRole = optionalGroup.orElseThrow(IOException::new);
                JsonNode usersJson = usersOfRole.getValue();

                Iterator<JsonNode> usersJsonIterator = usersJson.elements();
                while (usersJsonIterator.hasNext()) {
                    JsonNode userJson = usersJsonIterator.next();
                    Optional<User> optionalUser = userRepository.findByScreenName(userJson.get("screenName").asText());
                    if (!optionalUser.isPresent()) {
                        User user = new User(userJson.get("screenName").asText());
                        user.setFullName(userJson.get("fullName").asText());
                        user.setMail(userJson.get("mail").asText());
                        user.setStatus(UserAndGroupStatus.VALID);
                        userRepository.saveAndFlush(user);
                        LOGGER.warn("User with screeName {} was created and assigned role {}.", user.getScreenName(), usersOfRole.getKey());
                        groupForRole.addUser(user);
                    } else {
                        User user = optionalUser.get();
                        if (!groupForRole.getUsers().contains(user)) {
                            groupForRole.addUser(user);
                            LOGGER.warn("User with screeName {} was assigned role {}.", user.getScreenName(), usersOfRole.getKey());
                        } else {
                            LOGGER.warn("SKIPPED, User with screeName {} already has role {}.", user.getScreenName(), usersOfRole.getKey());
                        }
                    }
                }
                groupRepository.save(groupForRole);
                LOGGER.warn("{} role was given to users.", usersOfRole.getKey());
            } else {
                LOGGER.error("SKIPPED, Users with role {} were not loaded, the role does not exist.", usersOfRole.getKey());
                throw new IOException("SKIPPED, Users with role " + usersOfRole.getKey() + " were not loaded, the role does not exist.");
            }
        }
    }

    private void loadRoles(JsonNode initialRoles) throws IOException {
        JsonNode rolesOfServices = initialRoles.get("services");

        Iterator<JsonNode> services = rolesOfServices.elements();
        while (services.hasNext()) {
            JsonNode service = services.next();
            if (service.has("name") && service.has("roles")) {
                JsonNode roles = service.get("roles");
                roles.forEach(roleName -> {
                    if (!roleRepository.existsByRoleType(roleName.asText())) {
                        Role newRole = new Role();
                        newRole.setRoleType(roleName.asText());
                        roleRepository.save(newRole);
                        LOGGER.info("Role with name {} was created", roleName.asText());

                        IDMGroup groupForRole = new IDMGroup(roleName.asText(),
                                "Group of all users wiht role " + roleName.asText().toLowerCase());
                        groupForRole.addRole(newRole);
                        groupForRole.setStatus(UserAndGroupStatus.VALID);
                        groupRepository.save(groupForRole);
                        LOGGER.info("Group for role with name {} was created", roleName.asText());
                    } else {
                        LOGGER.warn("SKIPPED, Role with name {} already exists.", roleName.asText());
                    }
                });
            } else {
                LOGGER.error("At least one service does not have name or roles parameter, please fix it and try it again");
                throw new IOException("At least one service does not have name or roles parameter, please fix it and try it again");
            }
        }
    }
}
