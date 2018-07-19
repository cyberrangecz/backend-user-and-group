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

import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
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
    public User get(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        Optional<User> optionalUser = userRepository.findById(id);
        User user = optionalUser.orElseThrow(() -> new IdentityManagementException("User with id " + id + " not found"));
        log.info(user + " loaded.");
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public User create(User user) {
        Assert.notNull(user, "Input user must not be null");
        Assert.hasLength(user.getScreenName(), "Screen name of input user must not be empty");
        User u = userRepository.save(user);
        log.info(user + " was created.");
        return u;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)")
    public User update(User updatedUser) throws IdentityManagementException {
        Assert.notNull(updatedUser, "Input user must not be null");
        Assert.hasLength(updatedUser.getScreenName(), "Screen name of input user must not be empty");
        if (!isUserInternal(updatedUser.getId())) {
            log.error("External user cannot be updated.");
            throw new IdentityManagementException("Error: External user cannot be updated");
        }
        User u = userRepository.save(updatedUser);
        log.info(updatedUser + " was updated in IDM databases.");
        return u;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)")
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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)")
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
            } catch (IdentityManagementException ex) {
                u = new User();
                u.setId(id);
                response.put(u, UserDeletionStatus.NOT_FOUND);
            }
        });

        return response;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)")
    public void changeAdminRole(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup.orElseThrow(() -> new IdentityManagementException("Administrator group could not be  found."));
        if (user.getGroups().contains(administratorGroup)) {
            user.removeGroup(administratorGroup);
        } else {
            user.addGroup(administratorGroup);
        }
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public boolean isUserAdmin(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        Optional<IDMGroup> optionalAdministratorGroup = groupRepository.findAdministratorGroup();
        IDMGroup administratorGroup = optionalAdministratorGroup.orElseThrow(() -> new IdentityManagementException("Administrator group could not be found."));

        return user.getGroups().contains(administratorGroup);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameScreenName(#screenName)")
    public User getUserByScreenName(String screenName) throws IdentityManagementException {
        Assert.hasLength(screenName, "Input screen name must not be empty");
        Optional<User> optionalUser = userRepository.findByScreenName(screenName);
        User user = optionalUser.orElseThrow(() -> new IdentityManagementException("User with screen name " + screenName + " could not be found"));
        log.info(user + " loaded.");
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Page<User> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        log.info("All Users loaded");
        return users;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public User getUserWithGroups(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        User user = get(id);
        user.getGroups().size();
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameScreenName(#screenName)")
    public User getUserWithGroups(String screenName) throws IdentityManagementException {
        Assert.hasLength(screenName, "Input screen name must not be empty");
        User user = getUserByScreenName(screenName);
        user.getGroups().size();
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.dbmodel.core.model.RoleType).ADMINISTRATOR)" +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public boolean isUserInternal(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return userRepository.isUserInternal(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.hasLoggedInUserSameId(#id)")
    public Set<Role> getRolesOfUser(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return userRepository.getRolesOfUser(id);
    }

    private UserDeletionStatus checkKypoUserBeforeDelete(User user) {
        if (user.getExternalId() != null && user.getStatus().equals(UserAndGroupStatus.VALID)) {
            return UserDeletionStatus.EXTERNAL_VALID;
        }

        return UserDeletionStatus.SUCCESS;
    }
}
