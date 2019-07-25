package cz.muni.ics.kypo.userandgroup.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    private String login = "login";

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenLoginIsNullShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Login must not be empty");
        new User(null, "https://oidc.muni.cz/oidc/");
    }

    @Test
    public void createWhenLoginIsEmptyShouldThrowException() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage("Login must not be empty");
        new User("", "https://oidc.muni.cz/oidc/");
    }

    @Test
    public void saveShouldPersistData() {
        User u = this.entityManager.persistFlushFind(new User(login, "https://oidc.muni.cz/oidc/"));
        assertEquals(login, u.getLogin());
    }
}
