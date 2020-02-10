package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.impl.UserServiceImpl;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class UserServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private IDMGroupRepository groupRepository;

    private User user1, user2;
    private IDMGroup adminGroup, userGroup;
    private Role adminRole, guestRole;
    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        userService = new UserServiceImpl(userRepository, groupRepository, roleRepository);
        user1 = testDataFactory.getUser1();
        user2 = testDataFactory.getUser2();

        adminGroup = testDataFactory.getUAGAdminGroup();
        userGroup = testDataFactory.getUAGUserGroup();

        adminRole = testDataFactory.getUAGAdminRole();
        guestRole = testDataFactory.getUAGGuestRole();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getUser() {
        user1.setId(1L);
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));

        User u = userService.getUserById(user1.getId());
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getLogin(), u.getLogin());
        assertEquals(UserAndGroupStatus.VALID, u.getStatus());

        then(userRepository).should().findById(user1.getId());
    }

    @Test
    public void getUserWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getUserById(id) the input must not be null.");
        userService.getUserById(null);
    }

    @Test
    public void getUserNotFoundShouldThrowException() {
        Long id = 3L;
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + id + " could not be found");
        willThrow(EntityNotFoundException.class).given(userRepository).getOne(id);
        userService.getUserById(id);
    }

    @Test
    public void deleteUser() {
        userService.deleteUser(user1);
        then(userRepository).should().delete(user1);
    }

    @Test
    public void deleteUserWithNullUser() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method deleteUser(user) the input must not be null.");
        userService.deleteUser(null);
    }

    @Test
    public void changeAdminRoleToUser() {
        user1.setId(1L);
        user2.setId(2L);
        adminGroup.addUser(user1);
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(groupRepository.findAdministratorGroup()).willReturn(Optional.of(adminGroup));

        userService.changeAdminRole(user1.getId());
        assertFalse(user1.getGroups().contains(adminGroup));
        userService.changeAdminRole(user2.getId());
        assertTrue(user2.getGroups().contains(adminGroup));

        then(userRepository).should().findById(user1.getId());
        then(userRepository).should().findById(user2.getId());
        then(groupRepository).should(times(2)).findAdministratorGroup();
        then(userRepository).should().save(user1);
        then(userRepository).should().save(user2);
    }

    @Test
    public void changeAdminRoleToUserWithNullId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method changeAdminRole(id) the input must not be null.");
        userService.changeAdminRole(null);
    }

    @Test
    public void isUserAdmin() {
        user1.setId(1L);
        user2.setId(2L);
        adminGroup.addUser(user1);
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(groupRepository.findAdministratorGroup()).willReturn(Optional.of(adminGroup));

        assertTrue(userService.isUserAdmin(user1.getId()));
        assertFalse(userService.isUserAdmin(user2.getId()));

        then(userRepository).should().findById(user1.getId());
        then(userRepository).should().findById(user2.getId());
        then(groupRepository).should(times(2)).findAdministratorGroup();
    }

    @Test
    public void isAdminRoleToUserWithNullId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method isUserAdmin(id) the input must not be null.");
        userService.isUserAdmin(null);
    }

    @Test
    public void getUserBySubAndIss() {
        userGroup.addUser(user1);
        given(userRepository.findByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.of(user1));

        User u = userService.getUserByLoginAndIss(user1.getLogin(), "https://oidc.muni.cz/oidc/").get();
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getLogin(), u.getLogin());
        assertEquals(user1.getStatus(), u.getStatus());
        assertEquals(user1.getFamilyName(),u.getFamilyName());
        assertEquals(user1.getGivenName(),u.getGivenName());
        assertEquals(user1.getMail(), u.getMail());
        assertEquals(user1.getIss(), u.getIss());
        assertEquals(user1.getPicture(), u.getPicture());
        for (IDMGroup g : user1.getGroups()){
            assertTrue(u.getGroups().contains(g));
        }
        then(userRepository).should().findByLoginAndIss(user1.getLogin(), user1.getIss());
    }

    @Test
    public void getUserBySubWithNullSubShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getUserBySubAndIss(login) the input must not be null.");
        userService.getUserByLoginAndIss(null, "https://oidc.muni.cz/oidc/");
    }

    @Test
    public void getUserBySubWithNullIssShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getUserBySubAndIss(iss) the input must not be null.");
        userService.getUserByLoginAndIss(user1.getLogin(), null);
    }

    @Test
    public void getAllUsers() {
        given(userRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        List<User> users = userService.getAllUsers(predicate, pageable).getContent();
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        then(userRepository).should().findAll(predicate, pageable);
    }

    @Test
    public void getUserWithGroups() {
        user1.setId(1L);
        given(userRepository.getUserByIdWithGroups(user1.getId())).willReturn(Optional.of(user1));
        User u = userService.getUserWithGroups(user1.getId());
        assertEquals(user1, u);
        then(userRepository).should().getUserByIdWithGroups(user1.getId());
    }

    @Test
    public void getUserWithGroupsByLogin() {
        given(userRepository.getUserByLoginWithGroups(user1.getLogin(), user1.getIss())).willReturn(Optional.of(user1));

        User u = userService.getUserWithGroups(user1.getLogin(), user1.getIss());
        assertEquals(user1, u);

        then(userRepository).should().getUserByLoginWithGroups(user1.getLogin(), user1.getIss());
    }

    @Test
    public void getUserWithGroupsWithEmptyLoginShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getUserWithGroups(login, iss) the input login must not be empty.");
        userService.getUserWithGroups("", "https://oidc.muni.cz/oidc/");
    }

    @Test
    public void getRolesOfUser() {
        user1.setId(1L);
        given(userRepository.existsById(user1.getId())).willReturn(true);
        given(userRepository.getRolesOfUser(user1.getId()))
                .willReturn(Stream.of(adminRole, guestRole).collect(Collectors.toSet()));
        Set<Role> roles = userService.getRolesOfUser(user1.getId());
        assertEquals(2, roles.size());
        assertTrue(roles.contains(adminRole));
        assertTrue(roles.contains(guestRole));
        then(userRepository).should().getRolesOfUser(user1.getId());
    }

    @Test
    public void getRolesOfUserWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getRolesOfUser(id) the input must not be null.");
        userService.getRolesOfUser(null);
    }

    @Test
    public void getRolesOfUserWithUserNotFoundShouldThrowException() {
        user1.setId(1L);
        given(userRepository.existsById(user1.getId())).willReturn(false);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found.");
        userService.getRolesOfUser(user1.getId());
    }

    @Test
    public void getUsersWithGivenRole() {
        adminRole.setId(1L);
        given(userRepository.findAllByRoleId(adminRole.getId(), null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        given(roleRepository.existsById(adminRole.getId())).willReturn(true);

        Page<User> userContent = userService.getUsersWithGivenRole(adminRole.getId(), null, pageable);
        assertEquals(user1, userContent.getContent().get(0));
        then(userRepository).should().findAllByRoleId(adminRole.getId(), null, pageable);
    }

    @Test
    public void getUsersWithGivenRoleWithRoleNotFound() {
        adminRole.setId(1L);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Role with id: " + 1L + " could not be found.");
        given(roleRepository.existsById(1L)).willReturn(false);

        userService.getUsersWithGivenRole(adminRole.getId(), null, pageable);
    }

    @After
    public void afterMethod() {
        reset(userRepository, groupRepository);
    }
}
