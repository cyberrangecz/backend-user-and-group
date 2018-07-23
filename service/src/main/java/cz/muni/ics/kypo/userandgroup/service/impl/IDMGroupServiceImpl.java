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
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
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
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class IDMGroupServiceImpl implements IDMGroupService {

    private static Logger log = LoggerFactory.getLogger(IDMGroupServiceImpl.class.getName());

    private final IDMGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MicroserviceRepository microserviceRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public IDMGroupServiceImpl(IDMGroupRepository idmGroupRepository, RoleRepository roleRepository,
                               MicroserviceRepository microserviceRepository, RestTemplate restTemplate,
                               UserRepository userRepository) {
        this.groupRepository = idmGroupRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.microserviceRepository = microserviceRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup get(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        Optional<IDMGroup> optionalGroup = groupRepository.findById(id);
        IDMGroup group = optionalGroup.orElseThrow(() -> new UserAndGroupServiceException("IDM group with id " + id + " not found"));
        group.setRoles(getRolesOfGroup(group.getId()));
        log.info(group + " loaded.");
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup create(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        group.setStatus(UserAndGroupStatus.VALID);
        IDMGroup g = groupRepository.save(group);
        log.info(group + " created.");
        return g;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup update(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        IDMGroup g = groupRepository.save(group);
        g.setRoles(getRolesOfGroup(g.getId()));
        log.info(group + " updated.");
        return g;
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
    public Map<IDMGroup, GroupDeletionStatus> deleteGroups(List<Long> idsOfGroups) {
        Assert.notNull(idsOfGroups, "Input ids of groups must not be null");

        Map<IDMGroup, GroupDeletionStatus> response = new HashMap<>();

        idsOfGroups.forEach(id -> {
            IDMGroup g = null;
            try {
                g = get(id);

                try {
                    GroupDeletionStatus status = delete(g);
                    response.put(g, status);
                } catch (Exception ex) {
                    response.put(g, GroupDeletionStatus.ERROR);
                }
            } catch (UserAndGroupServiceException ex) {
                g = new IDMGroup();
                g.setId(id);
                response.put(g, GroupDeletionStatus.NOT_FOUND);
            }
        });

        return response;
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
        group.setRoles(getRolesOfGroup(group.getId()));
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
    public boolean isGroupInternal(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return get(id).getExternalId() == null;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public Set<Role> getRolesOfGroup(Long id) {
        Assert.notNull(id, "Input id must not be null");

        Set<Role> roles = new HashSet<>(groupRepository.getRolesOfGroup(id));

        List<Microservice> microservices = microserviceRepository.findAll();
        for (Microservice microservice : microservices) {
            String uri = microservice.getEndpoint() + "/of/{groupId}";

            ResponseEntity<Role[]> responseEntity = restTemplate.getForEntity(uri, Role[].class, id);
            roles.addAll(Arrays.asList(responseEntity.getBody()));
        }

        return roles;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup assignRole(Long groupId, RoleType roleType) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleType, "Input roleType must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with " + groupId + " could not be found."));

        switch (roleType) {
            case ADMINISTRATOR:
                Role adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR.name())
                        .orElseThrow(() ->
                                new UserAndGroupServiceException(RoleType.ADMINISTRATOR + " role could not be found. Start up of the project probably went wrong, please contact support."));
                log.info("ADMINISTRATOR");
                group.addRole(adminRole);
            case USER:
                Role userRole = roleRepository.findByRoleType(RoleType.USER.name())
                        .orElseThrow(() ->
                                new UserAndGroupServiceException(RoleType.USER + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(userRole);
                log.info("USER");
            case GUEST:
                Role guestRole = roleRepository.findByRoleType(RoleType.GUEST.name())
                        .orElseThrow(() ->
                                new UserAndGroupServiceException(RoleType.GUEST + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(guestRole);
                log.info("GUEST");
        }
        IDMGroup updatedGroup = groupRepository.save(group);
        updatedGroup.setRoles(getRolesOfGroup(updatedGroup.getId()));
        return updatedGroup;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        Assert.notNull(microserviceId, "Input microserviceId must not be null");

        Microservice microservice = microserviceRepository.findById(microserviceId)
                .orElseThrow(() ->
                        new UserAndGroupServiceException("Microservice with id " + microserviceId + " could not be found."));
        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new UserAndGroupServiceException("Group with id " + groupId + " could not be found."));

        final String uri = microservice.getEndpoint() + "/{roleId}/assign/to/{groupId}";

        restTemplate.put(uri, null, roleId, groupId);
        group.setRoles(getRolesOfGroup(group.getId()));

        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup removeMembers(Long groupId, List<Long> userIds) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(userIds, "Input list of users ids must not be null");

        if (!this.isGroupInternal(groupId)) {
            throw new UserAndGroupServiceException("Group is external therefore they could not be updated");
        }

        IDMGroup groupToUpdate = this.get(groupId);
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserAndGroupServiceException("User with id " + userId + " could not be found"));
            groupToUpdate.addUser(user);
        }
        return this.update(groupToUpdate);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public IDMGroup addMembers(Long groupId, List<Long> idsOfGroupsOfImportedUsers, List<Long> idsOfUsersToBeAdd) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(idsOfGroupsOfImportedUsers, "Input list of groups ids must not be null");
        Assert.notNull(idsOfUsersToBeAdd, "Input list of users ids must not be null");

        if (!this.isGroupInternal(groupId)) {
            throw new UserAndGroupServiceException("Group is external therefore they could not be updated");
        }
        IDMGroup groupToUpdate = this.get(groupId);
        for (Long userId : idsOfUsersToBeAdd) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserAndGroupServiceException("User with id " + userId + " could not be found"));
            groupToUpdate.addUser(user);
        }
        for (Long id : idsOfGroupsOfImportedUsers) {
            IDMGroup groupOfImportedMembers = this.get(groupId);
            groupOfImportedMembers.getUsers().forEach((u) -> {
                if (!groupToUpdate.getUsers().contains(u)) {
                    groupToUpdate.addUser(u);
                }
            });
        }

        return this.update(groupToUpdate);
    }

    private GroupDeletionStatus checkKypoGroupBeforeDelete(IDMGroup group) {
        if (group.getExternalId() != null && group.getStatus().equals(UserAndGroupStatus.VALID)) {
            return GroupDeletionStatus.EXTERNAL_VALID;
        }
        return GroupDeletionStatus.SUCCESS;
    }

    private void addMembersFromGroups(List<Long> groupIds, IDMGroup group) {

    }
}
