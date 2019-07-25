package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.security.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.security.service.SecurityService;
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
    private SecurityService securityService;

    @Autowired
    public IDMGroupServiceImpl(IDMGroupRepository idmGroupRepository, RoleRepository roleRepository,
                               UserRepository userRepository, SecurityService securityService) {
        this.groupRepository = idmGroupRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public IDMGroup get(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return groupRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("IDMGroup with id " + id + " not found"));
    }

    @Override
    public IDMGroup getGroupForDefaultRoles() {
        return groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).orElseThrow(() -> new UserAndGroupServiceException("IDM group for default roles not found"));
    }

    @Override
    @IsAdmin
    public IDMGroup create(IDMGroup group, List<Long> groupIdsOfImportedMembers) {
        Assert.notNull(group, "Input group must not be null.");
        group.setStatus(UserAndGroupStatus.VALID);

        if (!groupIdsOfImportedMembers.isEmpty()) {
            Set<User> importedMembersFromGroups = groupRepository.findUsersOfGivenGroups(groupIdsOfImportedMembers);
            for (User importedUserFromGroup : importedMembersFromGroups) {
                group.addUser(importedUserFromGroup);
            }
        }
        return groupRepository.save(group);
    }

    @Override
    @IsAdmin
    public IDMGroup update(IDMGroup group) {
        Assert.notNull(group, "Input group must not be null.");
        if (groupRepository.isIDMGroupInternal(group.getId())) {
            IDMGroup groupInDatabase = get(group.getId());
            if (List.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_USER.getName())
                    .contains(groupInDatabase.getName()) && !groupInDatabase.getName().equals(group.getName())) {
                throw new UserAndGroupServiceException("Cannot change name of main group " + groupInDatabase.getName() + " to " + group.getName() + ".");
            }
            groupInDatabase.setDescription(group.getDescription());
            groupInDatabase.setName(group.getName());
            groupInDatabase.setExpirationDate(group.getExpirationDate());
            return groupRepository.save(groupInDatabase);
        } else {
            throw new ExternalSourceException("Given idm group is external therefore it cannot be updated");
        }
    }

    @Override
    @IsAdmin
    public GroupDeletionStatusDTO delete(IDMGroup group) {
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
        return groupRepository.findAll(predicate, pageable);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#name)")
    public IDMGroup getIDMGroupByName(String name) {
        Assert.hasLength(name, "Input name of group must not be empty");
        return groupRepository.findByName(name).orElseThrow(() -> new UserAndGroupServiceException("IDM Group with name " + name + " not found"));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public boolean isGroupInternal(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return groupRepository.isIDMGroupInternal(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR) " +
            "or @securityService.isLoggedInUserInGroup(#id)")
    public Set<Role> getRolesOfGroup(Long id) {
        Assert.notNull(id, "Input id must not be null");
        IDMGroup group = groupRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("Group with id: " + id + " could not be found."));
        return group.getRoles();
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)")
    public IDMGroup assignRole(Long groupId, Long roleId) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with id: " + groupId + " could not be found."));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new UserAndGroupServiceException("Role with id: " + roleId + " could not be found. Start up of the project or registering of microservice probably went wrong, please contact support."));
        group.addRole(role);
        return groupRepository.save(group);
    }

    @Override
    @IsAdmin
    public IDMGroup removeRoleFromGroup(Long groupId, Long roleId) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleType must not be null");

        IDMGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new UserAndGroupServiceException("Group with id: " + groupId + " could not be found."));


        for (Role role : group.getRoles()) {
            if (role.getId().equals(roleId)) {
                if (group.getName().equals(ImplicitGroupNames.DEFAULT_GROUP.getName()) && role.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_GUEST.name()) ||
                        group.getName().equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName()) && role.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name()) ||
                        group.getName().equals(ImplicitGroupNames.USER_AND_GROUP_USER.getName()) && role.getRoleType().equals(RoleType.ROLE_USER_AND_GROUP_USER.name())) {
                    throw new RoleCannotBeRemovedToGroupException("Role " + role.getRoleType() + " cannot be removed from group. This role is main role of the group");
                }
                group.removeRole(role);
                return groupRepository.save(group);
            }
        }
        throw new UserAndGroupServiceException("Role with id: " + roleId + " could not be found in given group.");
    }

    @Override
    @IsAdmin
    public IDMGroup removeUsers(Long groupId, List<Long> userIds) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(userIds, "Input list of users ids must not be null");

        IDMGroup groupToUpdate = this.get(groupId);
        if (!groupRepository.isIDMGroupInternal(groupId)) {
            throw new ExternalSourceException("Group is external therefore it could not be updated");
        }
        if (groupToUpdate.getName().equals(ImplicitGroupNames.DEFAULT_GROUP.getName())) {
            throw new UserAndGroupServiceException("Cannot remove users from default group.");
        }
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserAndGroupServiceException("User with id " + userId + " could not be found"));
            if (groupToUpdate.getName().equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName()) && securityService.hasLoggedInUserSameLogin(user.getLogin())) {
                throw new UserAndGroupServiceException("An administrator could not remove himself from the administrator group.");
            }
            groupToUpdate.removeUser(user);
        }
        return groupRepository.save(groupToUpdate);
    }

    @Override
    @IsAdmin
    public IDMGroup addUsers(Long groupId, List<Long> idsOfGroupsOfImportedUsers, List<Long> idsOfUsersToBeAdd) {
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
        if (List.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_USER.getName()).contains(group.getName())) {
            return GroupDeletionStatusDTO.ERROR_MAIN_GROUP;
        }
        return GroupDeletionStatusDTO.SUCCESS;
    }
}
