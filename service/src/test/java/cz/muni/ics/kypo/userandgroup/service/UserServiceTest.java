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

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
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

    private Pageable pageable;
    private Predicate predicate;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {

        user1 = new User();
        user1.setId(1L);
        user1.setFullName("test user1");
        user1.setLogin("user1");
        user1.setStatus(UserAndGroupStatus.VALID);

        user2 = new User();
        user2.setId(2L);
        user2.setFullName("test user2");
        user2.setLogin("user2");
        user2.setStatus(UserAndGroupStatus.VALID);

        adminGroup = new IDMGroup("adminGroup", "Administrator group");
        adminGroup.setId(1L);

        adminRole = new Role();
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());
        adminRole.setId(1L);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST.name());
        guestRole.setId(2L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getUser() {
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));

        User u = userService.get(user1.getId());
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getLogin(), u.getLogin());
        assertEquals(UserAndGroupStatus.VALID, u.getStatus());

        then(userRepository).should().findById(user1.getId());
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
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + id + " not found");
        willThrow(EntityNotFoundException.class).given(userRepository).getOne(id);
        userService.get(id);
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

        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        willThrow(UserAndGroupServiceException.class).given(userRepository).getOne(user3.getId());

        Map<User, UserDeletionStatus> response = userService.deleteUsers(idsOfUsers);
        assertEquals(UserDeletionStatus.SUCCESS, response.get(user1));
        assertEquals(UserDeletionStatus.EXTERNAL_VALID, response.get(user2));
        assertEquals(UserDeletionStatus.NOT_FOUND, response.get(user3));

        then(userRepository).should(times(3)).findById(anyLong());
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
        thrown.expectMessage("Input id must not be null");
        userService.changeAdminRole(null);
    }

    @Test
    public void isUserAdmin() {
        adminGroup.addUser(user1);
        user1.addGroup(adminGroup);
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
        thrown.expectMessage("Input id must not be null");
        userService.isUserAdmin(null);
    }

    @Test
    public void getUserByLogin() {
        given(userRepository.findByLogin(user1.getLogin())).willReturn(Optional.of(user1));

        User u = userService.getUserByLogin(user1.getLogin());
        assertEquals(user1.getId(), u.getId());
        assertEquals(user1.getFullName(), u.getFullName());
        assertEquals(user1.getLogin(), u.getLogin());
        assertEquals(user1.getStatus(), u.getStatus());

        then(userRepository).should().findByLogin(user1.getLogin());
    }

    @Test
    public void getUserByLoginNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with login " + user1.getLogin() + " could not be found");
        given(userRepository.findByLogin(anyString())).willReturn(Optional.empty());
        userService.getUserByLogin(user1.getLogin());
    }

    @Test
    public void getUserByLoginWithNullLoginShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input login must not be empty");
        userService.getUserByLogin(null);
    }

    @Test
    public void getUserByLoginWithEmptyLoginShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input login must not be empty");
        userService.getUserByLogin("");
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
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        User u = userService.getUserWithGroups(user1.getId());
        assertEquals(user1, u);
        then(userRepository).should().findById(user1.getId());
    }

    @Test
    public void getUserWithGroupsByLogin() {
        given(userRepository.findByLogin(user1.getLogin())).willReturn(Optional.of(user1));

        User u = userService.getUserWithGroups(user1.getLogin());
        assertEquals(user1, u);

        then(userRepository).should().findByLogin(user1.getLogin());
    }

    @Test
    public void getUserWithGroupsWithEmptyLoginShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input login must not be empty");
        userService.getUserWithGroups("");
    }

    @Test
    public void isUserInternal() {
        given(userRepository.existsById(user1.getId())).willReturn(true);
        given(userRepository.isUserInternal(user1.getId())).willReturn(true);
        assertTrue(userService.isUserInternal(user1.getId()));
        then(userRepository).should().isUserInternal(user1.getId());
    }

    @Test
    public void isUserExternal() {
        user1.setExternalId(1L);
        given(userRepository.existsById(user1.getId())).willReturn(true);
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
    public void isUserInternalWithUserNotFoundShouldThrowException() {
        given(userRepository.existsById(user1.getId())).willReturn(false);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found.");
        userService.isUserInternal(user1.getId());
    }

    @Test
    public void getRolesOfUser() {
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
        thrown.expectMessage("Input id must not be null");
        userService.getRolesOfUser(null);
    }

    @Test
    public void getRolesOfUserWithUserNotFoundShouldThrowException() {
        given(userRepository.existsById(user1.getId())).willReturn(false);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found.");
        userService.getRolesOfUser(user1.getId());
    }

    @After
    public void afterMethod() {
        reset(userRepository, groupRepository);
    }
}
