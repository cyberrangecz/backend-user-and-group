package cz.muni.ics.kypo.userandgroup.persistence.domain;

import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class RoleEntityTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;

    private final RoleType roleType1 = RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR;
    private final RoleType roleType2 = RoleType.ROLE_USER_AND_GROUP_USER;
    private Microservice microservice;

    @BeforeEach
    public void setup() throws RuntimeException {
        microservice = testDataFactory.getKypoUaGMicroservice();
        microservice.setName("training");
        this.entityManager.persistAndFlush(microservice);

    }

    @Test
    public void createWhenMicroserviceIsNullShouldThrowException() {
        Role role = new Role();
        role.setRoleType(roleType1.toString());
        assertThrows(PersistenceException.class, () -> this.entityManager.persistFlushFind(role));
    }

    @Test
    public void createWhenRoleTypeIsNullShouldThrowException() {
        Role role = new Role();
        role.setMicroservice(microservice);
        assertThrows(PersistenceException.class, () -> this.entityManager.persistFlushFind(role));
    }

    @Test
    public void saveShouldPersistData() {
        Role role2 = new Role();
        role2.setRoleType(roleType2.toString());
        role2.setMicroservice(microservice);
        role2.setDescription("This role will allow you ...");
        this.entityManager.persistAndFlush(role2);

        Role role1 = new Role();
        role1.setRoleType(roleType1.toString());
        role1.setMicroservice(microservice);
        role1.setDescription("This role will allow you ...");
        Role r = this.entityManager.persistFlushFind(role1);
        assertEquals(roleType1.toString(), r.getRoleType());
    }
}
