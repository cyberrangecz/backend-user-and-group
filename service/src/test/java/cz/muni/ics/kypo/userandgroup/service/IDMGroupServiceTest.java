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
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

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
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.service"})
public class IDMGroupServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private IDMGroupService groupService;

    @MockBean
    private IDMGroupRepository groupRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private MicroserviceRepository microserviceRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RestTemplate restTemplate;

    private IDMGroup group1, group2;
    private Role adminRole, userRole, guestRole;
    private User user1, user2, user3;

    private Pageable pageable;
    private Predicate predicate;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        group1 = new IDMGroup("group1", "Great group1");
        group1.setId(1L);

        group2 = new IDMGroup("group2", "Great group2");
        group2.setId(2L);

        adminRole = new Role();
        adminRole.setRoleType(RoleType.ADMINISTRATOR.name());
        adminRole.setId(1L);

        userRole = new Role();
        userRole.setRoleType(RoleType.USER.name());
        userRole.setId(2L);

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST.name());
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

        ResponseEntity<Role[]> responseEntity = new ResponseEntity<>(new Role[0], HttpStatus.OK);
        given(restTemplate.getForEntity(anyString(), eq(Role[].class), anyLong())).willReturn(responseEntity);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getGroup() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));

        IDMGroup g = groupService.get(group1.getId());
        deepEqruals(group1, g);

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
        IDMGroup g = groupService.create(group1);
        deepEqruals(group1, g);

        then(groupRepository).should().save(group1);
    }

    @Test
    public void createGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input group must not be null");
        groupService.create(null);
    }

    @Test
    public void updateGroup() {
        given(groupRepository.save(group1)).willReturn(group1);
        IDMGroup g = groupService.update(group1);
        deepEqruals(group1, g);

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
        assertEquals(GroupDeletionStatus.SUCCESS, groupService.delete(group1));
        then(groupRepository).should().delete(group1);
    }

    @Test
    public void testDeleteGroupExternalAndValid() {
        group1.setStatus(UserAndGroupStatus.VALID);
        group1.setExternalId(123L);
        assertEquals(GroupDeletionStatus.EXTERNAL_VALID, groupService.delete(group1));
        then(groupRepository).should(never()).delete(group1);
    }

    @Test
    public void deleteGroupWithNullGroupShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input group must not be null");
        groupService.delete(null);
    }

    @Test
    public void deleteGroups() {
        group2.setExternalId(1L);

        IDMGroup group3 = new IDMGroup();
        group3.setId(3L);

        List<Long> idsOfGroups = Arrays.asList(group1.getId(), group2.getId(), group3.getId());

        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(groupRepository.findById(group2.getId())).willReturn(Optional.of(group2));
        willThrow(UserAndGroupServiceException.class).given(groupRepository).getOne(group3.getId());

        Map<IDMGroup, GroupDeletionStatus> response = groupService.deleteGroups(idsOfGroups);
        assertEquals(GroupDeletionStatus.SUCCESS, response.get(group1));
        assertEquals(GroupDeletionStatus.EXTERNAL_VALID, response.get(group2));
        assertEquals(GroupDeletionStatus.NOT_FOUND, response.get(group3));

        then(groupRepository).should(times(3)).findById(anyLong());
        then(groupRepository).should().delete(group1);
        then(groupRepository).should(never()).delete(group2);
        then(groupRepository).should(never()).delete(group3);
    }

    @Test
    public void deleteGroupWithNullIdsShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input ids of groups must not be null");
        groupService.deleteGroups(null);
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
        deepEqruals(group1, group);
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
    public void getIDMGroupsByName() {
        group2.setName(group1.getName());
        given(groupRepository.findAllByName(group1.getName(), pageable)).willReturn(new PageImpl<>(Arrays.asList(group1, group2)));

        List<IDMGroup> groups = groupService.getIDMGroupsByName(group1.getName(), pageable).getContent();
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));

        then(groupRepository).should().findAllByName(group1.getName(), pageable);
    }

    @Test
    public void getIDMGroupsByNameNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("IDM Groups with name containing " + group1.getName() + " not found");
        given(groupRepository.findAllByName(group1.getName(), pageable)).willReturn(new PageImpl<>(new ArrayList<>()));
        groupService.getIDMGroupsByName(group1.getName(), pageable);
    }

    @Test
    public void getGroupsByNameWithNullNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input name of group must not be empty");
        groupService.getIDMGroupsByName(null, pageable);
    }

    @Test
    public void getGroupsByNameWithEmptyNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input name of group must not be empty");
        groupService.getIDMGroupsByName("", pageable);
    }

    @Test
    public void getGroupsWithUsers() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        IDMGroup g = groupService.getIDMGroupWithUsers(group1.getId());
        deepEqruals(group1, g);
        then(groupRepository).should().findById(group1.getId());
    }

    @Test
    public void getGroupsByNameWithUsers() {
        given(groupRepository.findByName(group1.getName()))
                .willReturn(Optional.of(group1));
        IDMGroup g = groupService.getIDMGroupWithUsers(group1.getName());
        deepEqruals(group1, g);
        then(groupRepository).should().findByName(group1.getName());
    }

    @Test
    public void getGroupWithUsersWithNullNameShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input name of group must not be empty");
        groupService.getIDMGroupWithUsers("");
    }

    @Test
    public void isGroupInternal() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        assertTrue(groupService.isGroupInternal(group1.getId()));
        then(groupRepository).should().findById(group1.getId());
    }

    @Test
    public void isGroupExternal() {
        group1.setExternalId(1L);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        assertFalse(groupService.isGroupInternal(group1.getId()));
        then(groupRepository).should().findById(group1.getId());
    }

    @Test
    public void isGroupExternalWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        groupService.isGroupInternal(null);
    }

    @Test
    public void getRolesOfGroup() {
        given(groupRepository.existsById(group1.getId())).willReturn(true);
        given(groupRepository.getRolesOfGroup(group1.getId()))
                .willReturn(Stream.of(adminRole, guestRole).collect(Collectors.toSet()));
        Set<Role> roles = groupService.getRolesOfGroup(group1.getId());
        assertEquals(2, roles.size());
        assertTrue(roles.contains(adminRole));
        assertTrue(roles.contains(guestRole));
        then(groupRepository).should().getRolesOfGroup(group1.getId());
    }

    @Test
    public void getRolesOfGroupWithNullIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input id must not be null");
        groupService.getRolesOfGroup(null);
    }

    @Test
    public void getRolesOfGroupWithGroupNotFoundShouldThrowException() {
        given(groupRepository.existsById(group1.getId())).willReturn(false);
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group with id " + group1.getId() + " could not be found.");
        groupService.getRolesOfGroup(group1.getId());
    }

    @Test
    public void assignRole() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.of(adminRole));
        given(roleRepository.findByRoleType(userRole.getRoleType())).willReturn(Optional.of(userRole));
        given(roleRepository.findByRoleType(guestRole.getRoleType())).willReturn(Optional.of(guestRole));
        given(groupRepository.save(group1)).willReturn(group1);
        given(groupRepository.getRolesOfGroup(group1.getId())).willReturn(new HashSet<>(Arrays.asList(adminRole, userRole, guestRole)));

        IDMGroup g = groupService.assignRole(group1.getId(), RoleType.ADMINISTRATOR);

        assertTrue(g.getRoles().contains(adminRole));
        assertTrue(g.getRoles().contains(userRole));
        assertTrue(g.getRoles().contains(guestRole));
        assertEquals(group1, g);

        then(groupRepository).should().findById(group1.getId());
        then(roleRepository).should(times(3)).findByRoleType(anyString());
        then(groupRepository).should().save(group1);
        then(groupRepository).should().getRolesOfGroup(group1.getId());
    }

    @Test
    public void assignRoleWihtInputGroupIdIsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.assignRole(null, RoleType.ADMINISTRATOR);
    }

    @Test
    public void assignRoleWihtInputRoleTypeIsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input roleType must not be null");
        groupService.assignRole(1L, null);
    }

    @Test
    public void assignRoleWihtGroupNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group with " + group1.getId() + " could not be found.");
        given(groupRepository.findById(group1.getId())).willReturn(Optional.empty());
        groupService.assignRole(group1.getId(), RoleType.ADMINISTRATOR);
    }

    @Test
    public void assignRoleWihtAdminRoleNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage(RoleType.ADMINISTRATOR + " role could not be found. Start up of the project probably went wrong, please contact support.");
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.empty());
        groupService.assignRole(group1.getId(), RoleType.ADMINISTRATOR);
    }

    @Test
    public void assignRoleWihtUserRoleNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage(RoleType.USER + " role could not be found. Start up of the project probably went wrong, please contact support.");
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.of(adminRole));
        given(roleRepository.findByRoleType(userRole.getRoleType())).willReturn(Optional.empty());
        groupService.assignRole(group1.getId(), RoleType.ADMINISTRATOR);
    }

    @Test
    public void assignRoleWihtGuestRoleNotFoundShouldThrowException() {
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage(RoleType.GUEST + " role could not be found. Start up of the project probably went wrong, please contact support.");
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(roleRepository.findByRoleType(adminRole.getRoleType())).willReturn(Optional.of(adminRole));
        given(roleRepository.findByRoleType(userRole.getRoleType())).willReturn(Optional.of(userRole));
        given(roleRepository.findByRoleType(guestRole.getRoleType())).willReturn(Optional.empty());
        groupService.assignRole(group1.getId(), RoleType.ADMINISTRATOR);
    }

    @Test
    public void assignRoleInMicroservice() {
        Microservice microservice = new Microservice("traning", "/training");
        microservice.setId(1L);
        given(microserviceRepository.findById(microservice.getId())).willReturn(Optional.of(microservice));
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(groupRepository.getRolesOfGroup(group1.getId())).willReturn(new HashSet<>(Arrays.asList(adminRole, userRole, guestRole)));

        IDMGroup g = groupService.assignRoleInMicroservice(group1.getId(), adminRole.getId(), microservice.getId());

        assertTrue(g.getRoles().contains(adminRole));
        assertTrue(g.getRoles().contains(userRole));
        assertTrue(g.getRoles().contains(guestRole));
        assertEquals(group1, g);

        then(microserviceRepository).should().findById(microservice.getId());
        then(groupRepository).should().findById(group1.getId());
        then(groupRepository).should().getRolesOfGroup(group1.getId());
    }

    @Test
    public void assignRoleInMicroserviceWithNullInputGroupIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.assignRoleInMicroservice(null, 1L, 1L);
    }

    @Test
    public void assignRoleInMicroserviceWithNullInputRoleIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input roleId must not be null");
        groupService.assignRoleInMicroservice(1L, null, 1L);
    }

    @Test
    public void assignRoleInMicroserviceWithNullInputMicroserviceIdShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input microserviceId must not be null");
        groupService.assignRoleInMicroservice(1L, 1L, null);
    }

    @Test
    public void assignRoleInMicroserviceWithMicroserviceNotFoundShouldThrowException() {
        Microservice microservice = new Microservice("traning", "/training");
        microservice.setId(1L);
        given(microserviceRepository.findById(microservice.getId())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Microservice with id " + microservice.getId() + " could not be found.");
        groupService.assignRoleInMicroservice(group1.getId(), adminRole.getId(), microservice.getId());
    }

    @Test
    public void assignRoleInMicroserviceWithGroupNotFoundShouldThrowException() {
        Microservice microservice = new Microservice("traning", "/training");
        microservice.setId(1L);
        given(microserviceRepository.findById(microservice.getId())).willReturn(Optional.of(microservice));
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group with id " + group1.getId() + " could not be found.");
        groupService.assignRoleInMicroservice(group1.getId(), adminRole.getId(), microservice.getId());
    }

    @Test
    public void removeMembers() {
        group1.addUser(user1);
        group1.addUser(user2);
        group1.addUser(user3);

        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(groupRepository.save(group1)).willReturn(group1);

        IDMGroup g = groupService.removeMembers(group1.getId(), Arrays.asList(user1.getId(), group2.getId()));

        assertEquals(group1, g);
        assertFalse(g.getUsers().contains(user1));
        assertFalse(g.getUsers().contains(user2));
        assertTrue(g.getUsers().contains(user3));

        then(groupRepository).should(times(2)).findById(group1.getId());
        then(userRepository).should().findById(user1.getId());
        then(userRepository).should().findById(user2.getId());
        then(groupRepository).should().save(group1);
    }

    @Test
    public void removeMembersWithGroupIdNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.removeMembers(null, Collections.singletonList(1L));
    }

    @Test
    public void removeMembersWithUserIdsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input list of users ids must not be null");
        groupService.removeMembers(1L, null);
    }

    @Test
    public void removeMembersWithGroupNotFoundShouldThrowException() {
        group1.setExternalId(123L);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group is external therefore they could not be updated");
        groupService.removeMembers(group1.getId(), Arrays.asList(user1.getId(), user2.getId()));
    }

    @Test
    public void removeMembersWithUserNotFoundShouldThrowException() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found");
        groupService.removeMembers(group1.getId(), Arrays.asList(user1.getId(), user2.getId()));
    }

    @Test
    public void addMembers() {
        group2.addUser(user2);

        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(groupRepository.findById(group2.getId())).willReturn(Optional.of(group2));
        given(groupRepository.save(group1)).willReturn(group1);

        IDMGroup g = groupService.addMembers(group1.getId(), Collections.singletonList(group2.getId()), Collections.singletonList(user1.getId()));

        assertEquals(group1, g);
        assertTrue(g.getUsers().contains(user1));
        assertTrue(g.getUsers().contains(user2));
        assertFalse(g.getUsers().contains(user3));

        then(groupRepository).should(times(2)).findById(group1.getId());
        then(userRepository).should().findById(user1.getId());
        then(groupRepository).should().findById(group2.getId());
        then(groupRepository).should().save(group1);
    }

    @Test
    public void addMembersWithGroupIdNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input groupId must not be null");
        groupService.addMembers(null, Collections.singletonList(1L), Collections.singletonList(1L));
    }

    @Test
    public void addMembersWithListOfGroupsIdsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input list of groups ids must not be null");
        groupService.addMembers(1L, null, Collections.singletonList(1L));
    }

    @Test
    public void addMembersWithUserIdsNullShouldThrowException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input list of users ids must not be null");
        groupService.addMembers(1L, Collections.singletonList(1L), null);
    }

    @Test
    public void addMembersWithGroupNotFoundShouldThrowException() {
        group1.setExternalId(123L);
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Group is external therefore they could not be updated");
        groupService.addMembers(group1.getId(), Collections.singletonList(group2.getId()), Arrays.asList(user1.getId(), user2.getId()));
    }

    @Test
    public void addMembersWithUserNotFoundShouldThrowException() {
        given(groupRepository.findById(group1.getId())).willReturn(Optional.of(group1));
        given(userRepository.findById(user1.getId())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("User with id " + user1.getId() + " could not be found");
        groupService.addMembers(group1.getId(), Collections.singletonList(group2.getId()), Arrays.asList(user1.getId(), user2.getId()));
    }

    private void deepEqruals(IDMGroup expectedGroup, IDMGroup actualGroup) {
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
