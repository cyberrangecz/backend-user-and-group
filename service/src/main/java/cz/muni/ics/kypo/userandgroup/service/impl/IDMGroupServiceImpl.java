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

import cz.muni.ics.kypo.userandgroup.dbmodel.*;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.persistence.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.persistence.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import cz.muni.ics.kypo.userandgroup.persistence.IDMGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final RoleRepository roleRepository;
    private final MicroserviceRepository microserviceRepository;

    @Autowired
    public IDMGroupServiceImpl(IDMGroupRepository idmGroupRepository, RoleRepository roleRepository,
                               MicroserviceRepository microserviceRepository) {
        this.groupRepository = idmGroupRepository;
        this.roleRepository = roleRepository;
        this.microserviceRepository = microserviceRepository;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup get(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        Optional<IDMGroup> optionalGroup = groupRepository.findById(id);
        IDMGroup group = optionalGroup.orElseThrow(() -> new IdentityManagementException("IDM group with id " + id + " not found"));
        log.info(group + " loaded.");
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public IDMGroup create(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        group.setStatus(UserAndGroupStatus.VALID);
        IDMGroup g = groupRepository.save(group);
        log.info(group + " created.");
        return g;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public IDMGroup update(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        IDMGroup g = groupRepository.save(group);
        log.info(group + " updated.");
        return g;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
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
            } catch (IdentityManagementException ex) {
                g = new IDMGroup();
                g.setId(id);
                response.put(g, GroupDeletionStatus.NOT_FOUND);
            }
        });

        return response;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public List<IDMGroup> getAllIDMGroups() {
        List<IDMGroup> groups = groupRepository.findAll();
        log.info("All IDM Groups loaded");
        return groups;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupByName(String name) throws IdentityManagementException {
        Assert.hasLength(name, "Input name of group must not be empty");
        Optional<IDMGroup> optionalGroup = groupRepository.findByName(name);
        IDMGroup group = optionalGroup.orElseThrow(() -> new IdentityManagementException("IDM Group with name " + name + " not found"));
        log.info(group + " loaded.");
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public List<IDMGroup> getIDMGroupsByName(String name) throws IdentityManagementException {
        Assert.hasLength(name, "Input name of group must not be empty");
        List<IDMGroup> groups = groupRepository.findAllByName(name);
        if (groups != null && !groups.isEmpty()) {
            log.info(groups.toString() + " loaded.");
        } else {
            log.error("IDM Groups with name containing " + name + " not empty");
            throw new IdentityManagementException("IDM Groups with name containing " + name + " not found");
        }
        return groups;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup getIDMGroupWithUsers(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        IDMGroup group = get(id);
        group.getUsers().size();
        return group;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupWithUsers(String name) throws IdentityManagementException {
        Assert.hasLength(name, "Input name of group must not be empty");
        List<IDMGroup> groups = getIDMGroupsByName(name);
        IDMGroup g = groups.get(0);
        g.getUsers().size();
        return g;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public boolean isGroupInternal(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return get(id).getExternalId() == null;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public Set<Role> getRolesOfGroup(Long id) {
        Assert.notNull(id, "Input id must not be null");

        RestTemplate restTemplate = new RestTemplate();

        return groupRepository.getRolesOfGroup(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public IDMGroup assignRole(Long groupId, RoleType roleType) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleType, "Input roleType must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IdentityManagementException("Group with " + groupId + " could not be found."));

        switch (roleType) {
            case ADMINISTRATOR:
                Role adminRole = roleRepository.findByRoleType(RoleType.ADMINISTRATOR.name())
                        .orElseThrow(() ->
                                new IdentityManagementException(RoleType.ADMINISTRATOR + " role could not be found. Start up of the project probably went wrong, please contact support."));
                log.info("ADMINISTRATOR");
                group.addRole(adminRole);
            case USER:
                Role userRole = roleRepository.findByRoleType(RoleType.USER.name())
                        .orElseThrow(() ->
                                new IdentityManagementException(RoleType.USER + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(userRole);
                log.info("USER");
            case GUEST:
                Role guestRole = roleRepository.findByRoleType(RoleType.GUEST.name())
                        .orElseThrow(() ->
                                new IdentityManagementException(RoleType.GUEST + " role could not be found. Start up of the project probably went wrong, please contact support."));
                group.addRole(guestRole);
                log.info("GUEST");
        }
        IDMGroup updatedGroup = groupRepository.save(group);
        updatedGroup.setRoles(getRolesOfGroup(updatedGroup.getId()));
        return updatedGroup;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public IDMGroup assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        Assert.notNull(microserviceId, "Input microserviceId must not be null");

        Microservice microservice = microserviceRepository.findById(microserviceId)
                .orElseThrow(() ->
                        new IdentityManagementException("Microservice with id " + microserviceId + " could not be found."));
        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new IdentityManagementException("Group with id " + groupId + " could not be found."));

        final String uri = microservice.getEndpoint() + "/{roleId}/assign/to/{groupId}";

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.put(uri, null, roleId, groupId);
        group.setRoles(getRolesOfGroup(group.getId()));

        return group;
    }

    private GroupDeletionStatus checkKypoGroupBeforeDelete(IDMGroup group) {
        if (group.getExternalId() != null && group.getStatus().equals(UserAndGroupStatus.VALID)) {
            return GroupDeletionStatus.EXTERNAL_VALID;
        }
        return GroupDeletionStatus.SUCCESS;
    }
}
