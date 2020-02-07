package cz.muni.ics.kypo.userandgroup.facade;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsGuest;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserUpdateDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.enums.AbstractCacheNames;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.UserMapper;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.service.impl.IdenticonService;
import cz.muni.ics.kypo.userandgroup.service.impl.SecurityService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {AbstractCacheNames.USERS_CACHE_NAME})
@Transactional
public class UserFacadeImpl implements UserFacade {

    private static final int ICON_WIDTH = 75;
    private static final int ICON_HEIGHT = 75;

    @Value("${service.name}")
    private String nameOfUserAndGroupService;

    private UserService userService;
    private IDMGroupService idmGroupService;
    private IdenticonService identiconService;
    private SecurityService securityService;
    private UserMapper userMapper;
    private RoleMapper roleMapper;

    @Autowired
    public UserFacadeImpl(UserService userService, IDMGroupService idmGroupService, IdenticonService identiconService, SecurityService securityService, UserMapper userMapper, RoleMapper roleMapper) {
        this.userService = userService;
        this.idmGroupService = idmGroupService;
        this.identiconService = identiconService;
        this.securityService = securityService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResource(userService.getAllUsers(predicate, pageable));
    }

    @Override
    @IsGuest
    @TransactionalRO
    public PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds) {
        return userMapper.mapToPageResultResource(userService.getUsersWithGivenRoleAndNotWithGivenIds(roleType, userIds, predicate, pageable));
    }

    @Override
//    @Cacheable(key = "#id", sync = true)
    @IsGuest
    @TransactionalRO
    public UserDTO getUserById(Long id) {
        Assert.notNull(id, "In method getUserById(id) the input id must not be null.");
        try {
            return userMapper.mapToUserDTOWithRoles(userService.getUserById(id));
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

    @Override
//    @Cacheable(key = "{#sub+#iss}", sync = true)
    @TransactionalRO
    public UserDTO getUserInfo(String sub, String iss) {
        Assert.hasLength(sub, "In method getUserInfo(sub, iss) the input sub must not be empty.");
        Assert.hasLength(iss, "In method getUserInfo(sub, iss) the input iss must not be empty.");
        User user = userService.getUserByLoginAndIss(sub, iss)
                .orElseThrow(() ->
                        new UserAndGroupFacadeException(
                        new UserAndGroupServiceException("User with sub: " + sub + " and iss: + " + iss + " could not be found.", ErrorCode.RESOURCE_NOT_FOUND)));
        return userMapper.mapToUserDTOWithRoles(user);
    }

    @Override
    //    @Cacheable(key = "{#sub+#iss}", sync = true)
    public UserDTO createOrUpdateOrGetOIDCUser(String sub, String iss, JsonObject introspectionResponse) {
        Assert.hasLength(sub, "In method createOrUpdateOrGetOIDCUser(sub, iss) the input sub must not be empty.");
        Assert.hasLength(iss, "In method createOrUpdateOrGetOIDCUser(sub, iss) the input iss must not be empty.");

        Optional<User> user = userService.getUserByLoginAndIss(sub, iss);
        if (user.isPresent()) {
            return this.updateExistingUserInfo(user.get(), introspectionResponse);
        } else {
            UserDTO userDTO = this.createNewUserWithDefaultGroupRoles(sub, iss, introspectionResponse);
            userDTO.setRoles(roleMapper.mapToSetDTO(idmGroupService.getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).getRoles()));
            return userDTO;
        }
    }

    private UserDTO createNewUserWithDefaultGroupRoles(String login, String issuer, JsonObject introspectionResponse) {
        User userToCreate = new User();
        userToCreate.setLogin(login);
        userToCreate.setIss(issuer);
        userToCreate.setFullName(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.NAME));
        userToCreate.setMail(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.EMAIL));
        userToCreate.setGivenName(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.GIVEN_NAME));
        userToCreate.setFamilyName(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.FAMILY_NAME));
        userToCreate.setPicture(identiconService.generateIdenticons(login + issuer, ICON_WIDTH, ICON_HEIGHT));

        User newlyCreatedUser = userService.createUser(userToCreate);
        idmGroupService.getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).addUser(newlyCreatedUser);
        return userMapper.mapToDTO(newlyCreatedUser);
    }

    private UserDTO updateExistingUserInfo(User user, JsonObject introspectionResponse) {
        if (user.getFullName() == null || !user.getFullName().equals(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.NAME))
                || user.getGivenName() == null || !user.getGivenName().equals(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.GIVEN_NAME))
                || user.getFamilyName() == null || !user.getFamilyName().equals(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.FAMILY_NAME))
                || user.getMail() == null || !user.getMail().equals(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.EMAIL))) {

            user.setGivenName(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.GIVEN_NAME));
            user.setFamilyName(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.FAMILY_NAME));
            user.setFullName(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.NAME));
            user.setMail(getIntrospectionField(introspectionResponse, AuthenticatedUserOIDCItems.EMAIL));
            userService.updateUser(user);
        }
        UserDTO updatedUser = userMapper.mapToDTO(user);
        updatedUser.setRoles(roleMapper.mapToSetDTO(user.getGroups().stream()
                .flatMap(group -> group.getRoles().stream())
                .collect(Collectors.toCollection(HashSet::new))));
        return updatedUser;
    }

    private String getIntrospectionField(JsonObject introspectionResponse, AuthenticatedUserOIDCItems userOIDCFields) {
        return introspectionResponse.get(userOIDCFields.getName()) == null ? "" : introspectionResponse.get(userOIDCFields.getName()).getAsString();
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void deleteUser(Long id) {
        Assert.notNull(id, "In method deleteUser(id) the input id must not be null.");
        try {
            User user = userService.getUserById(id);
            userService.deleteUser(user);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        Assert.notNull(groupId, "In method getAllUsersNotInGivenGroup(groupId) the input id must not be null.");
        PageResultResource<UserDTO> users = userMapper.mapToPageResultResource(userService.getAllUsersNotInGivenGroup(groupId, predicate, pageable));
        List<UserDTO> usersWithRoles = users.getContent().stream()
                .map(userDTO -> {
                    userDTO.setRoles(this.getRolesOfUser(userDTO.getId()));
                    return userDTO;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        users.setContent(usersWithRoles);
        return users;
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserForGroupsDTO> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
        Assert.notEmpty(groupsIds, "In method getUsersInGroups(groupsIds) the input groupsIds must not be empty.");
        return userMapper.mapToPageResultResourceForGroups(userService.getUsersInGroups(groupsIds, predicate, pageable));
    }

    @Override
    @IsAdmin
    @TransactionalWO
    public void deleteUsers(List<Long> userIds) {
        Assert.notEmpty(userIds, "In method deleteUsers(ids) the input ids must not be empty.");
        List<User> usersToBeDeleted = userService.getUsersByIds(userIds);
        usersToBeDeleted.forEach(user -> userService.deleteUser(user));
    }

    @Override
    public UserDTO updateUser(UserUpdateDTO userUpdateDTO) {
        return userMapper.mapToDTO(userService.updateUser(userMapper.mapToEntity(userUpdateDTO)));
    }

    @Override
    @IsGuest
    @TransactionalRO
    public Set<RoleDTO> getRolesOfUser(Long id) {
        Assert.notNull(id, "In method getRolesOfUser(id) the input id must not be null.");
        try {
            Set<Role> roles = userService.getRolesOfUser(id);
            return roles.stream()
                    .map(role -> roleMapper.mapToRoleDTOWithMicroservice(role))
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
        Assert.notNull(roleId, "In method getUsersWithGivenRole(roleId) the input roleId must not be null.");
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenRole(roleId, predicate, pageable));
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

    @Override
    @IsGuest
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable) {
        Assert.hasLength(roleType, "In method getUsersWithGivenRoleType(roleType) the input roleType must not be null.");
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenRoleType(roleType, predicate, pageable));
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

    @Override
    @IsGuest
    public PageResultResource<UserDTO> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate) {
        Assert.notEmpty(ids, "In method getUsersWithGivenIds(ids) the input ids must not be null.");
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenIds(ids, pageable, predicate));
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

}
