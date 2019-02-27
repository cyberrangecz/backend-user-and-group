package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
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

@Service
public class UserServiceImpl implements UserService {

    private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class.getName());

    private UserRepository userRepository;

    private IDMGroupRepository groupRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, IDMGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public User get(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        Optional<User> optionalUser = userRepository.findById(id);
        User user = optionalUser.orElseThrow(() -> new UserAndGroupServiceException("User with id " + id + " not found"));
        log.info(user + " loaded.");
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public UserDeletionStatusDTO delete(User user) {
        Assert.notNull(user, "Input user must not be null");
        UserDeletionStatusDTO deletionCheck = checkKypoUserBeforeDelete(user);
        StringBuilder message = new StringBuilder();

        if (deletionCheck.equals(UserDeletionStatusDTO.SUCCESS)) {
            userRepository.delete(user);
            message.append("IDM user with id: ").append(user.getId()).append(" was successfully deleted.");
            log.info(message.toString());
        }
        return deletionCheck;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Map<User, UserDeletionStatusDTO> deleteUsers(List<Long> idsOfUsers) {
        Assert.notNull(idsOfUsers, "Input ids of users must not be null");
        Map<User, UserDeletionStatusDTO> response = new HashMap<>();

        idsOfUsers.forEach(id -> {
            User u = null;
            try {
                u = get(id);
                try {
                    UserDeletionStatusDTO status = delete(u);
                    response.put(u, status);
                } catch (Exception ex) {
                    response.put(u, UserDeletionStatusDTO.ERROR);
                }
            } catch (UserAndGroupServiceException ex) {
                u = new User();
                u.setId(id);
                response.put(u, UserDeletionStatusDTO.NOT_FOUND);
            }
        });

        return response;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public void changeAdminRole(Long id) throws UserAndGroupServiceException {
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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public boolean isUserAdmin(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup.orElseThrow(() -> new UserAndGroupServiceException("Administrator group could not be found."));

        return user.getGroups().contains(administratorGroup);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameLogin(#login)")
    public User getUserByLogin(String login) throws UserAndGroupServiceException {
        Assert.hasLength(login, "Input login must not be empty");
        Optional<User> optionalUser = userRepository.findByLogin(login);
        User user = optionalUser.orElseThrow(() -> new UserAndGroupServiceException("User with login " + login + " could not be found"));
        log.info(user + " loaded.");
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Page<User> getAllUsers(Predicate predicate, Pageable pageable) {
        Page<User> users = userRepository.findAll(predicate, pageable);
        log.info("All Users loaded");
        return users;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Page<User> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable) {
        Page<User> usersNotInGivenGroup = userRepository.usersNotInGivenGroup(groupId, pageable);
        log.info("All users who are not in group with id {} was loaded", groupId);
        return usersNotInGivenGroup;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public User getUserWithGroups(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        user.getGroups().size();
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameLogin(#login)")
    public User getUserWithGroups(String login) throws UserAndGroupServiceException {
        Assert.hasLength(login, "Input login must not be empty");
        User user = this.getUserByLogin(login);
        user.getGroups().size();
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public boolean isUserInternal(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        if (!userRepository.existsById(id)) {
            throw new UserAndGroupServiceException("User with id " + id + " could not be found.");
        }
        return userRepository.isUserInternal(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public Set<Role> getRolesOfUser(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        if (!userRepository.existsById(id)) {
            throw new UserAndGroupServiceException("User with id " + id + " could not be found.");
        }
        return userRepository.getRolesOfUser(id);
    }

    @Override
    public Page<User> getUsersInGroups(Set<Long> groupsIds, Pageable pageable) {
        Page<User> users = userRepository.usersInGivenGroups(groupsIds, pageable);
        log.info("All Users in given groups loaded");
        return users;
    }

    private UserDeletionStatusDTO checkKypoUserBeforeDelete(User user) {
        if (user.getExternalId() != null && user.getStatus().equals(UserAndGroupStatus.VALID)) {
            return UserDeletionStatusDTO.EXTERNAL_VALID;
        }
        return UserDeletionStatusDTO.SUCCESS;
    }
}
