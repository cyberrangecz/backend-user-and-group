package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.impl.IDMGroupServiceImpl;
import cz.muni.ics.kypo.userandgroup.service.impl.SecurityService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
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

    private IDMGroup group1, group2, mainGroup;
    private Role adminRole, userRole;

    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        groupService = new IDMGroupServiceImpl(groupRepository, roleRepository, securityService);

        group1 = testDataFactory.getUAGDefaultGroup();
        group2 = testDataFactory.getTrainingDesignerGroup();
        mainGroup = testDataFactory.getUAGAdminGroup();

        adminRole = testDataFactory.getUAGAdminRole();
        userRole = testDataFactory.getUAGUserRole();

        group1.setRoles(new HashSet<>(Arrays.asList(adminRole, userRole)));
        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getGroup() {
        group1.setId(1L);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        IDMGroup g = groupService.getGroupById(group1.getId());
        deepEquals(group1, g);

        then(groupRepository).should().findById(group1.getId());
    }

    @Test
    public void getGroupNotFoundShouldThrowException() {
        Long id = 3L;
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("IDMGroup with id " + id + " not found");
        given(groupRepository.findById(id)).willReturn(Optional.empty());
        groupService.getGroupById(id);
    }

    @Test
    public void getGroupWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getGroupById(id) the input must not be null.");
        groupService.getGroupById(null);
    }

    @Test
    public void createGroup() {
        Role guestRole = testDataFactory.getUAGGuestRole();
        given(groupRepository.save(group1)).willReturn(group1);
        given(roleRepository.findByRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString())).willReturn(Optional.ofNullable(guestRole));
        IDMGroup g = groupService.createIDMGroup(group1, new ArrayList<>());
        deepEquals(group1, g);

        then(groupRepository).should().save(group1);
    }

    @Test
    public void createGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method createIDMGroup(id) the input must not be null.");
        groupService.createIDMGroup(null, new ArrayList<>());
    }

    @Test
    public void updateGroup() {
        group1.setId(1L);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(groupRepository.save(group1)).willReturn(group1);
        IDMGroup g = groupService.updateIDMGroup(group1);
        deepEquals(group1, g);

        then(groupRepository).should().save(group1);
    }

    @Test
    public void updateGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method updateIDMGroup(id) the input must not be null.");
        groupService.updateIDMGroup(null);
    }

    @Test
    public void testDeleteGroupSuccess() {
        group2.setId(1L);
        groupService.deleteIDMGroup(group2);
        then(groupRepository).should().delete(group2);

    }

    @Test
    public void testDeleteGroupErrorMainGroup() {
        mainGroup.setId(1L);
        given(roleRepository.findAll()).willReturn(Collections.singletonList(adminRole));
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("It is not possible to delete group with id: " + mainGroup.getId() + ". " +
        "This group is User and Group default group that could not be deleted.");
        groupService.deleteIDMGroup(mainGroup);
        then(groupRepository).should(never()).delete(mainGroup);
    }

    @Test
    public void deleteGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method updateIDMGroup(id) the input must not be null.");
        groupService.deleteIDMGroup(null);
    }

    @Test
    public void getAllGroups() {
        given(groupRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(group1, group2)));

        // do not createMicroservice user3
        IDMGroup group3 = new IDMGroup("Participants", "third group");

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
    public void getRolesOfGroup() {
        group1.setId(1L);
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
        thrown.expectMessage("In method getRolesOfGroup(id) the input must not be null.");
        groupService.getRolesOfGroup(null);
    }

    @Test
    public void getRolesOfGroupWithGroupNotFoundShouldThrowException() {
        group1.setId(1L);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("IDMGroup with id " + group1.getId() + " not found.");
        groupService.getRolesOfGroup(group1.getId());
    }

    @Test
    public void assignRole() {
        group1.setId(1L);
        adminRole.setId(1L);
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
    public void assignRoleWithInputGroupIdIsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method assignRole(groupId, roleId) the input groupId must not be null.");
        groupService.assignRole(null, 1L);
    }

    @Test
    public void assignRoleWihtInputRoleIdIsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method assignRole(groupId, roleId) the input roleId must not be null.");
        groupService.assignRole(1L, null);
    }

    @Test
    public void assignRoleWithGroupNotFoundShouldThrowException() {
        group1.setId(1L);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("IDMGroup with id "+ group1.getId() + " not found.");
        given(groupRepository.findById(group1.getId())).willReturn(Optional.empty());
        groupService.assignRole(group1.getId(), 1L);
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
