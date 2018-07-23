package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;


import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.facade",  "cz.muni.ics.kypo.userandgroup.service"})
public class IDMGroupFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private IDMGroupFacade idmGroupFacade;

    @MockBean
    private IDMGroupService idmGroupService;

    @MockBean
    private BeanMapping beanMapping;

    private IDMGroup g1, g2;
    private User user1;
    private Predicate predicate;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        user1 = new User();
        user1.setId(1L);
        user1.setFullName("User 1");

        g1 = new IDMGroup();
        g1.setId(1L);
        g1.setName("Group 1");
        g1.setStatus(UserAndGroupStatus.VALID);
        g1.setExternalId(10L);
        g1.addUser(user1);


        g2 = new IDMGroup();
        g2.setId(2L);
        g2.setName("Group 2");
        g2.setStatus(UserAndGroupStatus.VALID);


    }

    @Test
    public void testCreateGroup() {
        given(idmGroupService.create(any(IDMGroup.class))).willReturn(g1);
        given(beanMapping.mapTo(any(NewGroupDTO.class), eq(IDMGroup.class))).willReturn(g1);
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDTO.class))).willReturn(getGroupDTO());
        GroupDTO groupDTO = idmGroupFacade.createGroup(getNewGroupDTO());

        assertEquals(g1.getName(), groupDTO.getName());
        then(idmGroupService).should().create(g1);

    }

    @Test
    public void testUpdateGroup() {
        GroupDTO updatedGroupDTO = getGroupDTO();
        updatedGroupDTO.setName("Group 2");
        given(beanMapping.mapTo(any(UpdateGroupDTO.class), eq(IDMGroup.class))).willReturn(g2);
        given(idmGroupService.update(g2)).willReturn(g2);
        given(beanMapping.mapTo(g2, GroupDTO.class)).willReturn(updatedGroupDTO);
        GroupDTO groupDTO = idmGroupFacade.updateGroup(getUpdateGroupDTO());

        assertEquals("Group 2", groupDTO.getName());
        then(idmGroupService).should().update(g2);

    }

    @Test
    public void testRemoveMembers() {
        given(idmGroupService.removeMembers(1L, Arrays.asList(1L))).willReturn(g2);
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDTO.class))).willReturn(getGroupDTO());
        GroupDTO groupDTO = idmGroupFacade.removeMembers(1L, Arrays.asList(1L));

        assertEquals(0, groupDTO.getMembers().size());
        then(idmGroupService).should().removeMembers(1L, Arrays.asList(1L));

    }

    @Test
    public void testRemoveMembersWithServiceExcpetion() {
        given(idmGroupService.removeMembers(1L, Arrays.asList(1L))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        idmGroupFacade.removeMembers(1L, Arrays.asList(1L));
    }

    @Test
    public void testAddMembers() {
        GroupDTO g = getGroupDTO();
        g.setMembers(Arrays.asList(new UserForGroupsDTO()));
        given(idmGroupService.addMembers(1L, Arrays.asList(1L), Arrays.asList(1L))).willReturn(g1);
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDTO.class))).willReturn(g);
        GroupDTO groupDTO = idmGroupFacade.addMembers(getAddMembersToGroupDTO());

        assertEquals(1, groupDTO.getMembers().size());
        then(idmGroupService).should().addMembers(1L, Arrays.asList(1L), Arrays.asList(1L));

    }

    @Test
    public void testAddMembersWithServiceException() {
        given(idmGroupService.addMembers(1L, Arrays.asList(1L), Arrays.asList(1L))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        idmGroupFacade.addMembers(getAddMembersToGroupDTO());
    }

    @Test
    public void testDeleteGroup() {
        given(idmGroupService.get(anyLong())).willReturn(g1);
        given(idmGroupService.delete(any(IDMGroup.class))).willReturn(GroupDeletionStatus.SUCCESS);
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDeletionResponseDTO.class))).willReturn(getGroupDeletionResponseDTO());
        GroupDeletionResponseDTO groupDeletionResponseDTO = idmGroupFacade.deleteGroup(1L);

        assertEquals(GroupDeletionStatus.SUCCESS, groupDeletionResponseDTO.getStatus());
        then(idmGroupService).should().delete(g1);
    }

    @Test
    public void testDeleteGroupWithServiceException() {
        given(idmGroupService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        idmGroupFacade.deleteGroup(1L);
    }

    @Test
    public void testDeleteGroups() {
        Map<IDMGroup, GroupDeletionStatus> groupGroupDeletionStatusMap = new HashMap<>();
        groupGroupDeletionStatusMap.put(g1, GroupDeletionStatus.SUCCESS);

        given(idmGroupService.deleteGroups(anyList())).willReturn(groupGroupDeletionStatusMap);
        List<GroupDeletionResponseDTO> responseDTOS = idmGroupFacade.deleteGroups(Arrays.asList(1L));

        assertEquals(GroupDeletionStatus.SUCCESS, responseDTOS.get(0).getStatus());
        then(idmGroupService).should().deleteGroups(Arrays.asList(1L));

    }

    @Test
    public void testGetAllGroups() {
        Page<IDMGroup> idmGroupPage = new PageImpl<IDMGroup>(Arrays.asList(g1));
        PageResultResource<GroupDTO> pages = new PageResultResource<>();
        pages.setContent(Arrays.asList(getGroupDTO()));

        given(idmGroupService.getAllIDMGroups(predicate, pageable)).willReturn(idmGroupPage);
        given(beanMapping.mapToPageResultDTO(any(Page.class), eq(GroupDTO.class))).willReturn(pages);
        PageResultResource<GroupDTO> responseDTOPageResultResource = idmGroupFacade.getAllGroups(predicate, pageable);

        assertEquals(1, responseDTOPageResultResource.getContent().size());
        then(idmGroupService).should().getAllIDMGroups(predicate, pageable);
    }

    @Test
    public void testGetGroup() {
        given(idmGroupService.get(anyLong())).willReturn(g1);
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDTO.class))).willReturn(getGroupDTO());
        GroupDTO groupDTO = idmGroupFacade.getGroup(1L);

        assertEquals(g1.getName(), groupDTO.getName());
        then(idmGroupService).should().get(1L);
    }

    @Test
    public void testGetGroupWithServiceException() {
        given(idmGroupService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        idmGroupFacade.getGroup(1L);
    }

    @Test
    public void testGetRolesOfGroup() {

        Set<Role> roles = new HashSet<>();
        roles.add(getRole());

        Set<RoleDTO> roleDTOS = new HashSet<>();
        roleDTOS.add(getRoleDTO());

        given(idmGroupService.getRolesOfGroup(anyLong())).willReturn(roles);
        given(beanMapping.mapToSet(anySet(), eq(RoleDTO.class))).willReturn(roleDTOS);
        Set<RoleDTO> rolesDTO = idmGroupFacade.getRolesOfGroup(1L);
        assertEquals(1L, roleDTOS.size());
        assertEquals(RoleType.USER.toString(), new ArrayList<RoleDTO>(roleDTOS).get(0).getRoleType().toString());
    }

    @Test
    public void testAssignRole() {
        given(idmGroupService.assignRole(anyLong(), any(RoleType.class))).willReturn(g1);
        given(beanMapping.mapTo(any(IDMGroup.class), eq(GroupDTO.class))).willReturn(getGroupDTO());
        GroupDTO groupDTO = idmGroupFacade.assignRole(1L, RoleType.USER);

        assertEquals(RoleType.USER.toString(), new ArrayList<RoleDTO>(groupDTO.getRoles()).get(0).getRoleType().toString());
    }

    @Test
    public void testAssignRoleWithServiceException() {
        given(idmGroupService.assignRole(anyLong(), any(RoleType.class))).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        idmGroupFacade.assignRole(1L, RoleType.USER);
    }


    private NewGroupDTO getNewGroupDTO() {
        NewGroupDTO newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("Group1");

        return newGroupDTO;
    }

    private GroupDTO getGroupDTO() {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(1l);
        groupDTO.setName("Group 1");
        groupDTO.setMembers(Arrays.asList());

        Set<RoleDTO> roleDTOS = new HashSet<>();
        roleDTOS.add(getRoleDTO());
        groupDTO.setRoles(roleDTOS);
        return groupDTO;
    }

    private UpdateGroupDTO getUpdateGroupDTO() {
        UpdateGroupDTO updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setId(2L);
        updateGroupDTO.setName("Group 2");
        return updateGroupDTO;
    }



    private AddMembersToGroupDTO getAddMembersToGroupDTO() {
        AddMembersToGroupDTO addMembers = new AddMembersToGroupDTO();
        addMembers.setGroupId(1L);
        addMembers.setIdsOfUsersToBeAdd(Arrays.asList(1L));
        addMembers.setIdsOfGroupsOfImportedUsers(Arrays.asList(1L));
        return addMembers;
    }

    private GroupDeletionResponseDTO getGroupDeletionResponseDTO() {
        GroupDeletionResponseDTO gdrDTO = new GroupDeletionResponseDTO();
        gdrDTO.setId(1L);
        gdrDTO.setName("Group deletion response");
        return gdrDTO;
    }

    private Role getRole() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.USER.toString());
        return role;
    }

    private RoleDTO getRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType(RoleType.USER.toString());
        return roleDTO;
    }






}
