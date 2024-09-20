package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdminOrPowerUser;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsTrainee;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.UsersImportDTO;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.*;
import cz.muni.ics.kypo.userandgroup.enums.AbstractCacheNames;
import cz.muni.ics.kypo.userandgroup.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.enums.dto.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.exceptions.SecurityException;
import cz.muni.ics.kypo.userandgroup.exceptions.UnprocessableEntityException;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapper;
import cz.muni.ics.kypo.userandgroup.mapping.UserMapper;
import cz.muni.ics.kypo.userandgroup.service.*;
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
    private final SecurityService securityService;
    private final RoleService roleService;
    private final IdenticonService identiconService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Autowired
    public UserFacade(UserService userService,
                      IDMGroupService idmGroupService,
                      SecurityService securityService, RoleService roleService,
                      IdenticonService identiconService,
                      UserMapper userMapper,
                      RoleMapper roleMapper) {
        this.userService = userService;
        this.idmGroupService = idmGroupService;
        this.securityService = securityService;
        this.roleService = roleService;
        this.identiconService = identiconService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserBasicViewDto> getUsers(Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageUserBasicViewDto(userService.getAllUsers(predicate, pageable));
    }

    @IsAdminOrPowerUser
    @TransactionalRO
    public PageResultResource<UserBasicViewDto> getUsers(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds) {
        return userMapper.mapToPageUserBasicViewDto(userService.getUsersWithGivenRoleAndNotWithGivenIds(roleType, userIds, predicate, pageable));
    }

    @IsTrainee
    @TransactionalRO
    public PageResultResource<UserBasicViewDto> getUsersWithGivenIds(List<Long> ids, Pageable pageable, Predicate predicate) {
        if (securityService.canRetrieveAnyInformation()) {
            return userMapper.mapToPageUserBasicViewDto(userService.getUsersWithGivenIds(ids, pageable, predicate));
        }
        return userMapper.mapToPageUserBasicViewDTOAnonymize(
                userService.getUsersWithGivenIds(ids, pageable, predicate),
                securityService.getLoggedInUser().getId());
    }

    @IsTrainee
    @TransactionalRO
    public UserDTO getUserById(Long id) {
        if (securityService.canRetrieveAnyInformation()) {
            return userMapper.mapToUserDTOWithRoles(userService.getUserById(id));
        }

        User loggedInUser = securityService.getLoggedInUser();
        // user can always retrieve himself
        if (loggedInUser.getId() == id) {
            return userMapper.mapToUserDTOWithRoles(loggedInUser);
        }
        throw new SecurityException("Cannot retrieve information about another user with current authorization.");
    }

    //    @Cacheable(key = "{#sub+#iss}", sync = true)
    @IsTrainee
    @TransactionalRO
    public UserDTO getUserInfo() {
        return userMapper.mapToUserDTOWithRoles(securityService.getLoggedInUser());
    }

    //    @Cacheable(key = "{#sub+#iss}", sync = true)
    // if creation of user fail because of DataIntegrityViolationException, method is repeated one more time which cause that user is updated not created
    @Retryable(value = {DataIntegrityViolationException.class}, maxAttempts = 2)
    public UserDTO createOrUpdateOrGetOIDCUser(UserCreateDTO oidcUserDTO) {
        Assert.notNull(oidcUserDTO, "In method createOrUpdateOrGetOIDCUser(userInfo) the input userInfo must not be null.");

        if (oidcUserDTO.getGivenName() == null || oidcUserDTO.getFullName() == null || oidcUserDTO.getFamilyName() == null) {
            throw new UnprocessableEntityException(new EntityErrorDetail(UserCreateDTO.class, "User must provide access to their name."));
        }

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
        usersToBeDeleted.forEach(userService::deleteUser);
    }

    @IsAdminOrPowerUser
    @TransactionalRO
    public Set<RoleDTO> getRolesOfUser(Long id) {
        Set<Role> roles = userService.getRolesOfUser(id);
        return roles.stream()
                .map(roleMapper::mapToRoleDTOWithMicroservice)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @IsAdminOrPowerUser
    @TransactionalRO
    public PageResultResource<RoleDTO> getRolesOfUserWithPagination(Long id, Pageable pageable, Predicate predicate) {
        Page<Role> rolePage = userService.getRolesOfUserWithPagination(id, pageable, predicate);
        return new PageResultResource<>(rolePage.map(roleMapper::mapToRoleDTOWithMicroservice).getContent(), roleMapper.createPagination(rolePage));
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResource(userService.getUsersWithGivenRole(roleId, predicate, pageable));
    }

    @IsAdminOrPowerUser
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResource(userService.getUsersWithGivenRoleType(roleType, predicate, pageable));
    }
    @IsAdmin
    public byte[] getInitialOIDCUsers() {
        return userService.getInitialOIDCUsers();
    }

    @IsAdmin
    @TransactionalWO
    public void importUsers(UsersImportDTO usersImportDTO) {
        Set<User> importedUsers = userMapper.mapUsersImportToSet(usersImportDTO.getUsers());
        for (User user : importedUsers) {
            user.setPicture(identiconService.generateIdenticons(user.getSub() + user.getIss(), ICON_WIDTH, ICON_HEIGHT));
        }
        Set<User> storedUsers = new HashSet<>(userService.createUsers(importedUsers));
        // add users to default group
        IDMGroup defaultGroup = idmGroupService.getGroupForDefaultRoles();
        storedUsers.forEach(defaultGroup::addUser);

        if(usersImportDTO.getGroupName() != null && !usersImportDTO.getGroupName().isBlank()) {
            IDMGroup group = new IDMGroup();
            group.setName(usersImportDTO.getGroupName());
            group.setDescription("No description");
            group.setStatus(UserAndGroupStatus.VALID);
            idmGroupService.createIDMGroup(group, new ArrayList<>());
            storedUsers.forEach(group::addUser);
        }
    }
}
