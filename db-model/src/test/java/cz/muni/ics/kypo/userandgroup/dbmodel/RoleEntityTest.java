package cz.muni.ics.kypo.userandgroup.dbmodel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.PersistenceException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RoleEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    private String roleType = RoleType.ADMINISTRATOR.name();

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenRoleTypeIsNullShouldThrowException() {
        thrown.expect(PersistenceException.class);
        Role role = new Role();
        this.entityManager.persistFlushFind(role);
    }

    @Test
    public void saveShouldPersistData() {
        Role role = new Role();
        role.setRoleType(roleType);
        Role r = this.entityManager.persistFlushFind(role);
        assertEquals(roleType, r.getRoleType());
    }
}
