package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
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

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void findByRoleType() throws Exception {
        Role role = new Role();
        role.setRoleType(RoleType.ADMINISTRATOR);
        this.entityManager.persist(role);
        Optional<Role> optionalRole = this.roleRepository.findByRoleType(RoleType.ADMINISTRATOR);
        Role r = optionalRole.orElseThrow(() -> new Exception("Role shoul be found"));
        assertEquals(role, r);
        assertEquals(RoleType.ADMINISTRATOR, r.getRoleType());
    }

    @Test
    public void findByRoleTypeNotFound() {
        assertFalse(this.roleRepository.findByRoleType(RoleType.ADMINISTRATOR).isPresent());
    }

    @Test
    public void existByRoleType() {
        Role role = new Role();
        role.setRoleType(RoleType.ADMINISTRATOR);
        this.entityManager.persist(role);
        assertTrue(roleRepository.existsByRoleType(RoleType.ADMINISTRATOR));
        assertFalse(roleRepository.existsByRoleType(RoleType.USER));
    }
}
