package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.impl.IDMGroupServiceImpl;
import cz.muni.ics.kypo.userandgroup.service.impl.SecurityService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class IDMGroupServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;
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
    private SecurityService securityService;
    @MockBean
    private UserRepository userRepository;

    private IDMGroup defaultGroup, designerGroup, adminGroup;
    private Role adminRole, userRole, guestRole;
    private User user1, user2;

    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        groupService = new IDMGroupServiceImpl(groupRepository, roleRepository, securityService);

        defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setId(1L);
        designerGroup = testDataFactory.getTrainingDesignerGroup();
        designerGroup.setId(2L);
        adminGroup = testDataFactory.getUAGAdminGroup();
        adminGroup.setId(3L);

        adminRole = testDataFactory.getUAGAdminRole();
        adminRole.setId(1L);
        userRole = testDataFactory.getUAGUserRole();
        userRole.setId(2L);
        guestRole = testDataFactory.getUAGGuestRole();
        guestRole.setId(3L);

        user1 = testDataFactory.getUser1();
        user1.setId(1L);
        user2 = testDataFactory.getUser2();
        user2.setId(2L);

        defaultGroup.setRoles(new HashSet<>(Arrays.asList(adminRole, userRole)));
        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getGroup() {
        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.of(defaultGroup));
        IDMGroup g = groupService.getGroupById(defaultGroup.getId());
        deepEquals(defaultGroup, g);

        then(groupRepository).should().findById(defaultGroup.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getGroupNotFoundShouldThrowException() {
        Long id = 3L;
        given(groupRepository.findById(id)).willReturn(Optional.empty());
        groupService.getGroupById(id);
    }

    @Test
    public void getGroupsByIds(){
        given(groupRepository.findByIdIn(List.of(defaultGroup.getId(), designerGroup.getId()))).willReturn(List.of(defaultGroup, designerGroup));
        List<IDMGroup> idmGroups = groupService.getGroupsByIds(List.of(defaultGroup.getId(), designerGroup.getId()));
        assertEquals(2, idmGroups.size());
        assertTrue(idmGroups.containsAll(Set.of(defaultGroup, designerGroup)));
        then(groupRepository).should().findByIdIn(List.of(defaultGroup.getId(), designerGroup.getId()));
    }

    @Test
    public void getGroupForDefaultRoles() {
        given(groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName())).willReturn(Optional.of(adminGroup));
        IDMGroup group = groupService.getGroupForDefaultRoles();
        deepEquals(group, adminGroup);

        then(groupRepository).should().findByName(ImplicitGroupNames.DEFAULT_GROUP.getName());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getGroupForDefaultRolesGroupNotFound() {
        given(groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName())).willReturn(Optional.empty());
        groupService.getGroupForDefaultRoles();
    }

    @Test
    public void createGroup() {
        designerGroup.setUsers(new HashSet<>(Set.of(user1)));
        given(groupRepository.save(designerGroup)).willReturn(designerGroup);
        given(groupRepository.findUsersOfGivenGroups(List.of(defaultGroup.getId()))).willReturn(Set.of(user2));

        IDMGroup g = groupService.createIDMGroup(designerGroup, List.of(defaultGroup.getId()));
        deepEquals(designerGroup, g);
        assertTrue(g.getUsers().containsAll(Set.of(user1, user2)));
        then(groupRepository).should().save(designerGroup);
    }

    @Test(expected = EntityConflictException.class)
    public void createGroupThatAlreadyExists(){
        given(groupRepository.existsByName(defaultGroup.getName())).willReturn(true);
        groupService.createIDMGroup(defaultGroup, new ArrayList<>());
    }

    @Test
    public void updateGroup() {
        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.of(defaultGroup));
        given(groupRepository.save(defaultGroup)).willReturn(defaultGroup);
        IDMGroup g = groupService.updateIDMGroup(defaultGroup);
        deepEquals(defaultGroup, g);

        then(groupRepository).should().save(defaultGroup);
    }

    @Test(expected = EntityConflictException.class)
    public void updateGroupWithMainGroup(){
        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.of(adminGroup));
        groupService.updateIDMGroup(defaultGroup);

    }

    @Test
    public void testDeleteGroupSuccess() {
        groupService.deleteIDMGroup(designerGroup);
        then(groupRepository).should().delete(designerGroup);
    }

    @Test(expected = EntityConflictException.class)
    public void testDeleteGroupErrorMainGroup() {
        given(roleRepository.findAll()).willReturn(Collections.singletonList(adminRole));
        groupService.deleteIDMGroup(adminGroup);
        then(groupRepository).should(never()).delete(adminGroup);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteGroupWithUsers() {
        designerGroup.setUsers(Set.of(user1));
        groupService.deleteIDMGroup(designerGroup);
    }

    @Test
    public void getAllGroups() {
        given(groupRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(defaultGroup, designerGroup)));
        List<IDMGroup> groups = groupService.getAllIDMGroups(predicate, pageable).getContent();
        assertEquals(2, groups.size());
        assertTrue(groups.containsAll(Set.of(defaultGroup, designerGroup)));
        then(groupRepository).should().findAll(predicate, pageable);
    }

    @Test
    public void getIDMGroupByName() {
        given(groupRepository.findByName(defaultGroup.getName())).willReturn(Optional.of(defaultGroup));
        IDMGroup group = groupService.getIDMGroupByName(defaultGroup.getName());
        deepEquals(defaultGroup, group);
        assertNotEquals(designerGroup, group);

        then(groupRepository).should().findByName(defaultGroup.getName());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getIDMGroupByNameNotFoundShouldThrowException() {
        given(groupRepository.findByName(defaultGroup.getName())).willReturn(Optional.empty());
        groupService.getIDMGroupByName(defaultGroup.getName());
    }

    @Test
    public void getIDMGroupByNameWithRoles() {
        given(groupRepository.findByNameWithRoles(defaultGroup.getName())).willReturn(Optional.of(defaultGroup));

        IDMGroup group = groupService.getIDMGroupWithRolesByName(defaultGroup.getName());
        deepEquals(defaultGroup, group);
        assertNotEquals(designerGroup, group);
        then(groupRepository).should().findByNameWithRoles(defaultGroup.getName());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getIDMGroupByNameWithRolesNotFound() {
        given(groupRepository.findByNameWithRoles(defaultGroup.getName())).willReturn(Optional.empty());
        groupService.getIDMGroupWithRolesByName(defaultGroup.getName());
    }

    @Test
    public void getRolesOfGroup() {
        given(groupRepository.findById(defaultGroup.getId()))
                .willReturn(Optional.ofNullable(defaultGroup));
        Set<Role> roles = groupService.getRolesOfGroup(defaultGroup.getId());
        assertEquals(2, roles.size());
        assertTrue(roles.containsAll(Set.of(adminRole, userRole)));
        then(groupRepository).should().findById(defaultGroup.getId());
    }

    @Test
    public void assignRole() {
        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.of(defaultGroup));
        given(roleRepository.findById(adminRole.getId())).willReturn(Optional.of(adminRole));
        given(groupRepository.save(defaultGroup)).willReturn(defaultGroup);

        IDMGroup g = groupService.assignRole(defaultGroup.getId(), adminRole.getId());

        assertTrue(g.getRoles().contains(adminRole));
        assertEquals(defaultGroup, g);
        then(groupRepository).should().findById(defaultGroup.getId());
        then(groupRepository).should().save(defaultGroup);
    }

    @Test(expected = EntityNotFoundException.class)
    public void assignRoleWithGroupNotFoundShouldThrowException() {
        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.empty());
        groupService.assignRole(defaultGroup.getId(), 1L);
    }

    @Test
    public void removeRoleFromGroup() {
        Role traineeRole = testDataFactory.getTrainingTraineeRole();
        traineeRole.setId(2L);
        designerGroup.setRoles(new HashSet<>(Set.of(adminRole, traineeRole)));

        given(groupRepository.findById(designerGroup.getId())).willReturn(Optional.of(designerGroup));
        given(groupRepository.save(designerGroup)).willReturn(designerGroup);
        groupService.removeRoleFromGroup(designerGroup.getId(), traineeRole.getId());

        assertFalse(designerGroup.getRoles().containsAll(Set.of(traineeRole, adminRole)));
        then(groupRepository).should().save(designerGroup);
        then(groupRepository).should().findById(designerGroup.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void removeRoleFromGroupWithRoleNotFound(){
        defaultGroup.setRoles(new HashSet<>());
        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.of(defaultGroup));
        groupService.removeRoleFromGroup(defaultGroup.getId(), 100L);
    }

    @Test(expected = EntityConflictException.class)
    public void removeRoleFromGroupWithMainRole() {
        defaultGroup.setName(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName());
        defaultGroup.setRoles(new HashSet<>(Set.of(adminRole)));

        given(groupRepository.findById(defaultGroup.getId())).willReturn(Optional.of(defaultGroup));
        groupService.removeRoleFromGroup(defaultGroup.getId(), adminRole.getId());
    }

    @Test
    public void removeUserFromGroup() {
        defaultGroup.setUsers(new HashSet<>(Set.of(user1, user2)));
        defaultGroup.setName("externalGroup");

        groupService.removeUserFromGroup(defaultGroup, user1);
        assertEquals(1, defaultGroup.getUsers().size());
        assertTrue(defaultGroup.getUsers().contains(user2));
        assertFalse(defaultGroup.getUsers().contains(user1));
    }

    @Test(expected = EntityConflictException.class)
    public void removeUserFromDefaultGroup() {
        defaultGroup.setUsers(new HashSet<>(Set.of(user1)));
        groupService.removeUserFromGroup(defaultGroup, user1);
    }

    @Test(expected = EntityConflictException.class)
    public void removeUserFromGroupWithAdminRemovingThemselves(){
        adminGroup.setUsers(new HashSet<>(Set.of(user1)));
        given(securityService.hasLoggedInUserSameLogin(user1.getLogin())).willReturn(true);
        groupService.removeUserFromGroup(adminGroup, user1);
    }

    @Test
    public void addUserToGroup() {
        designerGroup.setUsers(new HashSet<>());
        given(groupRepository.save(designerGroup)).willReturn(designerGroup);

        groupService.addUserToGroup(designerGroup, user1);
        assertTrue(designerGroup.getUsers().contains(user1));
        assertEquals(1, designerGroup.getUsers().size());
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
