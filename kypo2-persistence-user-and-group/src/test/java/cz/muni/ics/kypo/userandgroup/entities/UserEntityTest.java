package cz.muni.ics.kypo.userandgroup.entities;

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
public class UserEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    private String sub = "sub";

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenLoginIsNullShouldThrowException() {
        this.thrown.expect(PersistenceException.class);
        this.entityManager.persist(new User(null, "https://oidc.muni.cz/oidc/"));
    }

    @Test
    public void saveShouldPersistData() {
        User u = this.entityManager.persistFlushFind(new User(sub, "https://oidc.muni.cz/oidc/"));
        assertEquals(sub, u.getSub());
    }
}
