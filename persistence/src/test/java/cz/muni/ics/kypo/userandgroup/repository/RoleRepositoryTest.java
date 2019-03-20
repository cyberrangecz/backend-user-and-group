package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Microservice microservice;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        microservice = new Microservice();
        microservice.setEndpoint("http://kypo2-training/api/v1");
        microservice.setName("training");
        this.entityManager.persistAndFlush(microservice);
    }

    @Test
    public void findByRoleType() throws Exception {
        Role role = new Role();
        role.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        role.setMicroservice(microservice);
        this.entityManager.persist(role);
        Optional<Role> optionalRole = this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        Role r = optionalRole.orElseThrow(() -> new Exception("Role shoul be found"));
        assertEquals(role, r);
        assertEquals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString(), r.getRoleType());
    }

    @Test
    public void findByRoleTypeNotFound() {
        assertFalse(this.roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString()).isPresent());
    }

    @Test
    public void existByRoleType() {
        Role role = new Role();
        role.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        role.setMicroservice(microservice);
        this.entityManager.persist(role);
        assertTrue(roleRepository.existsByRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString()));
        assertFalse(roleRepository.existsByRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString()));
    }
}
