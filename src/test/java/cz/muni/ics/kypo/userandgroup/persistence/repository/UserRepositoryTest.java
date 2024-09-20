package cz.muni.ics.kypo.userandgroup.persistence.repository;

import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    private Role adminRole, guestRole, userRole;
    private IDMGroup group1, group2, group3;
    private User user1, user2, user3, user4;

    @BeforeEach
    public void setup() {
        Microservice microservice = testDataFactory.getKypoTrainingMicroservice();
        this.entityManager.persistAndFlush(microservice);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setMicroservice(microservice);
        this.entityManager.persistAndFlush(adminRole);

        guestRole = testDataFactory.getUAGTraineeRole();
        guestRole.setMicroservice(microservice);
        this.entityManager.persistAndFlush(guestRole);

        userRole = testDataFactory.getUAGPowerUserRole();
        userRole.setMicroservice(microservice);
        this.entityManager.persistAndFlush(userRole);

        group1 = testDataFactory.getUAGDefaultGroup();
        group1.setRoles(Set.of(guestRole));
        group2 = testDataFactory.getTrainingAdminGroup();
        group2.setRoles(Set.of(adminRole));
        group3 = testDataFactory.getUAGPowerUserGroup();
        group3.setRoles(Set.of(userRole));

        user1 = testDataFactory.getUser1();
        user2 = testDataFactory.getUser2();
        user3 = testDataFactory.getUser3();
        user4 = testDataFactory.getUser4();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.persistAndFlush(user4);
    }

    @Test
    public void getLogin() throws Exception {
        String expectedLogin = "user1";
        User u = this.entityManager.persistAndFlush(new User(expectedLogin, "https://oidc.muni.cz/oidc/"));
        Optional<String> optionalLogin = this.userRepository.getSub(u.getId());
        String sub = optionalLogin.orElseThrow(() -> new Exception("User's sub should be found"));
        assertEquals(expectedLogin, sub);
    }

    @Test
    public void getLoginUserNotFound() {
        assertFalse(this.userRepository.getSub(100000000L).isPresent());
    }

    @Test
    public void getRolesOfUser() {
        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(guestRole);

        group1.addUser(user1);
        group1.setRoles(Stream.of(adminRole).collect(Collectors.toSet()));
        entityManager.persistAndFlush(group1);

        Set<Role> userRoles = userRepository.getRolesOfUser(user1.getId());
        assertEquals(1, userRoles.size());
        assertTrue(userRoles.contains(adminRole));
        assertFalse(userRoles.contains(guestRole));
    }

    @Test
    public void getUserByLoginWithGroups() {
        group1.addUser(user1);
        group2.addUser(user1);
        entityManager.persistAndFlush(group1);
        entityManager.persistAndFlush(group2);
        entityManager.persistAndFlush(group3);

        Optional<User> userWithGroups = userRepository.getUserBySubWithGroups(user1.getSub(), user1.getIss());
        assertTrue(userWithGroups.isPresent());
        assertTrue(userWithGroups.get().getGroups().contains(group1));
        assertTrue(userWithGroups.get().getGroups().contains(group2));
        assertFalse(userWithGroups.get().getGroups().contains(group3));
    }

    @Test
    public void usersNotInGivenGroup() {
        group1.addUser(user1);
        group1.addUser(user3);
        entityManager.persistAndFlush(group1);

        List<User> usersNotInGroup = userRepository.usersNotInGivenGroup(group1.getId(), null, PageRequest.of(0, 10)).getContent();
        assertFalse(usersNotInGroup.containsAll(Set.of(user1, user3)));
        assertTrue(usersNotInGroup.containsAll(Set.of(user2, user4)));
    }

    @Test
    public void usersInGivenGroups() {
        group1.setUsers(Set.of(user1, user2));
        entityManager.persistAndFlush(group1);
        group2.setUsers(Set.of(user2, user4));
        entityManager.persistAndFlush(group2);

        List<User> usersInGroups = userRepository.usersInGivenGroups(Set.of(group1.getId(), group2.getId()), null, PageRequest.of(0, 10)).getContent();
        assertEquals(3, usersInGroups.size());
        assertTrue(usersInGroups.containsAll(Set.of(user1, user2, user4)));
        assertFalse(usersInGroups.contains(user3));
    }

    @Test
    public void findAllByRoleId() {
        this.entityManager.persistAndFlush(adminRole);
        this.entityManager.persistAndFlush(guestRole);
        group1.setUsers(Set.of(user2, user3));
        group1.setRoles(Set.of(adminRole, guestRole));
        this.entityManager.persistAndFlush(group1);
        group2.setUsers(Set.of(user1, user3));
        group2.setRoles(Set.of(guestRole));
        this.entityManager.persistAndFlush(group2);

        Page<User> usersWithGuestRole = userRepository.findAllByRoleId(guestRole.getId(), null, PageRequest.of(0, 10));
        assertEquals(3, usersWithGuestRole.getContent().size());
        assertTrue(usersWithGuestRole.getContent().containsAll(Set.of(user1, user2, user3)));
        assertFalse(usersWithGuestRole.getContent().contains(user4));

        Page<User> usersWithAdminRoleRole = userRepository.findAllByRoleId(adminRole.getId(), null, PageRequest.of(0, 10));
        assertEquals(2, usersWithAdminRoleRole.getContent().size());
        assertTrue(usersWithAdminRoleRole.getContent().containsAll(Set.of(user2, user3)));
        assertFalse(usersWithAdminRoleRole.getContent().containsAll(Set.of(user1, user4)));
    }

    @Test
    public void findAllByRoleType() {
        this.entityManager.persistAndFlush(adminRole);
        this.entityManager.persistAndFlush(guestRole);
        group1.setUsers(Set.of(user2, user3));
        group1.setRoles(Set.of(adminRole, guestRole));
        this.entityManager.persistAndFlush(group1);
        group2.setUsers(Set.of(user1, user3));
        group2.setRoles(Set.of(guestRole));
        this.entityManager.persistAndFlush(group2);

        Page<User> usersWithGuestRole = userRepository.findAllByRoleType(guestRole.getRoleType(), null, PageRequest.of(0, 10));
        assertEquals(3, usersWithGuestRole.getContent().size());
        assertTrue(usersWithGuestRole.getContent().containsAll(Set.of(user1, user2, user3)));
        assertFalse(usersWithGuestRole.getContent().contains(user4));

        Page<User> usersWithAdminRoleRole = userRepository.findAllByRoleType(adminRole.getRoleType(), null, PageRequest.of(0, 10));
        assertEquals(2, usersWithAdminRoleRole.getContent().size());
        assertTrue(usersWithAdminRoleRole.getContent().containsAll(Set.of(user2, user3)));
        assertFalse(usersWithAdminRoleRole.getContent().containsAll(Set.of(user1, user4)));
    }

    @Test
    public void findAllByRoleAndNotWithIds() {
        this.entityManager.persistAndFlush(adminRole);
        this.entityManager.persistAndFlush(guestRole);
        group1.setUsers(Set.of(user2, user3));
        group1.setRoles(Set.of(adminRole, guestRole));
        this.entityManager.persistAndFlush(group1);
        group2.setUsers(Set.of(user1, user3));
        group2.setRoles(Set.of(guestRole));
        this.entityManager.persistAndFlush(group2);

        Page<User> usersWithGuestRole = userRepository.findAllByRoleAndNotWithIds(null, PageRequest.of(0, 10), guestRole.getRoleType(), Set.of(user3.getId(), user4.getId()));
        assertEquals(2, usersWithGuestRole.getContent().size());
        assertTrue(usersWithGuestRole.getContent().containsAll(Set.of(user1, user2)));
        assertFalse(usersWithGuestRole.getContent().contains(user4));

        Page<User> usersWithAdminRoleRole = userRepository.findAllByRoleAndNotWithIds(null, PageRequest.of(0, 10), adminRole.getRoleType(), Set.of(user3.getId(), user2.getId()));
        assertEquals(0, usersWithAdminRoleRole.getContent().size());
        assertFalse(usersWithAdminRoleRole.getContent().containsAll(Set.of(user1, user4, user2, user3)));
    }

    @Test
    public void findAllWithGivenIds() {
        Page<User> usersPage = userRepository.findAllWithGivenIds(Set.of(user1.getId(), user4.getId()), PageRequest.of(0, 5));
        assertEquals(2, usersPage.getContent().size());
        assertTrue(usersPage.getContent().contains(user1));
        assertTrue(usersPage.getContent().contains(user4));
    }

    @Test
    public void getUserByIdWithGroups() {
        group1.addUser(user1);
        group2.addUser(user1);
        entityManager.persistAndFlush(group1);
        entityManager.persistAndFlush(group2);
        entityManager.persistAndFlush(group3);

        Optional<User> userWithGroups = userRepository.getUserByIdWithGroups(user1.getId());
        assertTrue(userWithGroups.isPresent());
        assertEquals(2, userWithGroups.get().getGroups().size());
        assertTrue(userWithGroups.get().getGroups().contains(group1));
        assertTrue(userWithGroups.get().getGroups().contains(group2));
        assertFalse(userWithGroups.get().getGroups().contains(group3));
    }

    @Test
    public void findBySubAndIss() {
        String expectedSub = "user1";
        this.entityManager.persist(new User(expectedSub, "https://oidc.muni.cz/oidc/"));
        Optional<User> optionalUser = this.userRepository.findBySubAndIss(expectedSub, "https://oidc.muni.cz/oidc/");
        assertTrue(optionalUser.isPresent());
        assertEquals(expectedSub, optionalUser.get().getSub());
        assertEquals("https://oidc.muni.cz/oidc/", optionalUser.get().getIss());

    }

    @Test
    public void findBySubAndDifferentIss() {
        String expectedSub = "user1";
        this.entityManager.persist(new User(expectedSub, "https://oidc.muni.cz/oidc/"));
        Optional<User> optionalUser = this.userRepository.findBySubAndIss(expectedSub, "https://kypo.muni.cz/oidc/");
        assertFalse(optionalUser.isPresent());

    }

}
