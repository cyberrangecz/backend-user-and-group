package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.dbmodel"})
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void findByScreenName() {
        String expectedScreenName = "user1";
        this.entityManager.persist(new User(expectedScreenName));
        User u = this.userRepository.findByScreenName(expectedScreenName);
        assertEquals(expectedScreenName, u.getScreenName());
    }

    @Test
    public void findByScreenNameNotFound() {
        assertNull(this.userRepository.findByScreenName("user1"));
    }

    @Test
    public void getScreenName() {
        String expectedScreenName = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedScreenName));
        String screenName = this.userRepository.getScreenName(u.getId());
        assertEquals(expectedScreenName, screenName);
    }

    @Test
    public void getScreenNameUserNotFound() {
        assertNull(this.userRepository.getScreenName(10L));
    }

    @Test
    public void isUserInternal() {
        String expectedScreenName = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedScreenName));
        assertTrue(this.userRepository.isUserInternal(u.getId()));
    }

    @Test
    public void isUserExternal() {
        String expectedScreenName = "user1";
        User user = new User(expectedScreenName);
        user.setExternalId(1L);
        User u = this.entityManager.persistAndFlush(user);
        assertFalse(this.userRepository.isUserInternal(u.getId()));
    }

}
