package cz.muni.ics.kypo.userandgroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;
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

        LOGGER.info("START");
        try {
//            Map<String, List<User>> rolesOfUser = mapper.readValue(new File(pathToInitialUsersFile), Map.class);
            JsonNode jsonNode = mapper.readTree(new File(pathToInitialUsersFile));

            LOGGER.info("Users loaded");
            LOGGER.info(jsonNode.toString());
            jsonNode.fields().forEachRemaining(stringJsonNodeEntry -> LOGGER.info(stringJsonNodeEntry.toString()));
            jsonNode.fields().forEachRemaining(stringJsonNodeEntry -> {
                if(stringJsonNodeEntry.getKey().toUpperCase().equals(RoleType.ADMINISTRATOR.name())) {
                    Role adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR);
                }
            });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
