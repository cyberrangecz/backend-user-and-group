package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@ComponentScan(basePackages = "cz.muni.ics.kypo.userandgroup.util")
public class UserRepositoryTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    private Role adminRole, guestRole;
    private IDMGroup group1, group2;
    private User user;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setup() {
        Microservice microservice = testDataFactory.getKypoTrainingMicroservice();
        this.entityManager.persistAndFlush(microservice);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setMicroservice(microservice);
        this.entityManager.persistAndFlush(adminRole);

        guestRole = testDataFactory.getUAGGuestRole();
        guestRole.setMicroservice(microservice);
        this.entityManager.persistAndFlush(guestRole);

        group1 = testDataFactory.getUAGDefaultGroup();
        group1.setRoles(Set.of(guestRole));
        group2 = testDataFactory.getTrainingAdminGroup();
        group2.setRoles(Set.of(adminRole));

        user = testDataFactory.getUser1();
    }

    @Test
    public void getLogin() throws Exception {
        String expectedLogin = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedLogin, "https://oidc.muni.cz/oidc/"));
        Optional<String> optionalLogin = this.userRepository.getLogin(u.getId());
        String login = optionalLogin.orElseThrow(() -> new Exception("User's login should be found"));
        assertEquals(expectedLogin, login);
    }

    @Test
    public void getLoginUserNotFound() {
        assertFalse(this.userRepository.getLogin(10L).isPresent());
    }

    @Test
    public void getRolesOfUser() {
        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(guestRole);

        User u = entityManager.persistAndFlush(user);

        group1.addUser(u);
        group1.setRoles(Stream.of(adminRole).collect(Collectors.toSet()));
        IDMGroup g = entityManager.persistAndFlush(group1);


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

        for(int i=0;i<3;i++) {
            User user2 = new User(UUID.randomUUID().toString(), "https://oidc.muni.cz/oidc/");
            user2.setFullName("User two");
            user2.setMail("user.two@mail.com");
            user2.setStatus(UserAndGroupStatus.VALID);
            entityManager.persistAndFlush(user2);

            User user3 = new User(UUID.randomUUID().toString(), "https://oidc.muni.cz/oidc/");
            user3.setFullName("User three");
            user3.setMail("user.three@mail.com");
            user3.setStatus(UserAndGroupStatus.VALID);
            entityManager.persistAndFlush(user3);
        }
        List<User> usersNotInGroup = userRepository.usersNotInGivenGroup(group1.getId(), null, PageRequest.of(0, 10)).getContent();
        assertFalse(usersNotInGroup.contains(user));

    }

    @Test
    public void usersInGivenGroups() {
        entityManager.persistAndFlush(user);
        group1.addUser(user);
        IDMGroup g1 = entityManager.persistAndFlush(group1);
        IDMGroup g2 = entityManager.persistAndFlush(group2);

        User user2 = testDataFactory.getUser2();
        entityManager.persistAndFlush(user2);
        User user3 = testDataFactory.getUser3();
        entityManager.persistAndFlush(user3);
        g2.addUser(user2);

        List<User> usersInGroups = userRepository.usersInGivenGroups(Set.of(g1.getId(), g2.getId()), null, PageRequest.of(0, 10)).getContent();
        usersInGroups.forEach(System.out::println);
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
        entityManager.persistAndFlush(user);
        group1.addUser(user);
        group2.addUser(user);
        entityManager.persistAndFlush(group2);
        entityManager.persistAndFlush(group1);

        Optional<User> userWithGroups = userRepository.getUserByIdWithGroups(user.getId());
        assertEquals(2, userWithGroups.get().getGroups().size());
        assertTrue(userWithGroups.get().getGroups().contains(group1));
        assertTrue(userWithGroups.get().getGroups().contains(group2));
    }

    @Test
    public void findBySubAndIss() {
        String expectedSub = "user1";
        this.entityManager.persist(new User(expectedSub, "https://oidc.muni.cz/oidc/"));
        Optional<User> optionalUser = this.userRepository.findByLoginAndIss(expectedSub, "https://oidc.muni.cz/oidc/");
        assertTrue(optionalUser.isPresent());
        assertEquals(expectedSub, optionalUser.get().getLogin());
        assertEquals("https://oidc.muni.cz/oidc/", optionalUser.get().getIss());

    }

    @Test
    public void findBySubAndDifferentIss() {
        String expectedSub = "user1";
        this.entityManager.persist(new User(expectedSub, "https://oidc.muni.cz/oidc/"));
        Optional<User> optionalUser = this.userRepository.findByLoginAndIss(expectedSub, "https://kypo.muni.cz/oidc/");
        assertFalse(optionalUser.isPresent());

    }

}
