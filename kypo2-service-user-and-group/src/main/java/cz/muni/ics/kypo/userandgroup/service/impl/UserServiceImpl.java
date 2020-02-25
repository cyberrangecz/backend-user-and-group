package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.QUser;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class.getName());

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, IDMGroupRepository groupRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User getUserById(Long id) {
        Assert.notNull(id, "In method getUserById(id) the input must not be null.");
        return userRepository.findById(id)
                .orElseThrow(() -> new UserAndGroupServiceException("User with id " + id + " could not be found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Optional<User> getUserByLoginAndIss(String login, String iss) {
        Assert.notNull(login, "In method getUserBySubAndIss(login) the input must not be null.");
        Assert.notNull(iss, "In method getUserBySubAndIss(iss) the input must not be null.");
        return userRepository.findByLoginAndIss(login, iss);
    }

    @Override
    public Page<User> getAllUsers(Predicate predicate, Pageable pageable) {
        return userRepository.findAll(predicate, pageable);
    }

    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        Assert.notNull(userIds, "In method getUsersByIds(userIds) the input must not be null.");
        return userRepository.findByIdIn(userIds);
    }

    @Override
    public void deleteUser(User user) {
        Assert.notNull(user, "In method deleteUser(user) the input must not be null.");
        for (IDMGroup group : user.getGroups()) {
            group.removeUser(user);
        }
        userRepository.delete(user);
        LOG.debug("IDM user with id: {} was successfully deleted.", user.getId());
    }

    @Override
    public User createUser(User user) {
        Assert.notNull(user, "In method createUser(user) the input must not be null.");
        return userRepository.saveAndFlush(user);
    }

    @Override
    public User updateUser(User user) {
        Assert.notNull(user, "In method updateUser(user) the input must not be null.");
        return userRepository.saveAndFlush(user);
    }

    @Override
    public void changeAdminRole(Long id) {
        Assert.notNull(id, "In method changeAdminRole(id) the input must not be null.");
        User user = this.getUserById(id);
        //TODO create new private method to get administrator group
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup
                .orElseThrow(() -> new UserAndGroupServiceException("Administrator group could not be found.", ErrorCode.RESOURCE_NOT_FOUND));
        if (user.getGroups().contains(administratorGroup)) {
            administratorGroup.removeUser(user);
        } else {
            administratorGroup.addUser(user);
        }
        userRepository.save(user);
    }

    @Override
    public boolean isUserAdmin(Long id) {
        Assert.notNull(id, "In method isUserAdmin(id) the input must not be null.");
        User user = getUserById(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup
                .orElseThrow(() -> new UserAndGroupServiceException("Administrator group could not be found.", ErrorCode.RESOURCE_NOT_FOUND));
        return user.getGroups().contains(administratorGroup);
    }

    @Override
    public Page<User> getUsersWithGivenRoleAndNotWithGivenIds(String roleType, Set<Long> userIds, Predicate predicate, Pageable pageable) {
        Assert.notNull(roleType, "In method getUsersWithGivenRoleAndNotWithGivenIds(roleType, userIds, predicate, pageable) the input role type must not be null.");
        Assert.notNull(userIds, "In method getUsersWithGivenRoleAndNotWithGivenIds(roleType, userIds, predicate, pageable) the input user ids must not be null.");
        return userRepository.findAllByRoleAndNotWithIds(predicate, pageable, roleType, userIds);
    }

    @Override
    public Page<User> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        Assert.notNull(groupId, "In method getAllUsersNotInGivenGroup(groupId, predicate, pageable) the input group id must not be null.");
        return userRepository.usersNotInGivenGroup(groupId, predicate, pageable);
    }

    @Override
    public Page<User> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
        Assert.notNull(groupsIds, "In method getUsersInGroups(groupsIds, predicate, pageable) the input groups ids must not be null.");
        return userRepository.usersInGivenGroups(groupsIds, predicate, pageable);
    }

    @Override
    public User getUserWithGroups(Long id) {
        Assert.notNull(id, "In method getUserWithGroups(id) the input must not be null.");
        return userRepository.getUserByIdWithGroups(id)
                .orElseThrow(() -> new UserAndGroupServiceException("User with id " + id + " not found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public User getUserWithGroups(String login, String iss) {
        Assert.hasLength(login, "In method getUserWithGroups(login, iss) the input login must not be empty.");
        Assert.hasLength(iss, "In method getUserWithGroups(login, iss) the input iss must not be empty.");
        return userRepository.getUserByLoginWithGroups(login, iss)
                .orElseThrow(() -> new UserAndGroupServiceException("User with login " + login + " not found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Set<Role> getRolesOfUser(Long id) {
        Assert.notNull(id, "In method getRolesOfUser(id) the input must not be null.");
        if (!userRepository.existsById(id)) {
            throw new UserAndGroupServiceException("User with id " + id + " could not be found.", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return userRepository.getRolesOfUser(id);
    }

    @Override
    public Page<User> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
        Assert.notNull(roleId, "In method getUsersWithGivenRole(roleId, predicate, pageable) the input ids must not be null.");
        if (!roleRepository.existsById(roleId)) {
            throw new UserAndGroupServiceException("Role with id: " + roleId + " could not be found.", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return userRepository.findAllByRoleId(roleId, predicate, pageable);
    }

    @Override
    public Page<User> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable) {
        Assert.notNull(roleType, "In method getUsersWithGivenRoleType(roleType, predicate, pageable) the input role type must not be null.");
        if (!roleRepository.existsByRoleType(roleType)) {
            throw new UserAndGroupServiceException("Role with type: " + roleType + " could not be found.", ErrorCode.RESOURCE_NOT_FOUND);
        }
        return userRepository.findAllByRoleType(roleType, predicate, pageable);
    }

    @Override
    public Page<User> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate) {
        Assert.notNull(ids, "In method getUsersWithGivenIds(ids, pageable, predicate) the input ids must not be null.");
        Predicate finalPredicate = QUser.user.id.in(ids).and(predicate);
        return userRepository.findAll(finalPredicate, pageable);
    }

}
