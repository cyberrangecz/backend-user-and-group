package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.*;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private Role adminRole, guestRole;
    private IDMGroup group;
    private User user;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setup() {
        adminRole = new Role();
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST.name());

        group = new IDMGroup("group1", "group 1");
        group.setStatus(UserAndGroupStatus.VALID);

        user = new User("user");
        user.setFullName("User one");
        user.setMail("user.one@mail.com");
        user.setStatus(UserAndGroupStatus.VALID);
    }

    @Test
    public void findByScreenName() throws Exception {
        String expectedScreenName = "user1";
        this.entityManager.persist(new User(expectedScreenName));
        Optional<User> optionalUser = this.userRepository.findByScreenName(expectedScreenName);
        User u = optionalUser.orElseThrow(() -> new Exception("User should be found"));
        assertEquals(expectedScreenName, u.getScreenName());
    }

    @Test
    public void findByScreenNameNotFound() {
        assertFalse(this.userRepository.findByScreenName("user1").isPresent());
    }

    @Test
    public void getScreenName() throws Exception {
        String expectedScreenName = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedScreenName));
        Optional<String> optionalScreenName = this.userRepository.getScreenName(u.getId());
        String screenName = optionalScreenName.orElseThrow(() -> new Exception("User's screen name should be found"));
        assertEquals(expectedScreenName, screenName);
    }

    @Test
    public void getScreenNameUserNotFound() {
        assertFalse(this.userRepository.getScreenName(10L).isPresent());
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

    @Test
    public void getRolesOfUser() {
        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(guestRole);

        group.setRoles(Stream.of(adminRole).collect(Collectors.toSet()));
        IDMGroup g = entityManager.persistAndFlush(group);

        user.addGroup(g);
        User u = entityManager.persistAndFlush(user);

        Set<Role> userRoles = userRepository.getRolesOfUser(u.getId());
        assertEquals(1, userRoles.size());
        assertTrue(userRoles.contains(adminRole));
        assertFalse(userRoles.contains(guestRole));
    }

}
