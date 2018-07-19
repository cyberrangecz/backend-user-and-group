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

import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
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
    private RestTemplate restTemplate;

    private IDMGroup group1, group2;

    private Role adminRole, guestRole;

    private Pageable pageable;

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

        guestRole = new Role();
        guestRole.setRoleType(RoleType.GUEST.name());
        guestRole.setId(2L);

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
        thrown.expect(IdentityManagementException.class);
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
        willThrow(IdentityManagementException.class).given(groupRepository).getOne(group3.getId());

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
        given(groupRepository.findAll(pageable))
                .willReturn(new PageImpl<>(Arrays.asList(group1, group2)));

        // do not create user3
        IDMGroup group3 = new IDMGroup("Participants", "thrird group");

        List<IDMGroup> groups = groupService.getAllIDMGroups(pageable).getContent();
        assertEquals(2, groups.size());
        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));
        assertFalse(groups.contains(group3));

        then(groupRepository).should().findAll(pageable);
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
        thrown.expect(IdentityManagementException.class);
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
        thrown.expect(IdentityManagementException.class);
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

    private void deepEqruals(IDMGroup expectedGroup, IDMGroup actualGroup) {
        assertEquals(expectedGroup.getId(), actualGroup.getId());
        assertEquals(expectedGroup.getName(), actualGroup.getName());
        assertEquals(expectedGroup.getDescription(), actualGroup.getDescription());
        assertEquals(expectedGroup.getStatus(), actualGroup.getStatus());
    }

    @After
    public void afterMethod() {
        reset(groupRepository);
    }
}
