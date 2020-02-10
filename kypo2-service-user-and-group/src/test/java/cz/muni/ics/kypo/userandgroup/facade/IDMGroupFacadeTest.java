package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.IDMGroupMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class, IDMGroupMapperImpl.class})
@ContextConfiguration(classes = {TestDataFactory.class})
public class IDMGroupFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private IDMGroupFacade groupFacade;
    @Mock
    private IDMGroupService groupService;
    @Mock
    private UserService userService;
    @Mock
    private RestTemplate restTemplate;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private RoleMapperImpl roleMapper;
    @Autowired
    private IDMGroupMapperImpl groupMapper;

    private IDMGroup g1, g2;
    private Role adminRole, userRole;
    private GroupDTO groupDTO;
    private Predicate predicate;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        groupFacade = new IDMGroupFacadeImpl(groupService, userService, roleMapper, groupMapper);

        g1 = testDataFactory.getUAGAdminGroup();
        g2 = testDataFactory.getTrainingDesignerGroup();

        groupDTO = new GroupDTO();
        groupDTO.setId(1L);
        groupDTO.setName("Group 1");

        adminRole = testDataFactory.getUAGAdminRole();
        userRole = testDataFactory.getUAGUserRole();

        mockAuthentication();
    }

    @Test
    public void testCreateGroup() {
        g1.addUser(testDataFactory.getUser1());
        UserForGroupsDTO userForGroupsDTO = testDataFactory.getUserForGroupsDTO1();

        NewGroupDTO newGroupDTO = new NewGroupDTO();
        newGroupDTO.setName("Group 1");
        newGroupDTO.setUsers(Set.of(userForGroupsDTO));

        given(groupService.createIDMGroup(any(IDMGroup.class), anyList())).willReturn(g1);
        GroupDTO groupDTO = groupFacade.createGroup(new NewGroupDTO());

        assertEquals(g1.getName(), groupDTO.getName());
        assertEquals(1, groupDTO.getUsers().size());
        assertTrue(groupDTO.getUsers().contains(userForGroupsDTO));
        then(groupService).should().createIDMGroup(any(IDMGroup.class), anyList());
    }

    @Test
    public void testUpdateGroup() {
        UpdateGroupDTO updatedGroupDTO = new UpdateGroupDTO();
        updatedGroupDTO.setId(g2.getId());
        updatedGroupDTO.setName(g2.getName());
        updatedGroupDTO.setDescription(g2.getDescription());
        groupFacade.updateGroup(updatedGroupDTO);
        then(groupService).should().updateIDMGroup(g2);
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testDeleteGroupWithUsersThrowsException() {
        g1.setId(1L);
        given(groupService.getGroupById(g1.getId())).willReturn(g1);
        willThrow(new UserAndGroupServiceException(ErrorCode.RESOURCE_CONFLICT)).given(groupService).deleteIDMGroup(g1);
        groupFacade.deleteGroup(g1.getId());
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testDeleteGroupsWithPersonsThrows() {
        given(groupService.getGroupsByIds(anyList())).willReturn(List.of(g1));
        willThrow(new UserAndGroupServiceException(ErrorCode.RESOURCE_CONFLICT)).given(groupService).deleteIDMGroup(g1);
        groupFacade.deleteGroups(Collections.singletonList(1L));
    }

    @Test
    public void testGetAllGroups() {
        g1.setId(1L);
        RoleDTO[] rolesArray = new RoleDTO[1];
        rolesArray[0] = getRoleDTO();
        mockSpringSecurityContextForGet(rolesArray);
        Page<IDMGroup> idmGroupPage = new PageImpl<>(Collections.singletonList(g1));
        PageResultResource<GroupDTO> pages = new PageResultResource<>();
        pages.setContent(Collections.singletonList(groupDTO));

        given(groupService.getAllIDMGroups(predicate, pageable)).willReturn(idmGroupPage);
        given(groupService.getGroupById(anyLong())).willReturn(g1);
        PageResultResource<GroupDTO> responseDTOPageResultResource = groupFacade.getAllGroups(predicate, pageable);

        assertEquals(1, responseDTOPageResultResource.getContent().size());
        then(groupService).should().getAllIDMGroups(predicate, pageable);
    }

    @Test
    public void testGetGroup() {
        given(groupService.getGroupById(anyLong())).willReturn(g1);
        GroupDTO groupDTO = groupFacade.getGroupById(1L);

        assertEquals(g1.getName(), groupDTO.getName());
        then(groupService).should(times(1)).getGroupById(1L);
    }

    @Test
    public void testGetGroupWithServiceException() {
        given(groupService.getGroupById(anyLong())).willThrow(new UserAndGroupServiceException(ErrorCode.RESOURCE_NOT_FOUND));
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.getGroupById(1L);
    }

    @Test
    public void testGetRolesOfGroup() {

        given(groupService.getRolesOfGroup(anyLong())).willReturn(Set.of(adminRole, userRole));
        Set<RoleDTO> rolesDTO = groupFacade.getRolesOfGroup(1L);
        assertEquals(2, rolesDTO.size());
        assertTrue(rolesDTO.stream().anyMatch(r -> r.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name())));
        assertTrue(rolesDTO.stream().anyMatch(r -> r.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_USER.name())));
    }

    @Test
    public void testGetRolesOfGroupWithServiceThrowsException() {
        given(groupService.getRolesOfGroup(anyLong())).willThrow(UserAndGroupServiceException.class);
        thrown.expect(UserAndGroupFacadeException.class);
        groupFacade.getRolesOfGroup(1L);
    }

    private RoleDTO getRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
        return roleDTO;
    }

    private void mockAuthentication() {
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails auth = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getDetails()).willReturn(auth);
        given(auth.getTokenType()).willReturn("");
        given(auth.getTokenValue()).willReturn("");
    }

    private void mockSpringSecurityContextForGet(RoleDTO[] rolesArray) {
        ResponseEntity<RoleDTO[]> responseEntity = new ResponseEntity<>(rolesArray, HttpStatus.NO_CONTENT);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleDTO[].class), anyLong())).willReturn(responseEntity);
    }
}
