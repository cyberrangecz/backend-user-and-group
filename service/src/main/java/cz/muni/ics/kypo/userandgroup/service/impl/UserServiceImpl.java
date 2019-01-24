/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz
 *
 *  Date      : 31.5.2016
 *
 *  (c) Copyright 2016 MASARYK UNIVERSITY
 *  All rights reserved.
 *
 *  This software is freely available for non-commercial use under license
 *  specified in following license agreement in LICENSE file. Please review the terms
 *  of the license agreement before using this software. If you are interested in
 *  using this software commercially orin ways not allowed in aforementioned
 *  license, feel free to contact Technology transfer office of the Masaryk university
 *  in order to negotiate ad-hoc license agreement.
 */
package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
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
    public UserDeletionStatus delete(User user) {
        Assert.notNull(user, "Input user must not be null");
        UserDeletionStatus deletionCheck = checkKypoUserBeforeDelete(user);
        StringBuilder message = new StringBuilder();

        if (deletionCheck.equals(UserDeletionStatus.SUCCESS)) {
            userRepository.delete(user);
            message.append("IDM user with id: ").append(user.getId()).append(" was successfully deleted.");
            log.info(message.toString());
        }
        return deletionCheck;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Map<User, UserDeletionStatus> deleteUsers(List<Long> idsOfUsers) {
        Assert.notNull(idsOfUsers, "Input ids of users must not be null");
        Map<User, UserDeletionStatus> response = new HashMap<>();

        idsOfUsers.forEach(id -> {
            User u = null;
            try {
                u = get(id);
                try {
                    UserDeletionStatus status = delete(u);
                    response.put(u, status);
                } catch (Exception ex) {
                    response.put(u, UserDeletionStatus.ERROR);
                }
            } catch (UserAndGroupServiceException ex) {
                u = new User();
                u.setId(id);
                response.put(u, UserDeletionStatus.NOT_FOUND);
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

    private UserDeletionStatus checkKypoUserBeforeDelete(User user) {
        if (user.getExternalId() != null && user.getStatus().equals(UserAndGroupStatus.VALID)) {
            return UserDeletionStatus.EXTERNAL_VALID;
        }

        return UserDeletionStatus.SUCCESS;
    }
}
