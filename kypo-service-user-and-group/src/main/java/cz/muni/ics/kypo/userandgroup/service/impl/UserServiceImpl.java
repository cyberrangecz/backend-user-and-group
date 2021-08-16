package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.QUser;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
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
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(User.class, "id", id.getClass(), id)));
    }

    @Override
    public Optional<User> getUserBySubAndIss(String sub, String iss) {
        return userRepository.findBySubAndIss(sub, iss);
    }

    @Override
    public Page<User> getAllUsers(Predicate predicate, Pageable pageable) {
        return userRepository.findAll(predicate, pageable);
    }

    @Override
    public Page<User> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate) {
        Predicate finalPredicate = QUser.user.id.in(ids).and(predicate);
        return userRepository.findAll(finalPredicate, pageable);
    }

    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findByIdIn(userIds);
    }

    @Override
    public void deleteUser(User user) {
        for (IDMGroup group : user.getGroups()) {
            group.removeUser(user);
        }
        userRepository.delete(user);
        LOG.debug("IDM user with id: {} was successfully deleted.", user.getId());
    }

    @Override
    public User createUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public void changeAdminRole(Long id) {
        User user = this.getUserById(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "name", String.class,
                        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())));
        if (user.getGroups().contains(administratorGroup)) {
            administratorGroup.removeUser(user);
        } else {
            administratorGroup.addUser(user);
        }
        userRepository.save(user);
    }

    @Override
    public boolean isUserAdmin(Long id) {
        User user = getUserById(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "name", String.class,
                        ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName())));
        return user.getGroups().contains(administratorGroup);
    }

    @Override
    public Page<User> getUsersWithGivenRoleAndNotWithGivenIds(String roleType, Set<Long> userIds, Predicate predicate, Pageable pageable) {
        return userRepository.findAllByRoleAndNotWithIds(predicate, pageable, roleType, userIds);
    }

    @Override
    public Page<User> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        return userRepository.usersNotInGivenGroup(groupId, predicate, pageable);
    }

    @Override
    public Page<User> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
        return userRepository.usersInGivenGroups(groupsIds, predicate, pageable);
    }

    @Override
    public User getUserWithGroups(Long id) {
        return userRepository.getUserByIdWithGroups(id)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(User.class, "id", id.getClass(), id)));
    }

    @Override
    public User getUserWithGroups(String sub, String iss) {
        return userRepository.getUserBySubWithGroups(sub, iss)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(User.class, "sub", sub.getClass(), sub)));
    }

    @Override
    public Set<Role> getRolesOfUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(new EntityErrorDetail(User.class, "id", id.getClass(), id));
        }
        return userRepository.getRolesOfUser(id);
    }

    @Override
    public Page<Role> getRolesOfUserWithPagination(Long id, Pageable pageable, Predicate predicate) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(new EntityErrorDetail(User.class, "id", id.getClass(), id));
        }
        return roleRepository.findAllOfUser(id, pageable, predicate);
    }

    @Override
    public Page<User> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
        if (!roleRepository.existsById(roleId)) {
            throw new EntityNotFoundException(new EntityErrorDetail(Role.class, "roleId", roleId.getClass(), roleId));
        }
        return userRepository.findAllByRoleId(roleId, predicate, pageable);
    }

    @Override
    public Page<User> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable) {
        if (!roleRepository.existsByRoleType(roleType)) {
            throw new EntityNotFoundException(new EntityErrorDetail(Role.class, "roleType", roleType.getClass(), roleType));
        }
        return userRepository.findAllByRoleType(roleType, predicate, pageable);
    }

}
