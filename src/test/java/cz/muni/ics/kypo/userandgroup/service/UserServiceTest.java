package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {TestDataFactory.class})
public class UserServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;
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

    @BeforeEach
    public void init() {
        userService = new UserService(userRepository, groupRepository, roleRepository);
        user1 = testDataFactory.getUser1();
        user1.setId(1L);
        user2 = testDataFactory.getUser2();
        user2.setId(2L);

        adminGroup = testDataFactory.getUAGAdminGroup();
        adminGroup.setId(1L);
        userGroup = testDataFactory.getUAGUserGroup();
        userGroup.setId(2L);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        guestRole = testDataFactory.getUAGGuestRole();
        guestRole.setId(2L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getUserById() {
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        User u = userService.getUserById(user1.getId());
        assertUser(user1, u);
    }

    @Test
    public void getUserByIdNotFoundShouldThrowException() {
        given(userRepository.findById(user1.getId())).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(user1.getId()));
    }

    @Test
    public void getUserBySubAndIss() {
        userGroup.addUser(user1);
        given(userRepository.findBySubAndIss(user1.getSub(), user1.getIss())).willReturn(Optional.of(user1));

        Optional<User> userOptional = userService.getUserBySubAndIss(user1.getSub(), "https://oidc.muni.cz/oidc/");
        assertTrue(userOptional.isPresent());
        assertUser(user1, userOptional.get());
        for (IDMGroup g : user1.getGroups()) {
            assertTrue(userOptional.get().getGroups().contains(g));
        }
        then(userRepository).should().findBySubAndIss(user1.getSub(), user1.getIss());
    }

    @Test
    public void getAllUsers() {
        given(userRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        List<User> users = userService.getAllUsers(predicate, pageable).getContent();
        assertTrue(users.containsAll(Set.of(user1, user2)));
        then(userRepository).should().findAll(predicate, pageable);
    }

    @Test
    public void getUsersByIds() {
        given(userRepository.findByIdIn(List.of(1L, 2L))).willReturn(List.of(user1, user2));
        List<User> users = userService.getUsersByIds(Arrays.asList(1L, 2L));
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        then(userRepository).should().findByIdIn(Arrays.asList(1L, 2L));
    }

    @Test
    public void deleteUser() {
        userService.deleteUser(user1);
        then(userRepository).should().delete(user1);
    }

    @Test
    public void createUser() {
        given(userRepository.saveAndFlush(user1)).willReturn(user1);
        User user = userService.createUser(user1);
        assertEquals(user1, user);
        then(userRepository).should().saveAndFlush(user1);
    }

    @Test
    public void updateUser() {
        given(userRepository.saveAndFlush(user1)).willReturn(user1);
        User user = userService.updateUser(user1);
        assertEquals(user1, user);
        then(userRepository).should().saveAndFlush(user1);
    }

    @Test
    public void changeAdminRoleToUser() {
        adminGroup.addUser(user1);
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(groupRepository.findAdministratorGroup()).willReturn(Optional.of(adminGroup));

        userService.changeAdminRole(user1.getId());
        assertFalse(user1.getGroups().contains(adminGroup));

        then(userRepository).should().findById(user1.getId());
        then(groupRepository).should().findAdministratorGroup();
        then(userRepository).should().save(user1);
    }

    @Test
    public void changeAdminRoleFromUser() {
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(groupRepository.findAdministratorGroup()).willReturn(Optional.of(adminGroup));

        userService.changeAdminRole(user2.getId());
        assertTrue(user2.getGroups().contains(adminGroup));

        then(userRepository).should().findById(user2.getId());
        then(groupRepository).should().findAdministratorGroup();
        then(userRepository).should().save(user2);
    }

    @Test
    public void changeAdminRoleWithAdminGroupNotFound() {
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(groupRepository.findAdministratorGroup()).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.changeAdminRole(user1.getId()));
    }

    @Test
    public void isUserAdmin() {
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
    public void isUserAdminWithAdminGroupNotFound() {
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(groupRepository.findAdministratorGroup()).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.isUserAdmin(user1.getId()));
    }

    @Test
    public void getUsersWithGivenRoleAndNotWithGivenIds() {
        given(userRepository.findAllByRoleAndNotWithIds(predicate, pageable, adminRole.getRoleType(), Set.of(10L)))
                .willReturn(new PageImpl<>(Arrays.asList(user1, user2)));

        List<User> users = userService
                .getUsersWithGivenRoleAndNotWithGivenIds(adminRole.getRoleType(), Set.of(10L), predicate, pageable)
                .getContent();
        assertEquals(2, users.size());
        assertTrue(users.containsAll(Set.of(user1, user2)));
        then(userRepository).should().findAllByRoleAndNotWithIds(predicate, pageable, adminRole.getRoleType(), Set.of(10L));
    }

    @Test
    public void getAllUsersNotInGivenGroup() {
        given(userRepository.usersNotInGivenGroup(1L, predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        List<User> users = userService
                .getAllUsersNotInGivenGroup(1L, predicate, pageable)
                .getContent();
        assertEquals(2, users.size());
        assertTrue(users.containsAll(Set.of(user1, user2)));
        then(userRepository).should().usersNotInGivenGroup(1L, predicate, pageable);
    }

    @Test
    public void getUsersInGroups() {
        given(userRepository.usersInGivenGroups(Set.of(1L, 2L), predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        List<User> users = userService
                .getUsersInGroups(Set.of(1L, 2L), predicate, pageable)
                .getContent();
        assertEquals(2, users.size());
        assertTrue(users.containsAll(Set.of(user1, user2)));
        then(userRepository).should().usersInGivenGroups(Set.of(1L, 2L), predicate, pageable);
    }

    @Test
    public void getUserWithGroupsById() {
        given(userRepository.getUserByIdWithGroups(user1.getId())).willReturn(Optional.of(user1));
        User u = userService.getUserWithGroups(user1.getId());
        assertEquals(user1, u);
        then(userRepository).should().getUserByIdWithGroups(user1.getId());
    }

    @Test
    public void getUserWithGroupsByIdNotFound() {
        given(userRepository.getUserByIdWithGroups(100L)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserWithGroups(100L));
    }

    @Test
    public void getUserWithGroupsByLogin() {
        given(userRepository.getUserBySubWithGroups(user1.getSub(), user1.getIss())).willReturn(Optional.of(user1));
        User user = userService.getUserWithGroups(user1.getSub(), user1.getIss());
        assertUser(user1, user);

        then(userRepository).should().getUserBySubWithGroups(user1.getSub(), user1.getIss());
    }

    @Test
    public void getUserWithGroupsByLoginNotFound() {
        given(userRepository.getUserBySubWithGroups(user1.getSub(), user1.getIss())).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserWithGroups(user1.getSub(), user1.getIss()));
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
    public void getRolesOfUserWithUserNotFoundShouldThrowException() {
        given(userRepository.existsById(user1.getId())).willReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.getRolesOfUser(user1.getId()));
    }

    @Test
    public void getRolesOfUserWithPagination() {
        given(userRepository.existsById(user1.getId())).willReturn(true);
        given(roleRepository.findAllOfUser(eq(user1.getId()), eq(pageable), eq(predicate)))
                .willReturn(new PageImpl<>(List.of(adminRole, guestRole)));
        Page<Role> roles = userService.getRolesOfUserWithPagination(user1.getId(), pageable, predicate);
        assertEquals(2, roles.getContent().size());
        assertTrue(roles.getContent().contains(adminRole));
        assertTrue(roles.getContent().contains(guestRole));
    }

    @Test
    public void getRolesOfUserWithPaginationUserNotFound() {
        given(userRepository.existsById(user1.getId())).willReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.getRolesOfUserWithPagination(user1.getId(), pageable, predicate));
    }

    @Test
    public void getUsersWithGivenRole() {
        given(userRepository.findAllByRoleId(adminRole.getId(), null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        given(roleRepository.existsById(adminRole.getId())).willReturn(true);

        List<User> users = userService.getUsersWithGivenRole(adminRole.getId(), null, pageable).getContent();
        assertEquals(2, users.size());
        assertTrue(users.containsAll(Set.of(user1, user2)));
        then(userRepository).should().findAllByRoleId(adminRole.getId(), null, pageable);
    }

    @Test
    public void getUsersWithGivenRoleWithRoleNotFound() {
        given(roleRepository.existsById(adminRole.getId())).willReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.getUsersWithGivenRole(adminRole.getId(), null, pageable));
    }

    @Test
    public void getUsersWithGivenRoleType() {
        given(userRepository.findAllByRoleType(adminRole.getRoleType(), null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        given(roleRepository.existsByRoleType(adminRole.getRoleType())).willReturn(true);

        List<User> users = userService.getUsersWithGivenRoleType(adminRole.getRoleType(), null, pageable).getContent();
        assertEquals(2, users.size());
        assertTrue(users.containsAll(Set.of(user1, user2)));
        then(userRepository).should().findAllByRoleType(adminRole.getRoleType(), null, pageable);
    }

    @Test
    public void getUsersWithGivenRoleTypeWithRoleNotFound() {
        given(roleRepository.existsByRoleType(adminRole.getRoleType())).willReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.getUsersWithGivenRoleType(adminRole.getRoleType(), null, pageable));
    }

    @Test
    public void getUsersWithGivenIds() {
        given(userRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(new PageImpl<>(List.of(user1, user2)));
        List<User> users = userService.getUsersWithGivenIds(Set.of(user1.getId(), user2.getId()), pageable, predicate).getContent();
        assertEquals(2, users.size());
        assertTrue(users.containsAll(Set.of(user1, user2)));
    }

    public void assertUser(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFullName(), actual.getFullName());
        assertEquals(expected.getSub(), actual.getSub());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getFamilyName(), actual.getFamilyName());
        assertEquals(expected.getGivenName(), actual.getGivenName());
        assertEquals(expected.getMail(), actual.getMail());
        assertEquals(expected.getIss(), actual.getIss());
        assertEquals(expected.getPicture(), actual.getPicture());
    }

    @AfterEach
    public void afterMethod() {
        reset(userRepository, groupRepository);
    }
}
