package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsGuest;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.*;
import cz.muni.ics.kypo.userandgroup.enums.AbstractCacheNames;
import cz.muni.ics.kypo.userandgroup.enums.dto.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapper;
import cz.muni.ics.kypo.userandgroup.mapping.UserMapper;
import cz.muni.ics.kypo.userandgroup.service.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.IdenticonService;
import cz.muni.ics.kypo.userandgroup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {AbstractCacheNames.USERS_CACHE_NAME})
@Transactional
public class UserFacade {

    private static final int ICON_WIDTH = 75;
    private static final int ICON_HEIGHT = 75;

    private final UserService userService;
    private final IDMGroupService idmGroupService;
    private final IdenticonService identiconService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Autowired
    public UserFacade(UserService userService, IDMGroupService idmGroupService, IdenticonService identiconService, UserMapper userMapper, RoleMapper roleMapper) {
        this.userService = userService;
        this.idmGroupService = idmGroupService;
        this.identiconService = identiconService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserBasicViewDto> getUsers(Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageUserBasicViewDto(userService.getAllUsers(predicate, pageable));
    }

    @IsGuest
    @TransactionalRO
    public PageResultResource<UserBasicViewDto> getUsers(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds) {
        return userMapper.mapToPageUserBasicViewDto(userService.getUsersWithGivenRoleAndNotWithGivenIds(roleType, userIds, predicate, pageable));
    }

    @IsGuest
    public PageResultResource<UserBasicViewDto> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate) {
        return userMapper.mapToPageUserBasicViewDto(userService.getUsersWithGivenIds(ids, pageable, predicate));
    }

    //    @Cacheable(key = "#id", sync = true)
    @IsGuest
    @TransactionalRO
    public UserDTO getUserById(Long id) {
        return userMapper.mapToUserDTOWithRoles(userService.getUserById(id));
    }

    //    @Cacheable(key = "{#sub+#iss}", sync = true)
    @TransactionalRO
    public UserDTO getUserInfo(String sub, String iss) {
        Assert.hasLength(sub, "In method getUserInfo(sub, iss) the input sub must not be empty.");
        Assert.hasLength(iss, "In method getUserInfo(sub, iss) the input iss must not be empty.");
        User user = userService.getUserBySubAndIss(sub, iss)
                .orElseThrow(() ->
                        new EntityNotFoundException(new EntityErrorDetail(User.class, "sub", sub.getClass(), sub, "User not found.")));
        return userMapper.mapToUserDTOWithRoles(user);
    }

    //    @Cacheable(key = "{#sub+#iss}", sync = true)
    // if creation of user fail because of DataIntegrityViolationException, method is repeated one more time which cause that user is updated not created
    @Retryable(value = {DataIntegrityViolationException.class}, maxAttempts = 2)
    public UserDTO createOrUpdateOrGetOIDCUser(UserCreateDTO oidcUserDTO) {
        Assert.notNull(oidcUserDTO, "In method createOrUpdateOrGetOIDCUser(userInfo) the input userInfo must not be null.");

        Optional<User> user = userService.getUserBySubAndIss(oidcUserDTO.getSub(), oidcUserDTO.getIss());
        if (user.isPresent()) {
            return this.updateExistingUserInfo(user.get(), oidcUserDTO);
        } else {
            UserDTO userDTO = this.createNewUserWithDefaultGroupRoles(oidcUserDTO);
            userDTO.setRoles(roleMapper.mapToSetDTO(idmGroupService.getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).getRoles()));
            return userDTO;
        }
    }

    private UserDTO createNewUserWithDefaultGroupRoles(UserCreateDTO oidcUserDTO) {
        User userToCreate = userMapper.mapToEntity(oidcUserDTO);
        userToCreate.setPicture(identiconService.generateIdenticons(oidcUserDTO.getSub() + oidcUserDTO.getIss(), ICON_WIDTH, ICON_HEIGHT));

        User newlyCreatedUser = userService.createUser(userToCreate);
        idmGroupService.getIDMGroupWithRolesByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).addUser(newlyCreatedUser);
        return userMapper.mapToDTO(newlyCreatedUser);
    }

    private UserDTO updateExistingUserInfo(User user, UserCreateDTO oidcUserDTO) {
        if (user.getFullName() == null || !user.getFullName().equals(oidcUserDTO.getFullName())
                || user.getGivenName() == null || !user.getGivenName().equals(oidcUserDTO.getGivenName())
                || user.getFamilyName() == null || !user.getFamilyName().equals(oidcUserDTO.getFamilyName())
                || user.getMail() == null || !user.getMail().equals(oidcUserDTO.getMail())) {

            user.setGivenName(oidcUserDTO.getGivenName());
            user.setFamilyName(oidcUserDTO.getFamilyName());
            user.setFullName(oidcUserDTO.getFullName());
            user.setMail(oidcUserDTO.getMail());
            userService.updateUser(user);
        }
        UserDTO updatedUser = userMapper.mapToDTO(user);
        updatedUser.setRoles(roleMapper.mapToSetDTO(user.getGroups().stream()
                .flatMap(group -> group.getRoles().stream())
                .collect(Collectors.toCollection(HashSet::new))));
        return updatedUser;
    }

    @IsAdmin
    @TransactionalWO
    public void deleteUser(Long id) {
        User user = userService.getUserById(id);
        userService.deleteUser(user);
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
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

    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserForGroupsDTO> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResourceForGroups(userService.getUsersInGroups(groupsIds, predicate, pageable));
    }

    @IsAdmin
    @TransactionalWO
    public void deleteUsers(List<Long> userIds) {
        List<User> usersToBeDeleted = userService.getUsersByIds(userIds);
        usersToBeDeleted.forEach(user -> userService.deleteUser(user));
    }

    public UserDTO updateUser(UserUpdateDTO userUpdateDTO) {
        return userMapper.mapToDTO(userService.updateUser(userMapper.mapToEntity(userUpdateDTO)));
    }

    @IsGuest
    @TransactionalRO
    public Set<RoleDTO> getRolesOfUser(Long id) {
        Set<Role> roles = userService.getRolesOfUser(id);
        return roles.stream()
                .map(role -> roleMapper.mapToRoleDTOWithMicroservice(role))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @IsGuest
    @TransactionalRO
    public PageResultResource<RoleDTO> getRolesOfUserWithPagination(Long id, Pageable pageable, Predicate predicate) {
        Page<Role> rolePage = userService.getRolesOfUserWithPagination(id, pageable, predicate);
        return new PageResultResource<>(rolePage.map(role -> roleMapper.mapToRoleDTOWithMicroservice(role)).getContent(), roleMapper.createPagination(rolePage));
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResource(userService.getUsersWithGivenRole(roleId, predicate, pageable));
    }

    @IsGuest
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResource(userService.getUsersWithGivenRoleType(roleType, predicate, pageable));
    }
//    @IsAdmin
    public InitialOIDCUserDto[] getInitialOIDCUsers() {
        return userService.getInitialOIDCUsers();
    }

}
