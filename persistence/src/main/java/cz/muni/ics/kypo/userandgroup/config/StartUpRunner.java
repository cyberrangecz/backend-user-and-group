package cz.muni.ics.kypo.userandgroup.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.dbmodel.*;
import cz.muni.ics.kypo.userandgroup.exceptions.LoadingRolesAndUserException;
import cz.muni.ics.kypo.userandgroup.mapping.RolesAndUsersWrapper;
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

        loadMainRoles();
        LOGGER.info("Users from external file were loaded and created in DB");
    }

    private void loadMainRoles() throws Exception {
        adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Administrator role was not found in database"));
        userRole = roleRepository.findByRoleType(RoleType.USER.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, User role was not found in database"));
        guestRole = roleRepository.findByRoleType(RoleType.GUEST.name())
                .orElseThrow(() -> new Exception("Migration was not completed successfully, Guest role was not found in database"));
    }

    private void removeAlreadyAddedRole() {
        alreadyAddedRoles.forEach(rolesMapping -> {
            roleRepository.delete(roleRepository.findByRoleType(rolesMapping.getRoleType()).get());
        });
    }
}
