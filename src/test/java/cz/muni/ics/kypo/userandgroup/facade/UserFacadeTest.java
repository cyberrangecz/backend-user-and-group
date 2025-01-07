package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.*;
import cz.muni.ics.kypo.userandgroup.enums.dto.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.exceptions.SecurityException;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.UserMapperImpl;
import cz.muni.ics.kypo.userandgroup.service.*;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = { TestDataFactory.class, RoleMapperImpl.class, UserMapperImpl.class })
public class UserFacadeTest {

    @Autowired
    private TestDataFactory testDataFactory;
    private UserFacade userFacade;
    @Mock
    private UserService userService;
    @Mock
    private IdenticonService identiconService;
    @Mock
    private IDMGroupService idmGroupService;
    @Mock
    private RoleService roleService;

    @Mock
    private SecurityService securityService;
    @Autowired
    private UserMapperImpl userMapper;
    @Autowired
    private RoleMapperImpl roleMapper;

    private AutoCloseable closeable;
    private User user1, user2;
    private UserDTO userDTO1, userDTO2;
    private UserBasicViewDto userBasicViewDto1, userBasicViewDto2;
    private UserForGroupsDTO userForGroupsDTO1, userForGroupsDTO2;
    private UserUpdateDTO userUpdateDTO;
    private Role traineeRole, powerUserRole;
    private RoleDTO traineeRoleDTO, powerUserRoleDTO;
    private Microservice microservice;
    private Predicate predicate;
    private Pageable pageable;
    private IDMGroup adminGroup, defaultGroup;

    private static UserCreateDTO createOIDCUserInfo(User user) {
        UserCreateDTO oidcUserInfo = new UserCreateDTO();
        oidcUserInfo.setSub(user.getSub());
        oidcUserInfo.setFullName(user.getFullName());
        oidcUserInfo.setGivenName(user.getGivenName());
        oidcUserInfo.setFamilyName(user.getFamilyName());
        oidcUserInfo.setIss(user.getIss());
        return oidcUserInfo;
    }

    @BeforeEach
    public void init() {
        closeable = MockitoAnnotations.openMocks(this);
        userFacade = new UserFacade(userService, idmGroupService, securityService, roleService, identiconService, userMapper, roleMapper);

        microservice = testDataFactory.getKypoUaGMicroservice();
        microservice.setId(1L);

        user1 = testDataFactory.getUser1();
        user1.setId(1L);
        user2 = testDataFactory.getUser2();
        user2.setId(2L);

        userBasicViewDto1 = testDataFactory.getUserBasicViewDto1();
        userBasicViewDto1.setId(1L);
        userBasicViewDto2 = testDataFactory.getUserBasicViewDto2();
        userBasicViewDto2.setId(2L);

        adminGroup = testDataFactory.getUAGAdminGroup();
        adminGroup.setId(1L);
        adminGroup.addUser(user1);
        defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setId(2L);
        defaultGroup.addUser(user1);

        traineeRole = testDataFactory.getUAGTraineeRole();
        traineeRole.setId(1L);
        traineeRole.setMicroservice(microservice);
        traineeRoleDTO = testDataFactory.getUAGTraineeRoleDTO();
        traineeRoleDTO.setId(traineeRole.getId());

        powerUserRole = testDataFactory.getUAGPowerUserRole();
        powerUserRole.setId(1L);
        powerUserRole.setMicroservice(microservice);
        powerUserRoleDTO = testDataFactory.getUAGPowerUserRoleDTO();
        powerUserRoleDTO.setId(powerUserRole.getId());

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

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetUsers() {
        Page<User> rolePage = new PageImpl<>(Arrays.asList(user1, user2));
        PageResultResource<UserBasicViewDto> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(userBasicViewDto1, userBasicViewDto2));

        given(userService.getAllUsers(predicate, pageable)).willReturn(rolePage);
        PageResultResource<UserBasicViewDto> pageResultResource = userFacade.getUsers(predicate, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userBasicViewDto1, pageResultResource.getContent().get(0));
        assertEquals(userBasicViewDto2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testGetUserInfo() {
        Set<Role> expectedRoles = new HashSet<>();
        for (IDMGroup groupOfUser : user1.getGroups()) {
            expectedRoles.addAll(groupOfUser.getRoles());
        }

        given(securityService.getLoggedInUser()).willReturn(user1);
        UserDTO userDTO = userFacade.getUserInfo();

        userDTO1.setId(user1.getId());
        assertEquals(userDTO1, userDTO);
        for (Role role : expectedRoles) {
            RoleDTO expectedRole = new RoleDTO();
            expectedRole.setId(role.getId());
            expectedRole.setIdOfMicroservice(role.getMicroservice().getId());
            expectedRole.setNameOfMicroservice(role.getMicroservice().getName());
            expectedRole.setRoleType(role.getRoleType());
            assertTrue(userDTO.getRoles().contains(expectedRole));
        }
    }

    @Test
    public void testGetUserInfoWithEmptyUserOptional() {
        given(securityService.getLoggedInUser()).willThrow(SecurityException.class);
        assertThrows(SecurityException.class, () -> userFacade.getUserInfo());
    }

    @Test
    public void testGetUserByIdHimself() {
        given(securityService.canRetrieveAnyInformation()).willReturn(false);
        given(securityService.getLoggedInUser()).willReturn(user1);
        given(userService.getUserById(user1.getId())).willReturn(user1);
        UserDTO userDTO = userFacade.getUserById(user1.getId());

        assertEquals(userDTO1, userDTO);
    }

    @Test
    public void testGetUserByIdFailAuth() {
        given(securityService.canRetrieveAnyInformation()).willReturn(false);
        given(securityService.getLoggedInUser()).willReturn(user1);
        assertThrows(SecurityException.class, () -> userFacade.getUserById(user2.getId()));
    }

    @Test
    public void testGetUserByIdAny() {
        given(securityService.getLoggedInUser()).willReturn(user1);
        given(securityService.canRetrieveAnyInformation()).willReturn(true);
        given(userService.getUserById(user2.getId())).willReturn(user2);
        UserDTO userDTO = userFacade.getUserById(user2.getId());
        assertEquals(userDTO, userDTO2);
    }

    @Test
    public void testCreateOrUpdateOrGetOIDCUserCreate() {
        given(userService.getUserBySubAndIss(user1.getSub(), user1.getIss())).willReturn(Optional.empty());
        given(userService.createUser(user1)).willReturn(user1);
        given(idmGroupService.getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName())).willReturn(defaultGroup);
        userFacade.createOrUpdateOrGetOIDCUser(createOIDCUserInfo(user1));
    }

    @Test
    public void testCreateOrUpdateOrGetOIDCUserUpdate() {
        given(userService.getUserBySubAndIss(user1.getSub(), user1.getIss())).willReturn(Optional.ofNullable(user1));
        userFacade.createOrUpdateOrGetOIDCUser(createOIDCUserInfo(user1));

        then(userService).should().updateUser(user1);
    }

    @Test
    public void testCreateOrUpdateOrGetOIDCUserUpdateUserWithNullAttributes() {
        User userToUpdate = new User();
        userToUpdate.setId(user1.getId());
        userToUpdate.setSub(user1.getSub());
        userToUpdate.setIss(user1.getIss());
        given(userService.getUserBySubAndIss(user1.getSub(), user1.getIss())).willReturn(Optional.ofNullable(userToUpdate));
        userFacade.createOrUpdateOrGetOIDCUser(createOIDCUserInfo(user1));

        then(userService).should().updateUser(user1);
    }

    @Test
    public void testDeleteUser() {
        user1.setId(1L);
        given(userService.getUserById(anyLong())).willReturn(user1);
        userFacade.deleteUser(user1.getId());
        then(userService).should().deleteUser(user1);
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
        given(userService.getUsersByIds(List.of(user1.getId(), user2.getId()))).willReturn(List.of(user1, user2));
        userFacade.deleteUsers(List.of(user1.getId(), user2.getId()));
        then(userService).should().deleteUser(user1);
        then(userService).should().deleteUser(user2);
    }

    @Test
    public void testGetRolesOfUser() {
        powerUserRoleDTO.setNameOfMicroservice(microservice.getName());
        traineeRoleDTO.setNameOfMicroservice(microservice.getName());
        given(userService.getRolesOfUser(anyLong())).willReturn(Set.of(traineeRole, powerUserRole));
        Set<RoleDTO> responseRolesDTO = userFacade.getRolesOfUser(1L);

        assertEquals(2, responseRolesDTO.size());
        assertTrue(responseRolesDTO.contains(powerUserRoleDTO));
        assertTrue(responseRolesDTO.contains(traineeRoleDTO));
    }

    @Test
    public void testGetRolesOfUserWithPagination() {
        powerUserRoleDTO.setNameOfMicroservice(microservice.getName());
        traineeRoleDTO.setNameOfMicroservice(microservice.getName());
        given(userService.getRolesOfUserWithPagination(eq(user1.getId()), eq(pageable), eq(predicate))).willReturn(new PageImpl<>(List.of(traineeRole, powerUserRole)));
        PageResultResource<RoleDTO> responseRolesDTO = userFacade.getRolesOfUserWithPagination(user1.getId(), pageable, predicate);

        assertEquals(2, responseRolesDTO.getContent().size());
        assertTrue(responseRolesDTO.getContent().contains(powerUserRoleDTO));
        assertTrue(responseRolesDTO.getContent().contains(traineeRoleDTO));
    }

    @Test
    public void getUsersWithGivenRole() {
        given(userService.getUsersWithGivenRole(1L, null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        PageResultResource<UserDTO> usersDTO = userFacade.getUsersWithGivenRole(1L, null, pageable);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().contains(userDTO1));
        assertTrue(usersDTO.getContent().contains(userDTO2));
    }

    @Test
    public void getUsersWithGivenRoleType() {
        given(securityService.getLoggedInUser()).willReturn(user1);
        given(userService.getUsersWithGivenRoleType(powerUserRole.getRoleType(), null, pageable)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        PageResultResource<UserDTO> usersDTO = userFacade.getUsersWithGivenRoleType(powerUserRole.getRoleType(), null, pageable);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().containsAll(Set.of(userDTO1, userDTO2)));
    }

    @Test
    public void getUsersWithGivenIds() {
        given(securityService.canRetrieveAnyInformation()).willReturn(true);
        given(securityService.getLoggedInUser()).willReturn(user1);
        given(userService.getUsersWithGivenIds(List.of(user1.getId(), user2.getId()), pageable, null)).willReturn(new PageImpl<>(Arrays.asList(user1, user2)));
        PageResultResource<UserBasicViewDto> usersDTO = userFacade.getUsersWithGivenIds(List.of(user1.getId(), user2.getId()), pageable, null);

        assertEquals(2, usersDTO.getContent().size());
        assertTrue(usersDTO.getContent().containsAll(Set.of(userBasicViewDto1, userBasicViewDto2)));
    }

    @Test
    public void getUsersWithGivenIdsAnonymize() {
        List<Long> userIds = List.of(user1.getId(), user2.getId());
        given(securityService.canRetrieveAnyInformation()).willReturn(false);
        given(securityService.getLoggedInUser()).willReturn(user1);
        given(userService.getUsersWithGivenIds(userIds, pageable, null)).willReturn(new PageImpl<>(List.of(user1, user2)));
        PageResultResource<UserBasicViewDto> usersDTO = userFacade.getUsersWithGivenIds(userIds, pageable, null);

        assertEquals(2, usersDTO.getContent().size());
        for (UserBasicViewDto userBasicViewDto : usersDTO.getContent()) {
            if (userBasicViewDto.getId() == user1.getId()) {
                assertEquals(userBasicViewDto, userBasicViewDto1);
            } else {
                assertEquals(userBasicViewDto.getId(), user2.getId());
                assertEquals(userBasicViewDto.getGivenName(), "other");
                assertEquals(userBasicViewDto.getFamilyName(), "player");
            }
        }
    }

}
