package cz.muni.ics.kypo.userandgroup.facade;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserUpdateDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.UserMapperImpl;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.service.impl.IdenticonService;
import cz.muni.ics.kypo.userandgroup.service.impl.SecurityService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RoleMapperImpl.class, UserMapperImpl.class})
@ContextConfiguration(classes = {TestDataFactory.class})
public class UserFacadeTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private UserFacade userFacade;
    @Mock
    private UserService userService;
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
    private UserForGroupsDTO userForGroupsDTO1, userForGroupsDTO2;
    private UserUpdateDTO userUpdateDTO;
    private Role guestRole, userRole;
    private RoleDTO guestRoleDTO, userRoleDTO;
    private Microservice microservice;
    private Predicate predicate;
    private Pageable pageable;
    private IDMGroup adminGroup, defaultGroup;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);
        userFacade = new UserFacadeImpl(userService, idmGroupService, identiconService, securityService, userMapper, roleMapper);

        microservice = testDataFactory.getKypoUaGMicroservice();

        user1 = testDataFactory.getUser1();
        user1.setId(1L);
        user2 = testDataFactory.getUser2();
        user2.setId(2L);

        adminGroup = testDataFactory.getUAGAdminGroup();
        adminGroup.setId(1L);
        adminGroup.addUser(user1);
        defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setId(2L);
        defaultGroup.addUser(user1);

        guestRole = testDataFactory.getUAGGuestRole();
        guestRole.setId(1L);
        guestRoleDTO = testDataFactory.getuAGGuestRoleDTO();
        guestRoleDTO.setId(guestRole.getId());

        userRole = testDataFactory.getUAGUserRole();
        userRole.setId(1L);
        userRoleDTO = testDataFactory.getUAGUserRoleDTO();
        userRoleDTO.setId(userRole.getId());

        userDTO1 = testDataFactory.getUser1DTO();
        userDTO1.setId(user1.getId());
        userDTO2 = testDataFactory.getUser2DTO();
        userDTO2.setId(user2.getId());

        userForGroupsDTO1 = testDataFactory.getUserForGroupsDTO1();
        userForGroupsDTO1.setId(user1.getId());
        userForGroupsDTO2 = testDataFactory.getUserForGroupsDTO2();
        userForGroupsDTO2.setId(user2.getId());

        userUpdateDTO = testDataFactory.getUserUpdateDTO();
    }

    @Test
    public void testGetUsers() {
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

        userDTO1.setId(user1.getId());
        assertEquals(userDTO1, userDTO);
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
    public void testGetUserInfoWithEmptyUserOptional(){
        given(userService.getUserByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.empty());
        thrown.expect(UserAndGroupFacadeException.class);
        thrown.expectMessage("User with sub: " + user1.getLogin() + " and iss: + " + user1.getIss() +
                " could not be found.");
        userFacade.getUserInfo(user1.getLogin(), user1.getIss());
    }

    @Test
    public void testGetUserById() {
        given(userService.getUserById(anyLong())).willReturn(user1);
        UserDTO userDTO = userFacade.getUserById(user1.getId());

        assertEquals(userDTO1, userDTO);
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testGetUserByIdWithServiceException() {
        given(userService.getUserById(anyLong())).willThrow(UserAndGroupServiceException.class);
        userFacade.getUserById(1L);
    }

    @Test
    public void testCreateOrUpdateOrGetOIDCUserCreate() {
        given(userService.getUserByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.empty());
        given(userService.createUser(user1)).willReturn(user1);
        given(idmGroupService.getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName())).willReturn(defaultGroup);
        userFacade.createOrUpdateOrGetOIDCUser(user1.getLogin(), user1.getIss(), createSub(user1));
    }

    @Test
    public void testCreateOrUpdateOrGetOIDCUserUpdate() {
        given(userService.getUserByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.ofNullable(user1));
        userFacade.createOrUpdateOrGetOIDCUser(user1.getLogin(), user1.getIss(), createSub(user1));

        then(userService).should().updateUser(user1);
    }

    @Test
    public void testCreateOrUpdateOrGetOIDCUserUpdateUserWithNullAttributes() {
        User userToUpdate = new User();
        userToUpdate.setId(user1.getId());
        userToUpdate.setLogin(user1.getLogin());
        userToUpdate.setIss(user1.getIss());
        given(userService.getUserByLoginAndIss(user1.getLogin(), user1.getIss())).willReturn(Optional.ofNullable(userToUpdate));
        userFacade.createOrUpdateOrGetOIDCUser(user1.getLogin(), user1.getIss(), createSub(user1));

        then(userService).should().updateUser(user1);
    }

    @Test
    public void testDeleteUser() {
        user1.setId(1L);
        given(userService.getUserById(anyLong())).willReturn(user1);
        userFacade.deleteUser(user1.getId());
        then(userService).should().deleteUser(user1);
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testDeleteUserNotFound() {
        given(userService.getUserById(anyLong())).willThrow(UserAndGroupServiceException.class);
        userFacade.deleteUser(1L);
    }

    @Test
    public void testGetAllUsersNotInGivenGroup() {
        Page<User> userPage = new PageImpl<>(Arrays.asList(user1, user2));
        given(userService.getAllUsersNotInGivenGroup(1L, null, pageable)).willReturn(userPage);
        PageResultResource<UserDTO> pageResultResource = userFacade.getAllUsersNotInGivenGroup(1L, null, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userDTO1, pageResultResource.getContent().get(0));
        assertEquals(userDTO2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testGetUsersInGroups() {
        Page<User> userPage = new PageImpl<>(Arrays.asList(user1, user2));
        given(userService.getUsersInGroups(Set.of(adminGroup.getId(), defaultGroup.getId()), null, pageable)).willReturn(userPage);
        PageResultResource<UserForGroupsDTO> pageResultResource = userFacade.getUsersInGroups(Set.of(adminGroup.getId(), defaultGroup.getId()), null, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userForGroupsDTO1, pageResultResource.getContent().get(0));
        assertEquals(userForGroupsDTO2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testDeleteUsers() {
        given(userService.getUsersByIds(List.of(user1.getId(), user2.getId()))).willReturn(List.of(user1,user2));
        userFacade.deleteUsers(List.of(user1.getId(), user2.getId()));
        then(userService).should().deleteUser(user1);
        then(userService).should().deleteUser(user2);
    }

    @Test
    public void testUpdateUser() {
        given(userService.updateUser(user1)).willReturn(user1);
        userFacade.updateUser(userUpdateDTO);
        then(userService).should().updateUser(user1);
    }

    @Test
    public void testGetRolesOfUser() {
        userRoleDTO.setNameOfMicroservice(microservice.getName());
        guestRoleDTO.setNameOfMicroservice(microservice.getName());
        given(userService.getRolesOfUser(anyLong())).willReturn(Set.of(guestRole, userRole));
        Set<RoleDTO> responseRolesDTO = userFacade.getRolesOfUser(1L);

        assertEquals(2, responseRolesDTO.size());
        assertTrue(responseRolesDTO.contains(userRoleDTO));
        assertTrue(responseRolesDTO.contains(guestRoleDTO));
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void testGetRolesOfUserNotInDB() {
        willThrow(UserAndGroupServiceException.class).given(userService).getRolesOfUser(anyLong());
        userFacade.getRolesOfUser(100L);
    }

    @Test
    public void getUsersWithGivenRole() {
        given(userService.getUsersWithGivenRole(1L, null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        PageResultResource<UserDTO> usersDTO = userFacade.getUsersWithGivenRole(1L, null, pageable);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().contains(userDTO1));
        assertTrue(usersDTO.getContent().contains(userDTO2));
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void getUsersWithGivenRoleNotInDB() {
        willThrow(UserAndGroupServiceException.class).given(userService).getUsersWithGivenRole(1L, null, pageable);
        userFacade.getUsersWithGivenRole(1L, null, pageable);
    }

    @Test
    public void getUsersWithGivenRoleType() {
        given(userService.getUsersWithGivenRoleType(userRole.getRoleType(), null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        PageResultResource<UserDTO> usersDTO = userFacade.getUsersWithGivenRoleType(userRole.getRoleType(), null, pageable);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().containsAll(Set.of(userDTO1, userDTO2)));
    }

    @Test(expected = UserAndGroupFacadeException.class)
    public void getUsersWithGivenRoleTypeNotInDB() {
        willThrow(UserAndGroupServiceException.class).given(userService).getUsersWithGivenRoleType(userRole.getRoleType(), null, pageable);
        userFacade.getUsersWithGivenRoleType(userRole.getRoleType(), null, pageable);
    }

    @Test
    public void getUsersWithGivenIds() {
        given(userService.getUsersWithGivenIds(Set.of(user1.getId(), user2.getId()), pageable, null)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        PageResultResource<UserDTO> usersDTO = userFacade.getUsersWithGivenIds(Set.of(user1.getId(), user2.getId()), pageable, null);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().containsAll(Set.of(userDTO1, userDTO2)));
    }

    private static JsonObject createSub(User user){
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), user.getLogin());
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), user.getFullName());
        sub.addProperty(AuthenticatedUserOIDCItems.GIVEN_NAME.getName(), user.getGivenName());
        sub.addProperty(AuthenticatedUserOIDCItems.FAMILY_NAME.getName(), user.getFamilyName());
        sub.addProperty(AuthenticatedUserOIDCItems.ISS.getName(), user.getIss());
        return sub;
    }

}
