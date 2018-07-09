package cz.muni.ics.kypo.userandgroup;

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
import java.util.List;
import java.util.Map;

@Component
@PropertySource("file:${path-to-config-file}")
public class StartUpRunner implements ApplicationRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(StartUpRunner.class);

    @Value("${path.to.initial.users.file}")
    private String pathToInitialUsersFile;

    @Value("${path.to.initial.roles.file}")
    private String pathToInitialRolesFile;

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
        loadRoles(mapper);
        LOGGER.info("Roles and users from external file were loaded and created in DB");
    }

    private void loadRoles(ObjectMapper mapper) throws IOException {
        JsonNode rolesOfServices = mapper.readTree(new File(pathToInitialRolesFile)).get("services");

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

                        IDMGroup groupForRole = new IDMGroup("Group " + roleName.asText(),
                                "Group of all users wiht role " + roleName.asText().toLowerCase());
                        groupForRole.addRole(newRole);
                        groupForRole.setStatus(UserAndGroupStatus.VALID);
                        groupRepository.save(groupForRole);
                        LOGGER.info("Group for role with name {} was created", roleName.asText());
                    } else {
                        LOGGER.warn("Role with name {} already exists, it was not created.", roleName.asText());
                    }
                });
            } else {
                throw new IOException("At least one service does not have name or roles parameter, please fix it and try it again");
            }
        }
    }
}
