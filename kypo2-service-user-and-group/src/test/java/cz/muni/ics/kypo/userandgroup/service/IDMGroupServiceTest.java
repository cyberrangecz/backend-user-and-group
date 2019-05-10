package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.impl.IDMGroupServiceImpl;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
public class IDMGroupServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private IDMGroupService groupService;
    @MockBean
    private IDMGroupRepository groupRepository;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private MicroserviceRepository microserviceRepository;
    @MockBean
    private UserRepository userRepository;

    private IDMGroup group1, group2, mainGroup;
    private Role adminRole, userRole, guestRole;
    private User user1, user2, user3;

    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        groupService = new IDMGroupServiceImpl(groupRepository, roleRepository, userRepository);

        group1 = new IDMGroup("group1", "Great group1");
        group1.setId(1L);

        group2 = new IDMGroup("group2", "Great group2");
        group2.setId(2L);

        mainGroup = new IDMGroup("ROLE_USER_AND_GROUP_ADMINISTRATOR", "Main group of administrators");
        mainGroup.setId(3L);

        adminRole = new Role();
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        adminRole.setId(1L);

        userRole = new Role();
        userRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
        userRole.setId(2L);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
        guestRole.setId(3L);

        user1 = new User("user1");
        user1.setId(1L);
        user1.setFullName("User One");
        user1.setMail("user.one@mail.com");
        user1.setStatus(UserAndGroupStatus.VALID);

        user2 = new User("user2");
        user2.setId(2L);
        user2.setFullName("User Two");
        user2.setMail("user.two@mail.com");
        user2.setStatus(UserAndGroupStatus.VALID);

        user3 = new User("user3");
        user3.setId(3L);
        user3.setFullName("User Three");
        user3.setMail("user.three@mail.com");
        user3.setStatus(UserAndGroupStatus.VALID);

        group1.setRoles(new HashSet<>(Arrays.asList(adminRole, userRole)));
        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getGroup() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        IDMGroup g = groupService.get(group1.getId());
        deepEquals(group1, g);

        then(groupRepository).should().findById(group1.getId());
    }

    @Test
    public void getGroupNotFoundShouldThrowException() {
        Long id = 3L;
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("IDM group with id " + id + " not found");
        willThrow(EntityNotFoundException.class).given(groupRepository).getOne(id);
        groupService.get(id);
    }

    @Test
    public void getGroupWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        groupService.get(null);
    }

    @Test
    public void createGroup() {
        given(groupRepository.save(group1)).willReturn(group1);
        given(roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString())).willReturn(Optional.ofNullable(guestRole));
        IDMGroup g = groupService.create(group1, new ArrayList<>());
        deepEquals(group1, g);

        then(groupRepository).should().save(group1);
    }

    @Test
    public void createGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input group must not be null");
        groupService.create(null, new ArrayList<>());
    }

    @Test
    public void updateGroup() {
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(groupRepository.save(group1)).willReturn(group1);
        IDMGroup g = groupService.update(group1);
        deepEquals(group1, g);

        then(groupRepository).should().save(group1);
    }

    @Test
    public void updateGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input group must not be null");
        groupService.update(null);
    }

    @Test
    public void testDeleteGroupSuccess() {
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        assertEquals(GroupDeletionStatusDTO.SUCCESS, groupService.delete(group1));
        then(groupRepository).should().delete(group1);
    }

    @Test
    public void testDeleteGroupErrorMainGroup() {
        given(roleRepository.findAll()).willReturn(Collections.singletonList(adminRole));
        assertEquals(GroupDeletionStatusDTO.ERROR_MAIN_GROUP, groupService.delete(mainGroup));
        then(groupRepository).should(never()).delete(mainGroup);
    }

    @Test
    public void deleteGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input group must not be null");
        groupService.delete(null);
    }

    @Test
    public void getAllGroups() {
        given(groupRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(group1, group2)));

        // do not create user3
        IDMGroup group3 = new IDMGroup("Participants", "thrird group");

        List<IDMGroup> groups = groupService.getAllIDMGroups(predicate, pageable).getContent();
        assertEquals(2, groups.size());
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));
        assertFalse(groups.contains(group3));

        then(groupRepository).should().findAll(predicate, pageable);
    }

    @Test
    public void getIDMGroupByName() {
        given(groupRepository.findByName(group1.getName())).willReturn(Optional.of(group1));

        IDMGroup group = groupService.getIDMGroupByName(group1.getName());
        deepEquals(group1, group);
        assertNotEquals(group2, group);

        then(groupRepository).should().findByName(group1.getName());
    }

    @Test
    public void getIDMGroupByNameNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("IDM Group with name " + group1.getName() + " not found");
        given(groupRepository.findByName(group1.getName())).willReturn(Optional.empty());
        groupService.getIDMGroupByName(group1.getName());
    }

    @Test
    public void getGroupByNameWithNullNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input name of group must not be empty");
        groupService.getIDMGroupByName(null);
    }

    @Test
    public void getGroupByNameWithEmptyNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input name of group must not be empty");
        groupService.getIDMGroupByName("");
    }

    @Test
    public void isGroupInternal() {
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        assertTrue(groupService.isGroupInternal(group1.getId()));
        then(groupRepository).should().isIDMGroupInternal(group1.getId());
    }

    @Test
    public void isGroupExternal() {
        group1.setExternalId(1L);
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(false);
        assertFalse(groupService.isGroupInternal(group1.getId()));
        then(groupRepository).should().isIDMGroupInternal(group1.getId());
    }

    @Test
    public void isGroupExternalWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        groupService.isGroupInternal(null);
    }

    @Test
    public void getRolesOfGroup() {
        given(groupRepository.findById(group1.getId()))
                .willReturn(Optional.ofNullable(group1));
        Set<Role> roles = groupService.getRolesOfGroup(group1.getId());
        assertEquals(2, roles.size());
        assertTrue(roles.contains(adminRole));
        assertTrue(roles.contains(userRole));
        then(groupRepository).should().findById(group1.getId());
    }

    @Test
    public void getRolesOfGroupWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        groupService.getRolesOfGroup(null);
    }

    @Test
    public void getRolesOfGroupWithGroupNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group with id: " + group1.getId() + " could not be found.");
        groupService.getRolesOfGroup(group1.getId());
    }

    @Test
    public void assignRole() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(roleRepository.findById(adminRole.getId())).willReturn(Optional.of(adminRole));
        given(groupRepository.save(group1)).willReturn(group1);

        IDMGroup g = groupService.assignRole(group1.getId(), adminRole.getId());

        assertTrue(g.getRoles().contains(adminRole));
        assertEquals(group1, g);

        then(groupRepository).should().findById(group1.getId());
        then(groupRepository).should().save(group1);
    }

    @Test
    public void assignRoleWihtInputGroupIdIsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.assignRole(null, 1L);
    }

    @Test
    public void assignRoleWihtInputRoleIdIsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input roleId must not be null");
        groupService.assignRole(1L, null);
    }

    @Test
    public void assignRoleWihtGroupNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group with " + group1.getId() + " could not be found.");
        given(groupRepository.findById(group1.getId())).willReturn(Optional.empty());
        groupService.assignRole(group1.getId(), 1L);
    }

    @Test
    public void removeUsers() {
        group1.addUser(user1);
        group1.addUser(user2);
        group1.addUser(user3);

        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(groupRepository.save(group1)).willReturn(group1);

        IDMGroup g = groupService.removeUsers(group1.getId(), Arrays.asList(user1.getId(), group2.getId()));

        assertEquals(group1, g);
        assertFalse(g.getUsers().contains(user1));
        assertFalse(g.getUsers().contains(user2));
        assertTrue(g.getUsers().contains(user3));

        then(groupRepository).should().isIDMGroupInternal(group1.getId());
        then(groupRepository).should().findById(group1.getId());
        then(userRepository).should().findById(user1.getId());
        then(userRepository).should().findById(user2.getId());
        then(groupRepository).should().save(group1);
    }

    @Test
    public void removeUsersWithGroupIdNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.removeUsers(null, Collections.singletonList(1L));
    }

    @Test
    public void removeUsersWithUserIdsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input list of users ids must not be null");
        groupService.removeUsers(1L, null);
    }

    @Test
    public void removeUsersWithGroupIsExternalShouldThrowException() {
        group1.setExternalId(123L);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(false);
        thrown.expect(ExternalSourceException.class);
        thrown.expectMessage("Group is external therefore it could not be updated");
        groupService.removeUsers(group1.getId(), Arrays.asList(user1.getId(), user2.getId()));
    }

    @Test
    public void removeUsersWithUserNotFoundShouldThrowException() {
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found");
        groupService.removeUsers(group1.getId(), Arrays.asList(user1.getId(), user2.getId()));
    }

    @Test
    public void addUsers() {
        group2.addUser(user2);

        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(groupRepository.findById(group2.getId())).willReturn(Optional.of(group2));
        given(groupRepository.save(group1)).willReturn(group1);

        IDMGroup g = groupService.addUsers(group1.getId(), Collections.singletonList(group2.getId()), Collections.singletonList(user1.getId()));

        assertEquals(group1, g);
        assertTrue(g.getUsers().contains(user1));
        assertTrue(g.getUsers().contains(user2));
        assertFalse(g.getUsers().contains(user3));

        then(groupRepository).should().isIDMGroupInternal(group1.getId());
        then(groupRepository).should().findById(group1.getId());
        then(userRepository).should().findById(user1.getId());
        then(groupRepository).should().findById(group2.getId());
        then(groupRepository).should().save(group1);
    }

    @Test
    public void addUsersWithGroupIdNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.addUsers(null, Collections.singletonList(1L), Collections.singletonList(1L));
    }

    @Test
    public void addUsersWithListOfGroupsIdsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input list of groups ids must not be null");
        groupService.addUsers(1L, null, Collections.singletonList(1L));
    }

    @Test
    public void addUsersWithUserIdsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input list of users ids must not be null");
        groupService.addUsers(1L, Collections.singletonList(1L), null);
    }

    @Test
    public void addUsersWithGroupIsExternalShouldThrowException() {
        group1.setExternalId(123L);
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(false);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        thrown.expect(ExternalSourceException.class);
        thrown.expectMessage("Group is external therefore it could not be updated");
        groupService.addUsers(group1.getId(), Collections.singletonList(group2.getId()), Arrays.asList(user1.getId(), user2.getId()));
    }

    @Test
    public void addUsersWithUserNotFoundShouldThrowException() {
        given(groupRepository.isIDMGroupInternal(group1.getId())).willReturn(true);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found");
        groupService.addUsers(group1.getId(), Collections.singletonList(group2.getId()), Arrays.asList(user1.getId(), user2.getId()));
    }

    private void deepEquals(IDMGroup expectedGroup, IDMGroup actualGroup) {
        assertEquals(expectedGroup.getId(), actualGroup.getId());
        assertEquals(expectedGroup.getName(), actualGroup.getName());
        assertEquals(expectedGroup.getDescription(), actualGroup.getDescription());
        assertEquals(expectedGroup.getStatus(), actualGroup.getStatus());
    }

    @After
    public void afterMethod() {
        reset(groupRepository, userRepository, roleRepository, microserviceRepository);
    }
}
