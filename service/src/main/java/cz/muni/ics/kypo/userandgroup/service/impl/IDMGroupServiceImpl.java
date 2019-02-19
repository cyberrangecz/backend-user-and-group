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
import cz.muni.ics.kypo.userandgroup.exception.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.exception.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
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
public class IDMGroupServiceImpl implements IDMGroupService {

    private static Logger log = LoggerFactory.getLogger(IDMGroupServiceImpl.class.getName());

    private final IDMGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public IDMGroupServiceImpl(IDMGroupRepository idmGroupRepository, RoleRepository roleRepository,
                               UserRepository userRepository) {
        this.groupRepository = idmGroupRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup get(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        Optional<IDMGroup> optionalGroup = groupRepository.findById(id);
        IDMGroup group = optionalGroup.orElseThrow(() -> new UserAndGroupServiceException("IDM group with id " + id + " not found"));
        log.info(group + " loaded.");
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup create(IDMGroup group, List<Long> groupIdsOfImportedMembers) throws UserAndGroupServiceException {
        Assert.notNull(group, "Input group must not be null.");
        group.setStatus(UserAndGroupStatus.VALID);

        groupIdsOfImportedMembers.forEach(groupId -> {
            IDMGroup gr = groupRepository.findById(groupId)
                    .orElseThrow(() -> new UserAndGroupServiceException("Group with id " + groupId + " counld not be found"));
            gr.getUsers().forEach(user -> {
                if (!group.getUsers().contains(user)) {
                    group.addUser(user);
                }
            });
        });

        Role guestRole = roleRepository.findByRoleType(RoleType.GUEST)
                .orElseThrow(() ->
                        new UserAndGroupServiceException(RoleType.GUEST + " role could not be found. Start up of the project probably went wrong, please contact support."));
        group.addRole(guestRole);

        IDMGroup g = groupRepository.save(group);
        log.info(group + " created.");
        return g;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup update(IDMGroup group) throws ExternalSourceException {
        Assert.notNull(group, "Input group must not be null.");

        if (groupRepository.isIDMGroupInternal(group.getId())) {
            IDMGroup groupInDatabase = get(group.getId());
            groupInDatabase.setDescription(group.getDescription());
            groupInDatabase.setName(group.getName());
            IDMGroup g = groupRepository.save(groupInDatabase);
            log.info(group + " updated.");
            return g;
        } else {
            log.error("Given idm group is external therefore it cannot be updated");
            throw new ExternalSourceException("Given idm group is external therefore it cannot be udpated");
        }
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public GroupDeletionStatus delete(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        GroupDeletionStatus deletionStatus = checkKypoGroupBeforeDelete(group);
        if (deletionStatus.equals(GroupDeletionStatus.SUCCESS)) {
            groupRepository.delete(group);
            log.info(group + " deleted.");
        }
        return deletionStatus;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable) {
        Page<IDMGroup> groups = groupRepository.findAll(predicate, pageable);
        log.info("All IDM Groups loaded");
        return groups;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupByName(String name) throws UserAndGroupServiceException {
        Assert.hasLength(name, "Input name of group must not be empty");
        Optional<IDMGroup> optionalGroup = groupRepository.findByName(name);
        IDMGroup group = optionalGroup.orElseThrow(() -> new UserAndGroupServiceException("IDM Group with name " + name + " not found"));
        log.info(group + " loaded.");
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public Page<IDMGroup> getIDMGroupsByName(String name, Pageable pageable) throws UserAndGroupServiceException {
        Assert.hasLength(name, "Input name of group must not be empty");
        Page<IDMGroup> groups = groupRepository.findAllByName(name, pageable);
        if (groups != null && groups.getTotalElements() != 0) {
            log.info(groups.toString() + " loaded.");
        } else {
            log.error("IDM Groups with name containing " + name + " not empty");
            throw new UserAndGroupServiceException("IDM Groups with name containing " + name + " not found");
        }
        return groups;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup getIDMGroupWithUsers(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        IDMGroup group = get(id);
        group.getUsers().size();
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupWithUsers(String name) throws UserAndGroupServiceException {
        Assert.hasLength(name, "Input name of group must not be empty");
        IDMGroup group = getIDMGroupByName(name);
        group.getUsers().size();
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public boolean isGroupInternal(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        return groupRepository.isIDMGroupInternal(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public Set<Role> getRolesOfGroup(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        if (!groupRepository.existsById(id)) {
            throw new UserAndGroupServiceException("Group with id " + id + " could not be found.");
        }
        Set<Role> roles = new HashSet<>(groupRepository.getRolesOfGroup(id));
        return roles;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup assignRole(Long groupId, RoleType roleType) throws UserAndGroupServiceException {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleType, "Input roleType must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with " + groupId + " could not be found."));

        switch (roleType) {
            case ADMINISTRATOR:
                Role adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR)
                        .orElseThrow(() ->
                                new UserAndGroupServiceException(RoleType.ADMINISTRATOR + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(adminRole);
            case USER:
                Role userRole = roleRepository.findByRoleType(RoleType.USER)
                        .orElseThrow(() ->
                                new UserAndGroupServiceException(RoleType.USER + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(userRole);
            case GUEST:
                Role guestRole = roleRepository.findByRoleType(RoleType.GUEST)
                        .orElseThrow(() ->
                                new UserAndGroupServiceException(RoleType.GUEST + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(guestRole);
        }
        return groupRepository.save(group);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup removeRoleToGroup(Long groupId, RoleType roleType) throws UserAndGroupServiceException, RoleCannotBeRemovedToGroupException {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleType, "Input roleType must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with " + groupId + " could not be found."));

        if (roleType.equals(RoleType.ADMINISTRATOR)) {
            Role adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR)
                    .orElseThrow(() ->
                            new UserAndGroupServiceException(RoleType.ADMINISTRATOR + " role could not be found. Start up of the project probably went wrong, please contact support."));
            group.removeRole(adminRole);
        } else {
            throw new RoleCannotBeRemovedToGroupException("Roles USER and GUEST cannot be removed from group. These roles are main roles to give access to KYPO and if you" +
                    "want to remove them from group you have to remove the group.");
        }
        return groupRepository.save(group);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup removeUsers(Long groupId, List<Long> userIds) throws UserAndGroupServiceException, ExternalSourceException {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(userIds, "Input list of users ids must not be null");

        IDMGroup groupToUpdate = this.get(groupId);
        if (!groupRepository.isIDMGroupInternal(groupId)) {
            throw new ExternalSourceException("Group is external therefore it could not be updated");
        }
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserAndGroupServiceException("User with id " + userId + " could not be found"));
            groupToUpdate.removeUser(user);
        }
        return groupRepository.save(groupToUpdate);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup addUsers(Long groupId, List<Long> idsOfGroupsOfImportedUsers, List<Long> idsOfUsersToBeAdd) throws UserAndGroupServiceException, ExternalSourceException {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(idsOfGroupsOfImportedUsers, "Input list of groups ids must not be null");
        Assert.notNull(idsOfUsersToBeAdd, "Input list of users ids must not be null");

        IDMGroup groupToUpdate = this.get(groupId);
        if (!groupRepository.isIDMGroupInternal(groupId)) {
            throw new ExternalSourceException("Group is external therefore it could not be updated");
        }
        for (Long userId : idsOfUsersToBeAdd) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserAndGroupServiceException("User with id " + userId + " could not be found"));
            groupToUpdate.addUser(user);
        }
        for (Long id : idsOfGroupsOfImportedUsers) {
            IDMGroup groupOfImportedMembers = this.get(id);
            groupOfImportedMembers.getUsers().forEach((u) -> {
                if (!groupToUpdate.getUsers().contains(u)) {
                    groupToUpdate.addUser(u);
                }
            });
        }

        return groupRepository.save(groupToUpdate);
    }

    private GroupDeletionStatus checkKypoGroupBeforeDelete(IDMGroup group) {
        if (!groupRepository.isIDMGroupInternal(group.getId()) && group.getStatus().equals(UserAndGroupStatus.VALID)) {
            return GroupDeletionStatus.EXTERNAL_VALID;
        } else if (group.getName().equals(RoleType.ADMINISTRATOR.name()) ||
                group.getName().equals(RoleType.USER.name()) ||
                group.getName().equals(RoleType.GUEST.name())) {
            return GroupDeletionStatus.ERROR_MAIN_GROUP;
        }
        return GroupDeletionStatus.SUCCESS;
    }
}
