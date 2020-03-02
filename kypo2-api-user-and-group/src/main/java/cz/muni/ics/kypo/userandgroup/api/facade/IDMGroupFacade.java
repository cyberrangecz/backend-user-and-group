package cz.muni.ics.kypo.userandgroup.api.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * The interface for the IDMGroup facade layer.
 */
public interface IDMGroupFacade {

    /**
     * Gets IDMGroup with given ID from database.
     *
     * @param groupId the ID of the IDMGroup to be loaded.
     * @return the IDMGroup with the given ID.
     */
    GroupDTO getGroupById(Long groupId);

    /**
     * Gets GroupWithRolesDto with given groupName from database.
     *
     * @param groupName the name of the group to be retrieved.
     * @return the status of the deletion {@link GroupDeletionResponseDTO}.
     */
    GroupWithRolesDTO getIDMGroupWithRolesByName(String groupName);

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
     */
    void updateGroup(UpdateGroupDTO updateGroupDTO);

    /**
     * Delete the given group from the database.
     *
     * @param groupId the ID of the group to be deleted.
     */
    void deleteGroup(Long groupId);

    /**
     * Delete groups with given IDs from the database.
     *
     * @param groupIds IDs of groups to be deleted.
     */
    void deleteGroups(List<Long> groupIds);

    /**
     * Returns all groups from the database specified by the given predicate and pageable.
     *
     * @param predicate specifies query to database.
     * @param pageable  abstract interface for pagination information.
     * @return a list of all {@link GroupDTO} from the database wrapped up in {@link PageResultResource}.
     */
    PageResultResource<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable);

    /**
     * Returns all roles of the group with the given ID.
     *
     * @param groupId the ID of the IDMGroup.
     * @return set of {@link RoleDTO} of IDMGroup with the given ID.
     */
    Set<RoleDTO> getRolesOfGroup(Long groupId);

    /**
     * Assigns the role to the IDMGroup with the given ID. All users in group will obtain an assigned role.
     *
     * @param groupId the ID of the IDMGroup which will get role with given role ID.
     * @param roleId  the ID of the role to be assigned to IDMGroup.
     */
    void assignRole(Long groupId, Long roleId);

    /**
     * Removes the role from the IDMGroup with the given ID. All users in the group
     * will lose this role if they do not take on this role from another group.
     *
     * @param groupId the ID of the IDMGroup from which role with role ID is removed.
     * @param roleId  the ID of the role.
     * @throws UserAndGroupFacadeException         if the IDMGroup or role could not be found.
     */
    void removeRoleFromGroup(Long groupId, Long roleId);

    /**
     * Removes members with a given list of user IDs from the IDMGroup.
     *
     * @param groupId the ID of the IDMGroup.
     * @param userIds a list of IDs of users to be removed from given IDMGroup.
     */
    void removeUsers(Long groupId, List<Long> userIds);

    /**
     * Adds user to particular group.
     *
     * @param groupId the ID of the group to add users.
     * @param userId  the ID of the user.
     */
    void addUser(Long groupId, Long userId);

    /**
     * Adds users from {@link AddUsersToGroupDTO} to the IDMGroup with the given ID.
     *
     * @param groupId  the ID of the group to add users.
     * @param addUsers DTO containing a list of IDs of users and a list of IDs of groups.
     */
    void addUsersToGroup(Long groupId, AddUsersToGroupDTO addUsers);


}
