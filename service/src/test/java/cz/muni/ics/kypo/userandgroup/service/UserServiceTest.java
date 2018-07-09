/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz, Jan Duda 394179@mail.muni.cz
 *
 *  Date      : 31.5.2016
 *
 *  (c) Copyright 2016 MASARYK UNIVERSITY
 *  All rights reserved.
 *
 *  This software is freely available for non-commercial use under license
 *  specified in following license agreement in LICENSE file. Please review the terms
 *  of the license agreement before using this software. If you are interested in
 *  using this software commercially orin ways not allowed in aforementioned
 *  license, feel free to contact Technology transfer office of the Masaryk university
 *  in order to negotiate ad-hoc license agreement.
 */
package cz.muni.ics.kypo.userandgroup.service;

import cz.muni.ics.kypo.userandgroup.dbmodel.*;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.persistence.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
import cz.muni.ics.kypo.userandgroup.persistence.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.dbmodel"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.persistence"})
public class UserServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private IDMGroupRepository groupRepository;

    private User user1, user2;

    private IDMGroup adminGroup;

    private Role adminRole, guestRole;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {

        user1 = new User();
        user1.setId(1L);
        user1.setFullName("test user1");
        user1.setScreenName("user1");
        user1.setStatus(UserAndGroupStatus.VALID);

        user2 = new User();
        user2.setId(2L);
        user2.setFullName("test user2");
        user2.setScreenName("user2");
        user2.setStatus(UserAndGroupStatus.VALID);

        adminGroup = new IDMGroup("adminGroup", "Administrator group");
        adminGroup.setId(1L);

        adminRole = new Role();
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());
        adminRole.setId(1L);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST.name());
        guestRole.setId(2L);
    }

    @Test
    public void getUser() {
        given(userRepository.getOne(user1.getId())).willReturn(user1);

        User u = userService.get(user1.getId());
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getScreenName(), u.getScreenName());
        assertEquals(UserAndGroupStatus.VALID, u.getStatus());

        then(userRepository).should().getOne(user1.getId());
    }

    @Test
    public void getUserWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        userService.get(null);
    }

    @Test
    public void getUserNotFoundShouldThrowException() {
        Long id = 3L;
        thrown.expect(IdentityManagementException.class);
        thrown.expectMessage("User with id " + id + " not found");
        willThrow(EntityNotFoundException.class).given(userRepository).getOne(id);
        userService.get(id);
    }

    @Test
    public void createUser() {
        given(userRepository.save(user1)).willReturn(user1);

        User u = userService.create(user1);
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getScreenName(), u.getScreenName());
        assertEquals(UserAndGroupStatus.VALID, u.getStatus());

        then(userRepository).should().save(user1);
    }

    @Test
    public void createUserWithNullUserThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input user must not be null");
        userService.create(null);
    }

    @Test
    public void createUserWithNullScreenNameThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Screen name of input user must not be empty");
        User user = new User();

        userService.create(user);
    }

    @Test
    public void updateUser() {
        given(userRepository.isUserInternal(user1.getId())).willReturn(true);
        given(userRepository.save(user1)).willReturn(user1);

        User u = userService.update(user1);
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getScreenName(), u.getScreenName());
        assertEquals(UserAndGroupStatus.VALID, u.getStatus());

        then(userRepository).should().save(user1);
    }

    @Test
    public void updateUserWithNullUserShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input user must not be null");
        userService.update(null);
    }

    @Test
    public void updateUserWithNullScreenNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Screen name of input user must not be empty");
        user1.setScreenName(null);
        userService.update(user1);
    }

    @Test
    public void updateUserIsExternalShouldThrowException() {
        thrown.expect(IdentityManagementException.class);
        thrown.expectMessage("Error: External user cannot be updated");
        given(userRepository.isUserInternal(user1.getId())).willReturn(false);
        userService.update(user1);
    }

    @Test
    public void deleteUser() {
        UserDeletionStatus status = userService.delete(user1);
        assertEquals(UserDeletionStatus.SUCCESS, status);

        then(userRepository).should().delete(user1);
    }

    @Test
    public void deleteExternalUserAndValid() {
        user1.setExternalId(1L);
        assertEquals(UserDeletionStatus.EXTERNAL_VALID, userService.delete(user1));
        then(userRepository).should(never()).delete(any(User.class));
    }

    @Test
    public void deleteExternalUserAndNotValid() {
        user1.setExternalId(1L);
        user1.setStatus(UserAndGroupStatus.DELETED);
        assertEquals(UserDeletionStatus.SUCCESS, userService.delete(user1));
        then(userRepository).should().delete(any(User.class));
    }

    @Test
    public void deleteUserWithNullUser() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input user must not be null");
        userService.delete(null);
    }

    @Test
    public void deleteUsers() {
        user2.setExternalId(1L);

        User user3 = new User();
        user3.setId(3L);

        List<Long> idsOfUsers = Arrays.asList(user1.getId(), user2.getId(), user3.getId());

        given(userRepository.getOne(user1.getId())).willReturn(user1);
        given(userRepository.getOne(user2.getId())).willReturn(user2);
        willThrow(IdentityManagementException.class).given(userRepository).getOne(user3.getId());

        Map<User, UserDeletionStatus> response = userService.deleteUsers(idsOfUsers);
        assertEquals(UserDeletionStatus.SUCCESS, response.get(user1));
        assertEquals(UserDeletionStatus.EXTERNAL_VALID, response.get(user2));
        assertEquals(UserDeletionStatus.NOT_FOUND, response.get(user3));

        then(userRepository).should(times(3)).getOne(anyLong());
        then(userRepository).should().delete(user1);
        then(userRepository).should(never()).delete(user2);
        then(userRepository).should(never()).delete(user3);
    }

    @Test
    public void deleteUsersWithNullIdsShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input ids of users must not be null");
        userService.deleteUsers(null);
    }

    @Test
    public void changeAdminRoleToUser() {
        user1.addGroup(adminGroup);
        given(userRepository.getOne(user1.getId())).willReturn(user1);
        given(userRepository.getOne(user2.getId())).willReturn(user2);
        given(groupRepository.findAdministratorGroup()).willReturn(adminGroup);

        userService.changeAdminRole(user1.getId());
        assertFalse(user1.getGroups().contains(adminGroup));
        userService.changeAdminRole(user2.getId());
        assertTrue(user2.getGroups().contains(adminGroup));

        then(userRepository).should().getOne(user1.getId());
        then(userRepository).should().getOne(user2.getId());
        then(groupRepository).should(times(2)).findAdministratorGroup();
        then(userRepository).should().save(user1);
        then(userRepository).should().save(user2);
    }

    @Test
    public void changeAdminRoleToUserWithNullId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        userService.changeAdminRole(null);
    }

    @Test
    public void isUserAdmin() {
        adminGroup.addUser(user1);
        user1.addGroup(adminGroup);
        given(userRepository.getOne(user1.getId())).willReturn(user1);
        given(userRepository.getOne(user2.getId())).willReturn(user2);
        given(groupRepository.findAdministratorGroup()).willReturn(adminGroup);

        assertTrue(userService.isUserAdmin(user1.getId()));
        assertFalse(userService.isUserAdmin(user2.getId()));

        then(userRepository).should().getOne(user1.getId());
        then(userRepository).should().getOne(user2.getId());
        then(groupRepository).should(times(2)).findAdministratorGroup();
    }

    @Test
    public void isAdminRoleToUserWithNullId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        userService.isUserAdmin(null);
    }

    @Test
    public void getUserByScreenName() {
        given(userRepository.findByScreenName(user1.getScreenName())).willReturn(user1);

        User u = userService.getUserByScreenName(user1.getScreenName());
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getScreenName(), u.getScreenName());
        assertEquals(user1.getStatus(), u.getStatus());

        then(userRepository).should().findByScreenName(user1.getScreenName());
    }

    @Test
    public void getUserByScreenNameNotFoundShouldThrowException() {
        thrown.expect(IdentityManagementException.class);
        thrown.expectMessage("User with screen name " + user1.getScreenName() + " not found");
        given(userRepository.findByScreenName(anyString())).willReturn(null);
        userService.getUserByScreenName(user1.getScreenName());
    }

    @Test
    public void getUserByScreenNameWithNullScreenNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input screen name must not be empty");
        userService.getUserByScreenName(null);
    }

    @Test
    public void getUserByScreenNameWithEmptyScreenNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input screen name must not be empty");
        userService.getUserByScreenName("");
    }

    @Test
    public void getAllUsers() {
        given(userRepository.findAll()).willReturn(Arrays.asList(user1, user2));
        List<User> users = userService.getAllUsers();
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        then(userRepository).should().findAll();
    }

    @Test
    public void getUserWithGroups() {
        given(userRepository.getOne(user1.getId())).willReturn(user1);
        User u = userService.getUserWithGroups(user1.getId());
        assertEquals(user1, u);
        then(userRepository).should().getOne(user1.getId());
    }

    @Test
    public void getUserWithGroupsByScreenName() {
        given(userRepository.findByScreenName(user1.getScreenName())).willReturn(user1);

        User u = userService.getUserWithGroups(user1.getScreenName());
        assertEquals(user1, u);

        then(userRepository).should().findByScreenName(user1.getScreenName());
    }

    @Test
    public void getUserWithGroupsWithEmptyScreenNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input screen name must not be empty");
        userService.getUserWithGroups("");
    }

    @Test
    public void isUserInternal() {
        given(userRepository.isUserInternal(user1.getId())).willReturn(true);
        assertTrue(userService.isUserInternal(user1.getId()));
        then(userRepository).should().isUserInternal(user1.getId());
    }

    @Test
    public void isUserExternal() {
        user1.setExternalId(1L);
        given(userRepository.isUserInternal(user1.getId())).willReturn(false);
        assertFalse(userService.isUserInternal(user1.getId()));
        then(userRepository).should().isUserInternal(user1.getId());
    }

    @Test
    public void isUserExternalWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        userService.isUserInternal(null);
    }

    @Test
    public void getRolesOfUser() {
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
        thrown.expectMessage("Input id must not be null");
        userService.getRolesOfUser(null);
    }

    @After
    public void afterMethod() {
        reset(userRepository, groupRepository);
    }
}
