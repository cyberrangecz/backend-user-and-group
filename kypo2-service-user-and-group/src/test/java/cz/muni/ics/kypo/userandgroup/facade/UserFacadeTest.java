package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.UserMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.service.impl.IdenticonService;
import cz.muni.ics.kypo.userandgroup.service.impl.SecurityService;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class, UserMapperImpl.class})
public class UserFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private UserFacade userFacade;
    @Mock
    private UserService userService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private SecurityService securityService;
    @Mock
    private IdenticonService identiconService;
    @Mock
    private IDMGroupService idmGroupService;
    @Autowired
    private UserMapperImpl userMapper;
    @Autowired
    private RoleMapperImpl roleMapper;

    private User user1, user2;
    private UserDTO userDTO1, userDTO2;
    private Role adminRole, userRole;
    private Predicate predicate;
    private Pageable pageable;
    private IDMGroup adminGroup, userGroup;
    private static final String NAME_OF_USER_AND_GROUP_SERVICE = "kypo2-user-and-group";

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);
        userFacade = new UserFacadeImpl(userService, idmGroupService, identiconService, securityService, userMapper, roleMapper);

        user1 = new User("user1", "https://oidc.muni.cz/oidc/");
        user1.setId(1L);
        user1.setFullName("User One");
        user1.setMail("user.one@mail.com");
        user1.setStatus(UserAndGroupStatus.VALID);
        user1.setFamilyName("One");
        user1.setGivenName("User");
        user1.setPicture(new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, (byte)0xea});

        user2 = new User("user2", "https://oidc.muni.cz/oidc/");
        user2.setId(2L);
        user2.setFullName("User Two");
        user2.setMail("user.two@mail.com");
        user2.setStatus(UserAndGroupStatus.VALID);

        adminGroup = new IDMGroup("adminGroup", "Administrator group");
        adminGroup.setId(1L);

        userGroup = new IDMGroup("userGroup", "User group");
        userGroup.setId(10L);

        Microservice microservice = new Microservice();
        microservice.setId(1L);
        microservice.setName("TestMicroservice");

        adminRole = new Role();
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.toString());
        adminRole.setId(1L);
        adminRole.setMicroservice(microservice);
        adminGroup.setRoles(Set.of(adminRole));

        userRole = new Role();
        userRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_USER.toString());
        userRole.setId(2L);
        userRole.setMicroservice(microservice);
        userGroup.setRoles(Set.of(userRole));

        adminGroup.addUser(user1);
        userGroup.addUser(user1);

        userDTO1 = new UserDTO();
        userDTO1.setLogin("user1");
        userDTO1.setId(1L);
        userDTO1.setFullName("User One");
        userDTO1.setMail("user.one@mail.com");
        userDTO1.setIss("https://oidc.muni.cz/oidc/");
        userDTO1.setGivenName("User");
        userDTO1.setFamilyName("One");

        userDTO2 = new UserDTO();
        userDTO2.setLogin("user2");
        userDTO2.setId(2L);
        userDTO2.setFullName("User Two");
        userDTO2.setMail("user.two@mail.com");
        userDTO2.setIss("https://oidc.muni.cz/oidc/");
    }

    @Test
    public void testGetUsers() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.toString());
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType(RoleType.ROLE_USER_AND_GROUP_GUEST.name());
        RoleDTO[] rolesArray = new RoleDTO[1];
        rolesArray[0] = roleDTO;
        mockSpringSecurityContextForGet(rolesArray);
        Page<User> rolePage = new PageImpl<>(Arrays.asList(user1, user2));
        PageResultResource<UserDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(userDTO1, userDTO2));

        given(userService.getAllUsers(predicate, pageable)).willReturn(rolePage);
        PageResultResource<UserDTO> pageResultResource = userFacade.getUsers(predicate, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userDTO1, pageResultResource.getContent().get(0));
        assertEquals(userDTO2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testGetUserInfo(){
        Set<Role> expectedRoles = new HashSet<>();
        for (IDMGroup groupOfUser: user1.getGroups()) {
            expectedRoles.addAll(groupOfUser.getRoles());
        }
        given(userService.getUserByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.of(user1));
        UserDTO userDTO = userFacade.getUserInfo(user1.getLogin(), user1.getIss());

        assertEquals(userDTO.getId(), user1.getId());
        assertEquals(userDTO.getIss(), user1.getIss());
        assertEquals(userDTO.getLogin(), user1.getLogin());
        assertEquals(userDTO.getFullName(), user1.getFullName());
        assertEquals(userDTO.getMail(), user1.getMail());
        assertEquals(userDTO.getGivenName(), user1.getGivenName());
        assertEquals(userDTO.getFamilyName(), user1.getFamilyName());
        assertEquals(userDTO.getPicture(), user1.getPicture());

        for (Role role : expectedRoles){
            RoleDTO expectedRole = new RoleDTO();
            expectedRole.setId(role.getId());
            expectedRole.setIdOfMicroservice(role.getMicroservice().getId());
            expectedRole.setNameOfMicroservice(role.getMicroservice().getName());
            expectedRole.setRoleType(role.getRoleType());
            assertTrue(userDTO.getRoles().contains(expectedRole));
        }
    }

    @Test
    public void testGetUserInfoWithNullIss(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getUserInfo(sub, iss) the input iss must not be empty.");
        userFacade.getUserInfo(user1.getLogin(), null);
    }

    @Test
    public void testGetUserInfoWithNullSub(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("In method getUserInfo(sub, iss) the input sub must not be empty.");
        userFacade.getUserInfo(null, user1.getIss());
    }

    @Test
    public void testGetUserInfoWithEmptyUserOptional(){
        given(userService.getUserByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupFacadeException.class);
        thrown.expectMessage("User with sub: " + user1.getLogin() + " and iss: + " + user1.getIss() +
                " could not be found.");
        userFacade.getUserInfo(user1.getLogin(), user1.getIss());
    }

    @Test
    public void testGetUser() {
        given(userService.getUserById(anyLong())).willReturn(user1);
        UserDTO userDTO = userFacade.getUserById(1L);

        assertEquals(userDTO1, userDTO);
    }

    @Test
    public void testGetUserWithServiceException() {
        given(userService.getUserById(anyLong())).willThrow(new UserAndGroupServiceException(ErrorCode.RESOURCE_NOT_FOUND));
        thrown.expect(UserAndGroupFacadeException.class);
        userFacade.getUserById(1L);
    }

    @Test
    public void testGetAllUsersNotInGivenGroup() {
        Page<User> rolePage = new PageImpl<>(Arrays.asList(user1, user2));
        PageResultResource<UserDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(userDTO1, userDTO2));

        given(userService.getAllUsersNotInGivenGroup(1L, null, pageable)).willReturn(rolePage);
        PageResultResource<UserDTO> pageResultResource = userFacade.getAllUsersNotInGivenGroup(1L, null, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userDTO1, pageResultResource.getContent().get(0));
        assertEquals(userDTO2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testDeleteUser() {
        given(userService.getUserById(anyLong())).willReturn(user1);
        userFacade.deleteUser(user1.getId());
        then(userService).should().deleteUser(user1);

    }

    @Test
    public void testDeleteUserNotFound() {
        given(userService.getUserById(anyLong())).willThrow(new UserAndGroupServiceException(ErrorCode.RESOURCE_NOT_FOUND));
        thrown.expect(UserAndGroupFacadeException.class);
        userFacade.deleteUser(1L);
    }

    @Test
    public void testGetRolesOfUser() {
        Microservice microservice = new Microservice();
        microservice.setName(NAME_OF_USER_AND_GROUP_SERVICE);

        Role role1 = Mockito.mock(Role.class);
        given(role1.getMicroservice()).willReturn(microservice);
        given(role1.getId()).willReturn(1L);

        Role role2 = Mockito.mock(Role.class);
        given(role2.getMicroservice()).willReturn(microservice);
        given(role2.getId()).willReturn(2L);

        Set<Role> roles = Set.of(role1, role2);

        RoleDTO roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setNameOfMicroservice(NAME_OF_USER_AND_GROUP_SERVICE);

        RoleDTO roleDTO2 = new RoleDTO();
        roleDTO2.setId(2L);
        roleDTO2.setNameOfMicroservice(NAME_OF_USER_AND_GROUP_SERVICE);

        given(userService.getRolesOfUser(anyLong())).willReturn(roles);
        Set<RoleDTO> responseRolesDTO = userFacade.getRolesOfUser(1L);

        assertEquals(2, responseRolesDTO.size());
        assertTrue(responseRolesDTO.contains(roleDTO1));
        assertTrue(responseRolesDTO.contains(roleDTO2));
    }

    @Test
    public void getUsersWithGivenRole() {
        given(userService.getUsersWithGivenRole(1L, null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        given(userService.getRolesOfUser(user1.getId())).willReturn(Collections.emptySet());
        given(userService.getRolesOfUser(user2.getId())).willReturn(Collections.emptySet());

        PageResultResource<UserDTO> usersDTO = userFacade.getUsersWithGivenRole(1L, null, pageable);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().contains(userDTO1));
        assertTrue(usersDTO.getContent().contains(userDTO2));
    }

    private void mockSpringSecurityContextForGet(RoleDTO[] rolesArray) {
        ResponseEntity<RoleDTO[]> responseEntity = new ResponseEntity<>(rolesArray, HttpStatus.NO_CONTENT);
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails auth = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getDetails()).willReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        given(auth.getTokenType()).willReturn("");
        given(auth.getTokenValue()).willReturn("");
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(RoleDTO[].class), anyLong())).willReturn(responseEntity);
    }
}
