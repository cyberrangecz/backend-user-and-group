package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Service
public class UserServiceImpl implements UserService {

    private static Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class.getName());

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, IDMGroupRepository groupRepository,
                            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public User get(Long id) {
        LOG.debug("get({})", id);
        Assert.notNull(id, "Input id must not be null");
        return userRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("User with id " + id + " not found"));
    }

    @Override
    @IsAdmin
    public UserDeletionStatusDTO delete(User user) {
        Assert.notNull(user, "Input user must not be null");
        UserDeletionStatusDTO deletionCheck = checkKypoUserBeforeDelete(user);
        if (deletionCheck.equals(UserDeletionStatusDTO.SUCCESS)) {
            userRepository.delete(user);
            LOG.debug("IDM user with id: {} was successfully deleted.", user.getId());
        }
        return deletionCheck;
    }

    @Override
    @IsAdmin
    public Map<User, UserDeletionStatusDTO> deleteUsers(List<Long> idsOfUsers) {
        LOG.debug("deleteUsers({}) ", idsOfUsers);
        Assert.notNull(idsOfUsers, "Input ids of users must not be null");
        Map<User, UserDeletionStatusDTO> response = new HashMap<>();

        idsOfUsers.forEach(id -> {
            User user = null;
            try {
                user = get(id);
                try {
                    UserDeletionStatusDTO status = delete(user);
                    response.put(user, status);
                } catch (Exception ex) {
                    response.put(user, UserDeletionStatusDTO.ERROR);
                }
            } catch (UserAndGroupServiceException ex) {
                user = new User();
                user.setId(id);
                response.put(user, UserDeletionStatusDTO.NOT_FOUND);
            }
        });

        return response;
    }

    @Override
    @IsAdmin
    public void changeAdminRole(Long id) {
        LOG.debug("changeAdminRole({})", id);
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup.orElseThrow(() -> new UserAndGroupServiceException("Administrator group could not be  found."));
        if (user.getGroups().contains(administratorGroup)) {
            user.removeGroup(administratorGroup);
        } else {
            user.addGroup(administratorGroup);
        }
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public boolean isUserAdmin(Long id) {
        LOG.debug("isUserAdmin({})", id);
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup.orElseThrow(() -> new UserAndGroupServiceException("Administrator group could not be found."));

        return user.getGroups().contains(administratorGroup);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameLogin(#login)")
    public User getUserByLogin(String login) {
        LOG.debug("getUserByLogin({})", login);
        Assert.hasLength(login, "Input login must not be empty");
        return userRepository.findByLogin(login).orElseThrow(() -> new UserAndGroupServiceException("User with login " + login + " could not be found"));
    }

    @Override
    @IsAdmin
    public Page<User> getAllUsers(Predicate predicate, Pageable pageable) {
        LOG.debug("getAllUsers()");
        return userRepository.findAll(predicate, pageable);
    }

    @Override
    @IsAdmin
    public Page<User> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable) {
        LOG.debug("getAllUsersNotInGivenGroup({})", groupId);
        return userRepository.usersNotInGivenGroup(groupId, pageable);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public User getUserWithGroups(Long id) {
        LOG.debug("getUserWithGroups({})", id);
        Assert.notNull(id, "Input id must not be null");
        return userRepository.getUserByIdWithGroups(id).orElseThrow(() -> new UserAndGroupServiceException("User with id " + id + " not found"));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameLogin(#login)")
    public User getUserWithGroups(String login) {
        LOG.debug("getUserWithGroups({})", login);
        Assert.hasLength(login, "Input login must not be empty");
        return userRepository.getUserByLoginWithGroups(login).orElseThrow(() -> new UserAndGroupServiceException("User with login " + login + " not found"));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public boolean isUserInternal(Long id) {
        LOG.debug("isUserInternal({})", id);
        Assert.notNull(id, "Input id must not be null");
        if (!userRepository.existsById(id)) {
            throw new UserAndGroupServiceException("User with id " + id + " could not be found.");
        }
        return userRepository.isUserInternal(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_GUEST)")
    public Set<Role> getRolesOfUser(Long id) {
        LOG.debug("getRolesOfUser({})", id);
        Assert.notNull(id, "Input id must not be null");
        if (!userRepository.existsById(id)) {
            throw new UserAndGroupServiceException("User with id " + id + " could not be found.");
        }
        return userRepository.getRolesOfUser(id);
    }

    @Override
    public Page<User> getUsersInGroups(Set<Long> groupsIds, Pageable pageable) {
        LOG.debug("getUsersInGroups({})", groupsIds);
        return userRepository.usersInGivenGroups(groupsIds, pageable);
    }

    private UserDeletionStatusDTO checkKypoUserBeforeDelete(User user) {
        if (user.getExternalId() != null && user.getStatus().equals(UserAndGroupStatus.VALID)) {
            return UserDeletionStatusDTO.EXTERNAL_VALID;
        }
        return UserDeletionStatusDTO.SUCCESS;
    }

    @Override
    public Page<User> getUsersWithGivenRole(Long roleId, Pageable pageable) {
        LOG.debug("getUsersWithGivenRole({})", roleId);
        Assert.notNull(roleId, "Input role type must not be null");
        if (!roleRepository.existsById(roleId)) {
            throw new UserAndGroupServiceException("Role with id: " + roleId + " could not be found.");
        }
        return userRepository.findAllByRoleId(roleId, pageable);
    }

    @Override
    public Page<User> getUsersWithGivenRole(String roleType, Pageable pageable) {
        LOG.debug("getUsersWithGivenRole({})", roleType);
        Assert.notNull(roleType, "Input role type must not be null");
        Role role = roleRepository.findByRoleType(roleType).orElseThrow(() ->
                new UserAndGroupServiceException("Role with role type: " + roleType + " could not be found."));
        return userRepository.findAllByRoleId(role.getId(), pageable);
    }
}
