package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.enums.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.enums.dto.ImplicitGroupNames;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class IDMGroupService {

    private final IDMGroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final SecurityService securityService;

    @Autowired
    public IDMGroupService(IDMGroupRepository idmGroupRepository, RoleRepository roleRepository, SecurityService securityService) {
        this.groupRepository = idmGroupRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
    }

    public IDMGroup getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "id", id.getClass(), id)));
    }

    public List<IDMGroup> getGroupsByIds(List<Long> groupIds) {
        return groupRepository.findByIdIn(groupIds);
    }

    public IDMGroup getGroupForDefaultRoles() {
        return groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName())
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "name", String.class, ImplicitGroupNames.DEFAULT_GROUP.getName())));
    }

    public IDMGroup createIDMGroup(IDMGroup group, List<Long> groupIdsOfImportedMembers) {
        group.setStatus(UserAndGroupStatus.VALID);
        if (groupRepository.existsByName(group.getName())) {
            throw new EntityConflictException(new EntityErrorDetail(IDMGroup.class, "name", String.class, group.getName(), "Group with name '" + group.getName() + "' already exists."));
        }

        if (!groupIdsOfImportedMembers.isEmpty()) {
            Set<User> importedMembersFromGroups = groupRepository.findUsersOfGivenGroups(groupIdsOfImportedMembers);
            for (User importedUserFromGroup : importedMembersFromGroups) {
                group.addUser(importedUserFromGroup);
            }
        }
        return groupRepository.save(group);
    }

    public IDMGroup updateIDMGroup(IDMGroup group) {
        IDMGroup groupInDatabase = getGroupById(group.getId());
        if (getImplicitGroupNames().contains(groupInDatabase.getName()) && !groupInDatabase.getName().equals(group.getName())) {
            throw new EntityConflictException(new EntityErrorDetail(IDMGroup.class, "id", group.getId().getClass(), group.getId(), "Name of main group cannot be changed"));
        }
        groupInDatabase.setDescription(group.getDescription());
        groupInDatabase.setName(group.getName());
        groupInDatabase.setExpirationDate(group.getExpirationDate());
        return groupRepository.save(groupInDatabase);
    }

    public void deleteIDMGroup(IDMGroup group) {
        if (getImplicitGroupNames().contains(group.getName())) {
            throw new EntityConflictException(new EntityErrorDetail(IDMGroup.class, "name", String.class, group.getName(),
                    "This group is User and Group default group that cannot be deleted."));
        }
        if (group.getUsers() == null || !group.getUsers().isEmpty())
            throw new EntityConflictException(new EntityErrorDetail(IDMGroup.class, "id", Long.class, group.getId(),
                    "The group must be empty (without users) before it is deleted."));
        groupRepository.delete(group);
    }

    public Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable) {
        return groupRepository.findAll(predicate, pageable);
    }

    public IDMGroup getIDMGroupByName(String name) {
        return groupRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "name", name.getClass(), name)));
    }

    public IDMGroup getIDMGroupWithRolesByName(String name) {
        return groupRepository.findByNameWithRoles(name)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "name", name.getClass(), name)));
    }

    public Page<Role> getRolesOfGroup(Long groupId, Pageable pageable, Predicate predicate) {
        if (!groupRepository.existsById(groupId)) {
            throw new EntityNotFoundException(new EntityErrorDetail(IDMGroup.class, "id", groupId.getClass(), groupId,
                    "Group not found."));
        }
        return this.roleRepository.findAllOfGroup(groupId, pageable, predicate);
    }

    public IDMGroup assignRole(Long groupId, Long roleId) {
        IDMGroup group = this.getGroupById(groupId);
        Role role = roleRepository.findById(roleId).orElseThrow(() ->
                new EntityNotFoundException(new EntityErrorDetail(Role.class, "id", roleId.getClass(), roleId,
                        "Role not found. Start up of the project or registering of microservice probably went wrong, please contact support.")));
        group.addRole(role);
        return groupRepository.save(group);
    }

    public IDMGroup removeRoleFromGroup(Long groupId, Long roleId) {
        IDMGroup group = this.getGroupById(groupId);

        for (Role role : group.getRoles()) {
            if (role.getId().equals(roleId)) {
                checkIfCanBeRemoved(group.getName(), role.getRoleType());
                group.removeRole(role);
                return groupRepository.save(group);
            }
        }
        throw new EntityNotFoundException(new EntityErrorDetail(Role.class, "id", roleId.getClass(), roleId, "Role was not found in group."));
    }

    private void checkIfCanBeRemoved(String groupName, String roleType) {
        if (groupName.equals(ImplicitGroupNames.DEFAULT_GROUP.getName()) && roleType.equals(RoleType.ROLE_USER_AND_GROUP_TRAINEE.name()) ||
                groupName.equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName()) && roleType.equals(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name()) ||
                groupName.equals(ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName()) && roleType.equals(RoleType.ROLE_USER_AND_GROUP_POWER_USER.name())) {
            throw new EntityConflictException(new EntityErrorDetail(Role.class, "roleType", String.class, roleType,
                    "Main role of the group cannot be removed."));
        }
    }

    //    @CacheEvict(value = AbstractCacheNames.USERS_CACHE_NAME, key = "{#user.sub+#user.iss}")
    public void removeUserFromGroup(IDMGroup groupToUpdate, User user) {
        if (groupToUpdate.getName().equals(ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName()) && securityService.hasLoggedInUserSameLogin(user.getSub())) {
            throw new EntityConflictException(new EntityErrorDetail(IDMGroup.class, "name", String.class, groupToUpdate.getName(),
                    "An administrator cannot remove himself from the administrator group."));
        }
        if (groupToUpdate.getName().equals(ImplicitGroupNames.DEFAULT_GROUP.getName())) {
            throw new EntityConflictException(new EntityErrorDetail(IDMGroup.class, "name", String.class, groupToUpdate.getName(),
                    "Cannot remove user(s) from default group."));
        }
        groupToUpdate.removeUser(user);
    }

    //    @CacheEvict(value = AbstractCacheNames.USERS_CACHE_NAME, key = "{#userToBeAdded.sub+#userToBeAdded.iss}")
    public IDMGroup addUserToGroup(IDMGroup groupToUpdate, User userToBeAdded) {
        groupToUpdate.addUser(userToBeAdded);
        return groupRepository.save(groupToUpdate);
    }

    private List<String> getImplicitGroupNames() {
        return List.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName());
    }

    /**
     * Evict user from cache since his roles has changed
     *
     * @param user user to be evicted from the cache
     */
//    @CacheEvict(AbstractCacheNames.USERS_CACHE_NAME, key = "{#user.sub+#user.iss}")
    public void evictUserFromCache(User user) {
        // evicting user from cache
    }

}
