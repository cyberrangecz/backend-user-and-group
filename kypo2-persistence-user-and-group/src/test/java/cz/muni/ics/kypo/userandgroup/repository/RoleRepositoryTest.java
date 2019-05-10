package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Microservice kypoTraining, kypoUserAndGroup;
    private Role adminRole, designerRole, traineeRole;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setup() {
        kypoTraining = new Microservice();
        kypoTraining.setEndpoint("http://kypo2-training/api/v1");
        kypoTraining.setName("training");
        this.entityManager.persistAndFlush(kypoTraining);

        kypoUserAndGroup = new Microservice();
        kypoUserAndGroup.setEndpoint("http://kypo2-user-and-group/api/v1");
        kypoUserAndGroup.setName("userAndGroup");
        this.entityManager.persistAndFlush(kypoUserAndGroup);

        adminRole = new Role();
        adminRole.setMicroservice(kypoUserAndGroup);
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name());
        this.entityManager.persistAndFlush(adminRole);

        designerRole = new Role();
        designerRole.setMicroservice(kypoTraining);
        designerRole.setRoleType("ROLE_TRAINING_DESIGNER");
        this.entityManager.persistAndFlush(designerRole);

        traineeRole = new Role();
        traineeRole.setMicroservice(kypoTraining);
        traineeRole.setRoleType("ROLE_TRAINING_TRAINEE");
        this.entityManager.persistAndFlush(traineeRole);
    }

    @Test
    public void findByRoleType() throws Exception {
        Optional<Role> optionalRole = this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        Role r = optionalRole.orElseThrow(() -> new Exception("Role shoul be found"));
        assertEquals(adminRole, r);
        assertEquals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString(), r.getRoleType());
    }

    @Test
    public void findByRoleTypeNotFound() {
        assertFalse(this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString()).isPresent());
    }

    @Test
    public void existByRoleType() {
        assertTrue(roleRepository.existsByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString()));
        assertFalse(roleRepository.existsByRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString()));
    }

    @Test
    public void getAllRolesByMicroservice() {
        Set<Role> roles = roleRepository.getAllRolesByMicroserviceName(kypoTraining.getName());
        assertEquals(2, roles.size());
        assertTrue(roles.contains(designerRole));
        assertTrue(roles.contains(traineeRole));
        assertFalse(roles.contains(adminRole));
    }
}
