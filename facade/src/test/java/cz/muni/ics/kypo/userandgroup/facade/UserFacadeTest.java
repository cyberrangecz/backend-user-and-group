package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.UserFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
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
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.service"})
public class UserFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private UserFacade userFacade;

    @MockBean
    private UserService userService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private BeanMapping beanMapping;

    private User user1, user2;
    private UserDTO userDTO1, userDTO2;
    private Predicate predicate;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
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

        userDTO1 = new UserDTO();
        userDTO1.setLogin("user1");
        userDTO1.setId(1L);
        userDTO1.setFullName("User One");
        userDTO1.setMail("user.one@mail.com");

        userDTO2 = new UserDTO();
        userDTO2.setLogin("user2");
        userDTO2.setId(2L);
        userDTO2.setFullName("User Two");
        userDTO2.setMail("user.two@mail.com");
    }

    @Test
    public void testGetUsers() {
        Page<User> rolePage = new PageImpl<>(Arrays.asList(user1, user2));
        PageResultResource<UserDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(userDTO1, userDTO2));

        given(userService.getAllUsers(predicate, pageable)).willReturn(rolePage);
        given(beanMapping.mapToPageResultDTO(any(Page.class), eq(UserDTO.class))).willReturn(pageResult);
        PageResultResource<UserDTO> pageResultResource = userFacade.getUsers(predicate,pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userDTO1, pageResultResource.getContent().get(0));
        assertEquals(userDTO2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testGetUser() {
        given(userService.get(anyLong())).willReturn(user1);
        given(beanMapping.mapTo(any(User.class), eq(UserDTO.class))).willReturn(userDTO1);
        UserDTO userDTO = userFacade.getUser(1L);

        assertEquals(userDTO1, userDTO);
    }

    @Test
    public void testGetUserWithServiceException() {
        given(userService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        userFacade.getUser(1L);
    }

    @Test
    public void testGetAllUsersNotInGivenGroup() {
        Page<User> rolePage = new PageImpl<>(Arrays.asList(user1, user2));
        PageResultResource<UserDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(userDTO1, userDTO2));

        given(userService.getAllUsersNotInGivenGroup(1L,pageable)).willReturn(rolePage);
        given(beanMapping.mapToPageResultDTO(any(Page.class), eq(UserDTO.class))).willReturn(pageResult);
        PageResultResource<UserDTO> pageResultResource = userFacade.getAllUsersNotInGivenGroup(1L, pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userDTO1, pageResultResource.getContent().get(0));
        assertEquals(userDTO2, pageResultResource.getContent().get(1));
    }

    @Test
    public void testDeleteUser() {
        UserDeletionResponseDTO userDeletionResponseDTO = new UserDeletionResponseDTO();
        userDeletionResponseDTO.setUser(userDTO1);

        given(userService.get(anyLong())).willReturn(user1);
        given(userService.delete(any(User.class))).willReturn(UserDeletionStatus.SUCCESS);
        given(beanMapping.mapTo(any(User.class), eq(UserDeletionResponseDTO.class))).willReturn(userDeletionResponseDTO);
        userDeletionResponseDTO = userFacade.deleteUser(1L);

        assertEquals(UserDeletionStatus.SUCCESS, userDeletionResponseDTO.getStatus());
        then(userService).should().delete(user1);

    }

    @Test
    public void testDeleteUserNotFound() {
        given(userService.get(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        userFacade.deleteUser(1L);
    }

    @Test
    public void testDeleteUsers() {
        Map<User, UserDeletionStatus> deletionStatusMap = new HashMap<>();
        deletionStatusMap.put(user1, UserDeletionStatus.SUCCESS);
        deletionStatusMap.put(user2, UserDeletionStatus.NOT_FOUND);

        UserDeletionResponseDTO userDeletionResponseDTO1 = new UserDeletionResponseDTO();
        userDeletionResponseDTO1.setUser(userDTO1);
        UserDeletionResponseDTO userDeletionResponseDTO2 = new UserDeletionResponseDTO();
        userDeletionResponseDTO2.setUser(userDTO2);

        given(userService.deleteUsers(anyList())).willReturn(deletionStatusMap);
        given(beanMapping.mapTo(user1, UserDeletionResponseDTO.class)).willReturn(userDeletionResponseDTO1);
        given(beanMapping.mapTo(user2, UserDeletionResponseDTO.class)).willReturn(userDeletionResponseDTO2);
        List<UserDeletionResponseDTO> userDeletionResponseDTOS = userFacade.deleteUsers(Arrays.asList(1L, 2L));

        assertEquals(2,userDeletionResponseDTOS.size());
        assertEquals(UserDeletionStatus.SUCCESS, userDeletionResponseDTOS.get(0).getStatus());
        assertEquals(UserDeletionStatus.NOT_FOUND, userDeletionResponseDTOS.get(1).getStatus());
    }

    @Test
    public void testGetRolesOfUser() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setRoleType(RoleType.ADMINISTRATOR.toString());

        Role role2 = new Role();
        role2.setId(2L);
        role2.setRoleType(RoleType.USER.toString());

        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        RoleDTO roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setRoleType(RoleType.ADMINISTRATOR.toString());

        RoleDTO roleDTO2 = new RoleDTO();
        roleDTO2.setId(1L);
        roleDTO2.setRoleType(RoleType.USER.toString());

        Set<RoleDTO> roleDTOS = new HashSet<>();
        roleDTOS.add(roleDTO1);
        roleDTOS.add(roleDTO2);

        given(userService.getRolesOfUser(anyLong())).willReturn(roles);
        given(beanMapping.mapToSet(anySet(), eq(RoleDTO.class))).willReturn(roleDTOS);
        Set<RoleDTO> responseRolesDTO = userFacade.getRolesOfUser(1L);

        assertTrue(responseRolesDTO.contains(roleDTO1));
        assertTrue(responseRolesDTO.contains(roleDTO2));
    }

    @Test
    public void isGroupInternal() {
        given(userService.isUserInternal(user1.getId())).willReturn(true);
        assertTrue(userFacade.isUserInternal(user1.getId()));
        then(userService).should().isUserInternal(user1.getId());
    }

    @Test
    public void isGroupExternal() {
        user1.setExternalId(1L);
        given(userService.isUserInternal(user1.getId())).willReturn(false);
        assertFalse(userFacade.isUserInternal(user1.getId()));
        then(userService).should().isUserInternal(user1.getId());
    }

    @Test
    public void isGroupInternalWhenServiceThrowsException() {
        given(userService.isUserInternal(user1.getId())).willThrow(UserAndGroupServiceException.class);
        thrown.expect(UserAndGroupFacadeException.class);
        userFacade.isUserInternal(user1.getId());
    }
}
