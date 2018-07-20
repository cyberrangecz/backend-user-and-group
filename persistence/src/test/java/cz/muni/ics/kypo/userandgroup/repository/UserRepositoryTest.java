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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
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
    public void findByLogin() throws Exception {
        String expectedLogin = "user1";
        this.entityManager.persist(new User(expectedLogin));
        Optional<User> optionalUser = this.userRepository.findByLogin(expectedLogin);
        User u = optionalUser.orElseThrow(() -> new Exception("User should be found"));
        assertEquals(expectedLogin, u.getLogin());
    }

    @Test
    public void findByLoginNotFound() {
        assertFalse(this.userRepository.findByLogin("user1").isPresent());
    }

    @Test
    public void getLogin() throws Exception {
        String expectedLogin = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedLogin));
        Optional<String> optionalLogin = this.userRepository.getLogin(u.getId());
        String login = optionalLogin.orElseThrow(() -> new Exception("User's login should be found"));
        assertEquals(expectedLogin, login);
    }

    @Test
    public void getLoginUserNotFound() {
        assertFalse(this.userRepository.getLogin(10L).isPresent());
    }

    @Test
    public void isUserInternal() {
        String expectedLogin = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedLogin));
        assertTrue(this.userRepository.isUserInternal(u.getId()));
    }

    @Test
    public void isUserExternal() {
        String expectedLogin = "user1";
        User user = new User(expectedLogin);
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

    @Test
    public void usersNotInGivenGroup() {
        entityManager.persistAndFlush(user);
        group.addUser(user);
        IDMGroup g = entityManager.persistAndFlush(group);

        User user2 = new User("user2");
        user2.setFullName("User two");
        user2.setMail("user.two@mail.com");
        user2.setStatus(UserAndGroupStatus.VALID);
        entityManager.persistAndFlush(user2);
        User user3 = new User("user3");
        user3.setFullName("User three");
        user3.setMail("user.three@mail.com");
        user3.setStatus(UserAndGroupStatus.VALID);
        entityManager.persistAndFlush(user3);

        List<User> usersNotInGroup = userRepository.usersNotInGivenGroup(group.getId(), PageRequest.of(0, 10)).getContent();
        assertEquals(2, usersNotInGroup.size());
        assertFalse(usersNotInGroup.contains(user));
        assertTrue(usersNotInGroup.contains(user2));
        assertTrue(usersNotInGroup.contains(user3));
    }

}
