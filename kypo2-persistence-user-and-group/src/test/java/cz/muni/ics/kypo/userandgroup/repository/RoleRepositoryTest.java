package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@ComponentScan(basePackages = "cz.muni.ics.kypo.userandgroup.util")
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TestDataFactory testDataFactory;
    private Microservice kypoTraining, kypoUserAndGroup;
    private Role adminRole, guestRole, designerRole, traineeRole;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setup() {
        kypoTraining = testDataFactory.getKypoTrainingMicroservice();
        this.entityManager.persistAndFlush(kypoTraining);

        kypoUserAndGroup = testDataFactory.getKypoUaGMicroservice();
        this.entityManager.persistAndFlush(kypoUserAndGroup);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setMicroservice(kypoUserAndGroup);
        this.entityManager.persistAndFlush(adminRole);

        guestRole = testDataFactory.getUAGGuestRole();
        guestRole.setMicroservice(kypoUserAndGroup);
        this.entityManager.persistAndFlush(guestRole);

        designerRole = testDataFactory.getTrainingDesignerRole();
        designerRole.setMicroservice(kypoTraining);
        this.entityManager.persistAndFlush(designerRole);

        traineeRole = testDataFactory.getTrainingTraineeRole();
        traineeRole.setMicroservice(kypoTraining);
        this.entityManager.persistAndFlush(traineeRole);
    }

    @Test
    public void findByRoleType() throws Exception {
        Optional<Role> optionalRole = this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        Role r = optionalRole.orElseThrow(() -> new Exception("Role should be found"));
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

    @Test
    public void finDefaultRoleOfMicroservice() throws Exception{
        IDMGroup defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setRoles(Set.of(guestRole, traineeRole));
        this.entityManager.persistAndFlush(defaultGroup);

        Role defaultUAGRole = roleRepository.findDefaultRoleOfMicroservice(kypoUserAndGroup.getName())
                .orElseThrow(() -> new Exception("Role should be found"));
        assertEquals(defaultUAGRole, guestRole);
        Role defaultTrainingRole = roleRepository.findDefaultRoleOfMicroservice(kypoTraining.getName())
                .orElseThrow(() -> new Exception("Role should be found"));
        assertEquals(defaultTrainingRole, traineeRole);
    }
}
