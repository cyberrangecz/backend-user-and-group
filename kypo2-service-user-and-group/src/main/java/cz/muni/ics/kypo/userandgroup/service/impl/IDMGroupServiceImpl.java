package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Service
public class IDMGroupServiceImpl implements IDMGroupService {

    private static Logger LOG = LoggerFactory.getLogger(IDMGroupServiceImpl.class.getName());

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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup get(Long id) {
        LOG.debug("get({})", id);
        Assert.notNull(id, "Input id must not be null");
        return groupRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("IDM group with id " + id + " not found"));
    }

    @Override
    public IDMGroup getGroupForDefaultRoles() {
        LOG.debug("getGroupForDefaultRoles()");
        return groupRepository.findByName("DEFAULT_GROUP").orElseThrow(() -> new UserAndGroupServiceException("IDM group for default roles not found"));
    }

    @Override
    @IsAdmin
    public IDMGroup create(IDMGroup group, List<Long> groupIdsOfImportedMembers) {
        LOG.debug("create({}, {})", group, groupIdsOfImportedMembers);
        Assert.notNull(group, "Input group must not be null.");
        group.setStatus(UserAndGroupStatus.VALID);

        groupIdsOfImportedMembers.forEach(groupId -> {
            IDMGroup idmGroup = groupRepository.findById(groupId)
                    .orElseThrow(() -> new UserAndGroupServiceException("Group with id " + groupId + " counld not be found"));
            idmGroup.getUsers().forEach(user -> {
                if (!group.getUsers().contains(user)) {
                    group.addUser(user);
                }
            });
        });
        return groupRepository.save(group);
    }

    @Override
    @IsAdmin
    public IDMGroup update(IDMGroup group) {
        LOG.debug("update({})", group);
        Assert.notNull(group, "Input group must not be null.");
        if (groupRepository.isIDMGroupInternal(group.getId())) {
            IDMGroup groupInDatabase = get(group.getId());
            if(List.of("DEFAULT-GROUP", "USER-AND-GROUP_ADMINISTRATOR", "USER-AND-GROUP_USER")
                    .contains(groupInDatabase.getName()) && !groupInDatabase.getName().equals(group.getName())) {
                throw new UserAndGroupServiceException("Cannot change name of main group " + groupInDatabase.getName() +  " to " + group.getName() + ".");
            }
            groupInDatabase.setDescription(group.getDescription());
            groupInDatabase.setName(group.getName());
            return groupRepository.save(groupInDatabase);
        } else {
            throw new ExternalSourceException("Given idm group is external therefore it cannot be udpated");
        }
    }

    @Override
    @IsAdmin
    public GroupDeletionStatusDTO delete(IDMGroup group) {
        LOG.debug("delete({})", group);
        Assert.notNull(group, "Input group must not be null.");
        GroupDeletionStatusDTO deletionStatus = checkKypoGroupBeforeDelete(group);
        if (deletionStatus.equals(GroupDeletionStatusDTO.SUCCESS)) {
            groupRepository.delete(group);
        }
        return deletionStatus;
    }

    @Override
    @IsAdmin
    public Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable) {
        LOG.debug("getAllIDMGroups()");
        return groupRepository.findAll(predicate, pageable);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupByName(String name) {
        LOG.debug("getIDMGroupByName({})", name);
        Assert.hasLength(name, "Input name of group must not be empty");
        return groupRepository.findByName(name).orElseThrow(() -> new UserAndGroupServiceException("IDM Group with name " + name + " not found"));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public boolean isGroupInternal(Long id) {
        LOG.debug("isGroupInternal({})", id);
        Assert.notNull(id, "Input id must not be null");
        return groupRepository.isIDMGroupInternal(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public Set<Role> getRolesOfGroup(Long id) {
        LOG.debug("getRolesOfGroup({})", id);
        Assert.notNull(id, "Input id must not be null");
        IDMGroup group = groupRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("Group with id: " + id + " could not be found."));
        return group.getRoles();
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)")
    public IDMGroup assignRole(Long groupId, Long roleId) {
        LOG.debug("assignRole({}, {})", groupId, roleId);
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with " + groupId + " could not be found."));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new UserAndGroupServiceException("Role with id: " + roleId + " could not be found. Start up of the project or registering of microservice probably went wrong, please contact support."));
        group.addRole(role);
        return groupRepository.save(group);
    }

    @Override
    @IsAdmin
    public IDMGroup removeRoleFromGroup(Long groupId, Long roleId) {
        LOG.debug("removeRoleFromGroup({}, {})", groupId, roleId);
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleType must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with " + groupId + " could not be found."));


        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new UserAndGroupServiceException("Role with id: " + roleId + " could not be found. Start up of the project probably went wrong, please contact support."));
        if (group.getName().equalsIgnoreCase(role.getRoleType().toUpperCase())) {
            throw new RoleCannotBeRemovedToGroupException("Role " + role.getRoleType() + " cannot be removed from group. This role is main role of the group");
        }
        group.removeRole(role);
        return groupRepository.save(group);
    }

    @Override
    @IsAdmin
    public IDMGroup removeUsers(Long groupId, List<Long> userIds) {
        LOG.debug("removeUsers({}, {})", groupId, userIds);
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(userIds, "Input list of users ids must not be null");

        IDMGroup groupToUpdate = this.get(groupId);
        if (!groupRepository.isIDMGroupInternal(groupId)) {
            throw new ExternalSourceException("Group is external therefore it could not be updated");
        }
        if(groupToUpdate.getName().equals("DEFAULT_GROUP")) {
            throw new UserAndGroupServiceException("Cannot remove users from default group.");
        }
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserAndGroupServiceException("User with id " + userId + " could not be found"));
            groupToUpdate.removeUser(user);
        }
        return groupRepository.save(groupToUpdate);
    }

    @Override
    @IsAdmin
    public IDMGroup addUsers(Long groupId, List<Long> idsOfGroupsOfImportedUsers, List<Long> idsOfUsersToBeAdd) {
        LOG.debug("addUsers({}, {}, {})", groupId, idsOfGroupsOfImportedUsers, idsOfUsersToBeAdd);
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
            groupOfImportedMembers.getUsers().forEach(user -> {
                if (!groupToUpdate.getUsers().contains(user)) {
                    groupToUpdate.addUser(user);
                }
            });
        }

        return groupRepository.save(groupToUpdate);
    }

    private GroupDeletionStatusDTO checkKypoGroupBeforeDelete(IDMGroup group) {
        List<String> roles = roleRepository.findAll().stream().map(Role::getRoleType).collect(Collectors.toList());
        if (roles.contains(group.getName())) {
            return GroupDeletionStatusDTO.ERROR_MAIN_GROUP;
        }
        return GroupDeletionStatusDTO.SUCCESS;
    }
}
