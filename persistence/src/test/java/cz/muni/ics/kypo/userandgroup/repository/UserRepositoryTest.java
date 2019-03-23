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
    private IDMGroup group1, group2;
    private User user;
    private Microservice microservice;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setup() {
        microservice = new Microservice();
        microservice.setEndpoint("http://kypo2-training/api/v1");
        microservice.setName("training");
        this.entityManager.persistAndFlush(microservice);

        adminRole = new Role();
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        adminRole.setMicroservice(microservice);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
        guestRole.setMicroservice(microservice);

        group1 = new IDMGroup("group1", "group1 1");
        group1.setStatus(UserAndGroupStatus.VALID);

        group2 = new IDMGroup("group2", "group1 2");
        group2.setStatus(UserAndGroupStatus.VALID);

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

        group1.setRoles(Stream.of(adminRole).collect(Collectors.toSet()));
        IDMGroup g = entityManager.persistAndFlush(group1);

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
        group1.addUser(user);
        IDMGroup g = entityManager.persistAndFlush(group1);

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

        List<User> usersNotInGroup = userRepository.usersNotInGivenGroup(group1.getId(), PageRequest.of(0, 10)).getContent();
        assertEquals(2, usersNotInGroup.size());
        assertFalse(usersNotInGroup.contains(user));
        assertTrue(usersNotInGroup.contains(user2));
        assertTrue(usersNotInGroup.contains(user3));
    }

    @Test
    public void usersInGivenGroups() {
        entityManager.persistAndFlush(user);
        group1.addUser(user);
        IDMGroup g1 = entityManager.persistAndFlush(group1);
        IDMGroup g2 = entityManager.persistAndFlush(group2);

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
        g2.addUser(user2);

        List<User> usersInGroups = userRepository.usersInGivenGroups(Set.of(g1.getId(), g2.getId()), PageRequest.of(0, 10)).getContent();
        assertEquals(2, usersInGroups.size());
        assertTrue(usersInGroups.contains(user));
        assertTrue(usersInGroups.contains(user2));
        assertFalse(usersInGroups.contains(user3));
    }

    @Test
    public void findAllByRoleId() {
        this.entityManager.persistAndFlush(user);
        this.entityManager.persistAndFlush(adminRole);
        this.entityManager.persistAndFlush(guestRole);
        group1.addUser(user);
        group1.setRoles(Set.of(adminRole));
        this.entityManager.persistAndFlush(group1);
        group2.addUser(user);
        group2.setRoles(Set.of(guestRole));
        this.entityManager.persistAndFlush(group2);

        Set<Role> roles = userRepository.getRolesOfUser(user.getId());
        assertEquals(2, roles.size());
        assertTrue(roles.contains(adminRole));
        assertTrue(roles.contains(guestRole));
    }

    @Test
    public void getUserWithGroups() {
        entityManager.persistAndFlush(group1);
        entityManager.persistAndFlush(group2);
        user.addGroup(group2);
        user.addGroup(group1);
        entityManager.persistAndFlush(user);

        Optional<User> userWithGroups = userRepository.getUserByIdWithGroups(user.getId());
        assertEquals(2, userWithGroups.get().getGroups().size());
        assertTrue(userWithGroups.get().getGroups().contains(group1));
        assertTrue(userWithGroups.get().getGroups().contains(group2));
    }

}
