package cz.muni.ics.kypo.userandgroup.api.facade;

import cz.muni.ics.kypo.userandgroup.api.config.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import java.util.List;
import java.util.Set;

/**
 * The interface for the IDMGroup facade layer.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface IDMGroupFacade {

    /**
     * Gets IDMGroup with given ID from database.
     *
     * @param groupId the ID of the IDMGroup to be loaded.
     * @return the IDMGroup with the given ID.
     * @throws UserAndGroupFacadeException if group was not found
     */
    GroupDTO getGroup(Long groupId);

    /**
     * Creates a IDMGroup with information from the given {@link NewGroupDTO} and returns a created group.
     *
     * @param newGroupDTO a group to be created.
     * @return a created group encapsulate in {@link GroupDTO}.
     */
    GroupDTO createGroup(NewGroupDTO newGroupDTO);

    /**
     * Update given IDMGroup with information from the given {@link UpdateGroupDTO}.
     *
     * @param updateGroupDTO a group to be updated.
     * @throws ExternalSourceException if the given group is external and cannot be edited.
     */
    void updateGroup(UpdateGroupDTO updateGroupDTO);

    /**
     * Delete the given group from the database and return the status of the deletion.
     *
     * @param groupId the ID of the group to be deleted.
     * @return the status of the deletion {@link GroupDeletionResponseDTO}.
     */
    GroupDeletionResponseDTO deleteGroup(Long groupId);

    /**
     * Returns all groups from the database specified by the given predicate and pageable.
     *
     * @param predicate specifies query to database.
     * @param pageable  abstract interface for pagination information.
     * @return a list of all {@link GroupDTO} from the database wrapped up in {@link PageResultResource}.
     */
    PageResultResource<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable);

    /**
     * Returns true if the group is internal otherwise false.
     *
     * @param groupId the ID of the group.
     * @return true if the group is internal otherwise false.
     * @throws UserAndGroupFacadeException if the group was not found.
     */
    boolean isGroupInternal(Long groupId);

    /**
     * Returns all roles of the group with the given ID.
     *
     * @param groupId the ID of the IDMGroup.
     * @return set of {@link RoleDTO} of IDMGroup with the given ID.
     * @throws UserAndGroupFacadeException if the IDMGroup was not found.
     */
    Set<RoleDTO> getRolesOfGroup(Long groupId);

    /**
     * Assigns the role to the IDMGroup with the given ID. All users in group will obtain an assigned role.
     *
     * @param groupId the ID of the IDMGroup which will get role with given role ID.
     * @param roleId  the ID of the role to be assigned to IDMGroup.
     * @throws UserAndGroupFacadeException if the IDMGroup or the role with the given ID could not be found.
     */
    void assignRole(Long groupId, Long roleId);

    /**
     * Removes the role from the IDMGroup with the given ID. All users in the group
     * will lose this role if they do not take on this role from another group.
     *
     * @param groupId the ID of the IDMGroup from which role with role ID is removed.
     * @param roleId  the ID of the role.
     * @throws UserAndGroupFacadeException if the IDMGroup or role could not be found.
     * @throws RoleCannotBeRemovedToGroupException if role GUEST, USER, ADMINISTRATOR is removed from IDMGroup with name  DEFAULT-GROUP,
     * USER-AND-GROUP_USER, USER-AND-GROUP_ADMINISTRATOR.
     */
    void removeRoleFromGroup(Long groupId, Long roleId);

    /**
     * Removes members with a given list of user IDs from the IDMGroup.
     *
     * @param groupId the ID of the IDMGroup.
     * @param userIds a list of IDs of users to be removed from given IDMGroup.
     * @throws UserAndGroupFacadeException if the IDMGroup or the user could not be found, IDMGroup is DEFAULT-GROUP,
     * the administrator is trying to remove himself from USER-AND-GROUP_ADMINISTRATOR group.
     * @throws ExternalSourceException if the group with the given group ID is external and cannot be edited.
     */
    void removeUsers(Long groupId, List<Long> userIds);

    /**
     * Adds users from {@link AddUsersToGroupDTO} to the IDMGroup with the given ID.
     *
     * @param groupId the ID of the group to add users.
     * @param addUsers DTO containing a list of IDs of users and a list of IDs of groups.
     * @throws UserAndGroupFacadeException if the IDMGroup or some user could not be found.
     * @throws ExternalSourceException if the IDMGroup with given group ID is external and cannot be edited.
     */
    void addUsers(Long groupId, AddUsersToGroupDTO addUsers);

    /**
     * Delete groups with given IDs from the database and returns statuses of deletion with groups name and IDs.
     *
     * @param groupIds IDs of groups to be deleted.
     * @return statuses of deletion {@link GroupDeletionResponseDTO}.
     */
    List<GroupDeletionResponseDTO> deleteGroups(List<Long> groupIds);

}
