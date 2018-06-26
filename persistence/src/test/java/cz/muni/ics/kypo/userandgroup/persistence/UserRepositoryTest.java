package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.dbmodel"})
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
        adminRole.setRoleType(RoleType.ADMINISTRATOR);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST);

        group = new IDMGroup("group1", "group 1");
        group.setStatus(UserAndGroupStatus.VALID);

        user = new User("user");
        user.setFullName("User one");
        user.setMail("user.one@mail.com");
        user.setStatus(UserAndGroupStatus.VALID);
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
