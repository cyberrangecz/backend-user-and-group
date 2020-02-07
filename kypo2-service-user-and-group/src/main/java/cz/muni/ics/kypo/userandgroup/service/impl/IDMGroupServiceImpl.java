package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.model.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Service
public class IDMGroupServiceImpl implements IDMGroupService {

    private final IDMGroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private SecurityService securityService;

    @Autowired
    public IDMGroupServiceImpl(IDMGroupRepository idmGroupRepository, RoleRepository roleRepository, SecurityService securityService) {
        this.groupRepository = idmGroupRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
    }

    @Override
    public IDMGroup getGroupById(Long id) {
        Assert.notNull(id, "In method getGroupById(id) the input must not be null.");
        return groupRepository.findById(id)
                .orElseThrow(() -> new UserAndGroupServiceException("IDMGroup with id " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public List<IDMGroup> getGroupsByIds(List<Long> groupIds) {
        Assert.notNull(groupIds, "In method getGroupsByIds(groupIds) the input must not be null.");
        return groupRepository.findByIdIn(groupIds);
    }

    @Override
    public IDMGroup getGroupForDefaultRoles() {
        return groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName())
                .orElseThrow(() -> new UserAndGroupServiceException("IDM group for default roles not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public IDMGroup createIDMGroup(IDMGroup group, List<Long> groupIdsOfImportedMembers) {
        Assert.notNull(group, "In method createIDMGroup(id) the input must not be null.");
        Assert.notNull(groupIdsOfImportedMembers, "In method createIDMGroup(id) the input must not be null.");
        group.setStatus(UserAndGroupStatus.VALID);
        if (groupRepository.existsByName(group.getName())) {
            throw new UserAndGroupServiceException("Group with name " + group.getName() + " already exists in database.", ErrorCode.RESOURCE_NOT_CREATED);
        }

        if (!groupIdsOfImportedMembers.isEmpty()) {
            Set<User> importedMembersFromGroups = groupRepository.findUsersOfGivenGroups(groupIdsOfImportedMembers);
            for (User importedUserFromGroup : importedMembersFromGroups) {
                group.addUser(importedUserFromGroup);
            }
        }
        return groupRepository.save(group);
    }

    @Override
    public IDMGroup updateIDMGroup(IDMGroup group) {
        Assert.notNull(group, "In method updateIDMGroup(id) the input must not be null.");
        IDMGroup groupInDatabase = getGroupById(group.getId());
        if (getImplicitGroupNames().contains(groupInDatabase.getName()) && !groupInDatabase.getName().equals(group.getName())) {
            throw new UserAndGroupServiceException("Cannot change name of main group " + groupInDatabase.getName() + " to " + group.getName() + ".", ErrorCode.RESOURCE_CONFLICT);
        }
        groupInDatabase.setDescription(group.getDescription());
        groupInDatabase.setName(group.getName());
        groupInDatabase.setExpirationDate(group.getExpirationDate());
        return groupRepository.save(groupInDatabase);
    }

    @Override
    public void deleteIDMGroup(IDMGroup group) {
        Assert.notNull(group, "In method updateIDMGroup(id) the input must not be null.");
        if (getImplicitGroupNames().contains(group.getName())) {
            throw new UserAndGroupServiceException("It is not possible to delete group with id: " + group.getId() + ". " +
                    "This group is User and Group default group that could not be deleted.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (group.getUsers() == null || !group.getUsers().isEmpty())
        throw new UserAndGroupServiceException("It is not possible to delete group with id: " + group.getId() + ". The group must be empty (without users)", ErrorCode.RESOURCE_CONFLICT);
        groupRepository.delete(group);
    }

    @Override
    public Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable) {
        return groupRepository.findAll(predicate, pageable);
    }

    @Override
    public IDMGroup getIDMGroupByName(String name) {
        Assert.hasLength(name, "Input name of group must not be empty");
        return groupRepository.findByName(name)
                .orElseThrow(() -> new UserAndGroupServiceException("IDM Group with name " + name + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public IDMGroup getIDMGroupWithRolesByName(String name) {
        Assert.hasLength(name, "Input name of group must not be empty");
        return groupRepository.findByNameWithRoles(name)
                .orElseThrow(() -> new UserAndGroupServiceException("IDM Group with name " + name + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Set<Role> getRolesOfGroup(Long groupId) {
        Assert.notNull(groupId, "In method getRolesOfGroup(id) the input must not be null.");
        IDMGroup group = this.getGroupById(groupId);
        return group.getRoles();
    }

    @Override
    public IDMGroup assignRole(Long groupId, Long roleId) {
        Assert.notNull(groupId, "In method assignRole(groupId, roleId) the input groupId must not be null.");
        Assert.notNull(roleId, "In method assignRole(groupId, roleId) the input roleId must not be null.");
        IDMGroup group = this.getGroupById(groupId);
        Role role = roleRepository.findById(roleId).orElseThrow(() ->
                        new UserAndGroupServiceException("Role with id: " + roleId + " could not be found. Start up of the" +
                                " project or registering of microservice probably went wrong, please contact support.", ErrorCode.RESOURCE_NOT_FOUND));
        group.addRole(role);
        return groupRepository.save(group);
    }

    @Override
    public IDMGroup removeRoleFromGroup(Long groupId, Long roleId) {
        Assert.notNull(groupId, "In method assignRole(groupId, roleId) the input groupId must not be null.");
        Assert.notNull(roleId, "In method assignRole(groupId, roleId) the input roleId must not be null.");

        IDMGroup group = this.getGroupById(groupId);

        for (Role role : group.getRoles()) {
            if (role.getId().equals(roleId)) {
                checkIfCanBeRemoved(group.getName(), role.getRoleType());
                group.removeRole(role);
                return groupRepository.save(group);
            }
        }
        throw new UserAndGroupServiceException("Role with id: " + roleId + " could not be found in given group.", ErrorCode.RESOURCE_NOT_FOUND);
    }

    private void checkIfCanBeRemoved(String groupName, String roleType) {
        if (groupName.equals(ImplicitGroupNames.DEFAULT_GROUP.getName()) && roleType.equals(RoleType.ROLE_USER_AND_GROUP_GUEST.name()) ||
                groupName.equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName()) && roleType.equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name()) ||
                groupName.equals(ImplicitGroupNames.USER_AND_GROUP_USER.getName()) && roleType.equals(RoleType.ROLE_USER_AND_GROUP_USER.name())) {
            throw new UserAndGroupServiceException("Role " + roleType + " cannot be removed from group. This role is main role of the group.", ErrorCode.RESOURCE_CONFLICT);
        }
    }

    //    @CacheEvict(value = AbstractCacheNames.USERS_CACHE_NAME, key = "{#user.sub+#user.iss}")
    @Override
    public void removeUserFromGroup(IDMGroup groupToUpdate, User user) {
        if (groupToUpdate.getName().equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName()) && securityService.hasLoggedInUserSameLogin(user.getLogin())) {
            throw new UserAndGroupServiceException("An administrator could not remove himself from the administrator group.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (groupToUpdate.getName().equals(ImplicitGroupNames.DEFAULT_GROUP.getName())) {
            throw new UserAndGroupServiceException("Cannot remove user(s) from default group.", ErrorCode.RESOURCE_CONFLICT);
        }
        groupToUpdate.removeUser(user);
    }

    //    @CacheEvict(value = AbstractCacheNames.USERS_CACHE_NAME, key = "{#userToBeAdded.sub+#userToBeAdded.iss}")
    @Override
    public IDMGroup addUserToGroup(IDMGroup groupToUpdate, User userToBeAdded) {
        groupToUpdate.addUser(userToBeAdded);
        return groupRepository.save(groupToUpdate);
    }

    private List<String> getImplicitGroupNames(){
        return List.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_USER.getName());
    }

    /**
     * Evict user from cache since his roles has changed
     *
     * @param user user to be evicted from the cache
     */
    @Override
//    @CacheEvict(AbstractCacheNames.USERS_CACHE_NAME, key = "{#user.sub+#user.iss}")
    public void evictUserFromCache(User user) {
        // evicting user from cache
    }

}
