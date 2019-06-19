package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * The interface for IDMGroup service layer.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface IDMGroupService {
    /**
     * Gets IDMGroup with the given ID.
     *
     * @param id The ID of the IDMGroup to be loaded.
     * @return the {@link IDMGroup} with the given ID.
     * @throws UserAndGroupServiceException if a group was not found.
     */
    IDMGroup get(Long id);

    /**
     * Creates new IDMGroup.
     *
     * @param group IDMGroup to be created.
     * @param groupIdsOfImportedMembers all {@link cz.muni.ics.kypo.userandgroup.model.User}s from groups with given IDs will be imported to new group.
     * @return created {@link IDMGroup}.
     * @throws UserAndGroupServiceException if some of the groups with given IDs could not be found.
     */
    IDMGroup create(IDMGroup group, List<Long> groupIdsOfImportedMembers);

    /**
     * Update given IDMGroup.
     *
     * @param group IDMGroup to be updated.
     * @return the {@link IDMGroup} with  updated fields.
     * @throws ExternalSourceException if the group with the given ID is external and cannot be edited.
     * @throws UserAndGroupServiceException if the group with the given ID is main and its name is trying to be changed.
     */
    IDMGroup update(IDMGroup group);

    /**
     * Delete given IDMGroup from the database and return status of the deletion.
     *
     * @param group the group to be deleted.
     * @return status of the group deletion {@link cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDeletionResponseDTO}.
     */
    GroupDeletionStatusDTO delete(IDMGroup group);

    /**
     * Returns all IDMGroups from the database.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return list of all {@link IDMGroup}s from database wrapped up in {@link Page}.
     */
    Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable);

    /**
     * Gets the IDMGroup with the given name.
     *
     * @param name the name of the IDMGroup to be loaded.
     * @return the {@link IDMGroup} with the given name
     * @throws UserAndGroupServiceException if the group was not found.
     */
    IDMGroup getIDMGroupByName(String name);

    /**
     * Returns true if IDMGroup is internal, false otherwise.
     *
     * @param id the ID of the IDMGroup.
     * @return true if the {@link IDMGroup} is internal otherwise false.
     * @throws UserAndGroupServiceException if the group was not found.
     */
    boolean isGroupInternal(Long id);

    /**
     * Returns all roles of IDMGroup with given ID.
     *
     * @param id ID of the IDMGroup.
     * @return all roles of {@link IDMGroup} with the given ID.
     * @throws UserAndGroupServiceException if the group was not found.
     */
    Set<Role> getRolesOfGroup(Long id);

    /**
     * Assigns the role to the IDMGroup with the given ID. All {@link cz.muni.ics.kypo.userandgroup.model.User}s in the group
     * will obtain an assigned role.
     *
     * @param groupId the ID of the IDMGroup which will get the role with the given role ID.
     * @param roleId  the ID of the role to be assigned to the IDMGroup.
     * @return the {@link IDMGroup} with the assigned role with the given role ID.
     * @throws UserAndGroupServiceException if the IDMGroup or role with a given ID could not be found.
     */
    IDMGroup assignRole(Long groupId, Long roleId);

    /**
     * Removes role from IDMGroup with the given ID. All {@link cz.muni.ics.kypo.userandgroup.model.User}s in the group
     * will lose this role if they do not take on this role from another group.
     *
     * @param groupId the ID of the IDMGroup from which role with role ID is removed.
     * @param roleId  the ID of the role.
     * @return the {@link IDMGroup} with the removed role.
     * @throws UserAndGroupServiceException if the IDMGroup or the role could not be found.
     * @throws RoleCannotBeRemovedToGroupException if the role <i>GUEST, USER, ADMINISTRATOR<i/> is removed from the IDMGroup with name  <i>DEFAULT-GROUP,
     * USER-AND-GROUP_USER, USER-AND-GROUP_ADMINISTRATOR<i/>.
     */
    IDMGroup removeRoleFromGroup(Long groupId, Long roleId);

    /**
     * Removes members of IDMGroup with given list of user IDs from the IDMGroup.
     *
     * @param groupId the ID of the IDMGroup.
     * @param userIds list of IDs of users to be removed from given IDMGroup
     * @return IDMGroup without removed members.
     * @throws UserAndGroupServiceException if the IDMGroup or the user could not be found, IDMGroup is DEFAULT-GROUP,
     * an administrator is trying to remove himself from USER-AND-GROUP_ADMINISTRATOR group.
     * @throws ExternalSourceException if the group with given group ID is external and cannot be edited.
     */
    IDMGroup removeUsers(Long groupId, List<Long> userIds);

    /**
     * Adds users from the list of user IDs and users from IDMGroups from the list of group IDs to the IDMGroup with the given ID.
     *
     * @param groupId                    the ID of the group to add users.
     * @param idsOfGroupsOfImportedUsers list of group IDs whose users to be imported to the given IDMGroup.
     * @param idsOfUsersToBeAdd          list of user IDs to be imported to the given group.
     * @return updated {@link IDMGroup}.
     * @throws UserAndGroupServiceException if the IDMGroup or some user could not be found.
     * @throws ExternalSourceException      if the IDMGroup with a given group ID is external and cannot be edited.
     */
    IDMGroup addUsers(Long groupId, List<Long> idsOfGroupsOfImportedUsers, List<Long> idsOfUsersToBeAdd);

    /**
     * Gets IDMGroup for default roles from the database.
     *
     * @return {@link IDMGroup} for default roles - <i>DEFAULT-GROUP<i/>.
     * @throws UserAndGroupServiceException if the IDMGroup was not found.
     */
    IDMGroup getGroupForDefaultRoles();
}
