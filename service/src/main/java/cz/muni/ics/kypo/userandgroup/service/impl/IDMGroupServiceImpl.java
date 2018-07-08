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

import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import cz.muni.ics.kypo.userandgroup.persistence.IDMGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IDMGroupServiceImpl implements IDMGroupService {

    private static Logger log = LoggerFactory.getLogger(IDMGroupServiceImpl.class.getName());

    private final IDMGroupRepository groupRepository;

    @Autowired
    public IDMGroupServiceImpl(IDMGroupRepository idmGroupRepository) {
        this.groupRepository = idmGroupRepository;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup get(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        try {
            IDMGroup group = groupRepository.getOne(id);
            log.info(group + " loaded.");
            return group;
        } catch (EntityNotFoundException ex) {
            log.error("IDM group with id " + id + " not found");
            throw new IdentityManagementException("IDM group with id " + id + " not found");
        }
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public IDMGroup create(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        IDMGroup g = groupRepository.save(group);
        log.info(group + " created.");
        return g;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public IDMGroup update(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        IDMGroup g = groupRepository.save(group);
        log.info(group + " updated.");
        return g;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
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
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public Map<IDMGroup, GroupDeletionStatus> deleteGroups(List<Long> idsOfGroups) {
        Assert.notNull(idsOfGroups, "Input ids of groups must not be null");

        Map<IDMGroup, GroupDeletionStatus> response = new HashMap<>();

        idsOfGroups.forEach( id -> {
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
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public List<IDMGroup> getAllIDMGroups() {
        List<IDMGroup> groups = groupRepository.findAll();
        log.info("All IDM Groups loaded");
        return groups;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupByName(String name) throws IdentityManagementException {
        Assert.hasLength(name, "Input name of group must not be empty");
        IDMGroup group = groupRepository.findByName(name);
        if (group != null) {
            log.info(group + " loaded.");
        } else {
            log.error("IDM Group with name " + name + " not found");
            throw new IdentityManagementException("IDM Group with name " + name + " not found");
        }
        return group;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
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
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup getIDMGroupWithUsers(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        IDMGroup group = get(id);
        group.getUsers().size();
        return group;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupWithUsers(String name) throws IdentityManagementException {
        Assert.hasLength(name, "Input name of group must not be empty");
        List<IDMGroup> groups = getIDMGroupsByName(name);
        IDMGroup g = groups.get(0);
        g.getUsers().size();
        return g;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public boolean isGroupInternal(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return get(id).getExternalId() == null;
    }

    @Override
    @PreAuthorize("hasRole(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public Set<Role> getRolesOfGroup(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return groupRepository.getRolesOfGroup(id);
    }

    private GroupDeletionStatus checkKypoGroupBeforeDelete(IDMGroup group) {
        if (group.getExternalId() != null && group.getStatus().equals(UserAndGroupStatus.VALID)) {
            return GroupDeletionStatus.EXTERNAL_VALID;
        }
        return GroupDeletionStatus.SUCCESS;
    }
}
