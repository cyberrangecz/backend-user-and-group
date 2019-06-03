package cz.muni.ics.kypo.userandgroup.model;

import org.junit.Before;
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

    private RoleType roleType1 = RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR;
    private RoleType roleType2 = RoleType.ROLE_USER_AND_GROUP_USER;
    private Microservice microservice;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setup() throws RuntimeException {
        microservice = new Microservice();
        microservice.setEndpoint("http://kypo2-training/api/v1");
        microservice.setName("training");
        this.entityManager.persistAndFlush(microservice);

    }

    @Test
    public void createWhenMicroserviceIsNullShouldThrowException() {
        thrown.expect(PersistenceException.class);
        Role role = new Role();
        role.setRoleType(roleType1.toString());
        this.entityManager.persistFlushFind(role);
    }


    @Test
    public void createWhenRoleTypeIsNullShouldThrowException() {
        thrown.expect(PersistenceException.class);
        Role role = new Role();
        role.setMicroservice(microservice);
        this.entityManager.persistFlushFind(role);
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
