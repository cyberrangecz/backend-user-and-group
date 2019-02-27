package cz.muni.ics.kypo.userandgroup.service.interfaces;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.types.Predicate;

import java.util.List;
import java.util.Set;

public interface IDMGroupService {
    /**
     * Gets IDM group with given id from database.
     *
     * @param id of the IDM group to be loaded
     * @return IDM group with given id
     * @throws UserAndGroupServiceException if group was not found
     */
    IDMGroup get(Long id) throws UserAndGroupServiceException;

    /**
     * Creates given IDM group in database.
     *
     * @param group group to be created
     * @param groupIdsOfImportedMembers all users from groups with given ids will be imported to new group
     * @return created group
     * @throws UserAndGroupServiceException if some of group with given ids could not be found
     */
    IDMGroup create(IDMGroup group, List<Long> groupIdsOfImportedMembers) throws UserAndGroupServiceException;

    /**
     * Updates given IDM group in database.
     *
     * @param group group to be updated
     * @return updated group
     * @throws ExternalSourceException if group with given groupID is external and cannot be edited
     */
    IDMGroup update(IDMGroup group) throws ExternalSourceException;

    /**
     * Delete given IDM group from database and return status of deletion.
     * Return values: SUCCESS - succesfully deleted
     *                EXTERNAL_VALID - group is from external source and is not marked as DELETED
     *
     * @param group group to be deleted
     * @return status of deletion
     */
    GroupDeletionStatusDTO delete(IDMGroup group);

    /**
     * Returns all IDM groups from database.
     *
     * @return IDM groups in database
     */
    Page<IDMGroup> getAllIDMGroups(Predicate predicate, Pageable pageable);

    /**
     * Gets IDM group with given name from database.
     *
     * @param name of the IDM group to be loaded
     * @return IDM group with given name
     * @throws UserAndGroupServiceException if group was not found
     */
    IDMGroup getIDMGroupByName(String name) throws UserAndGroupServiceException;

    /**
     * Gets IDM groups with given name from database using like.
     *
     * @param name of the IDM group to be loaded (with '%' symbol)
     * @return IDM groups with given name
     * @throws UserAndGroupServiceException
     */
    Page<IDMGroup> getIDMGroupsByName(String name, Pageable pageable) throws UserAndGroupServiceException;

    /**
     * Returns IDM group with assigned users from database
     *
     * @param id of the IDM group to be loaded
     * @return IDM group with assigned users in database
     * @throws UserAndGroupServiceException if group was not found
     */
    IDMGroup getIDMGroupWithUsers(Long id) throws UserAndGroupServiceException;

    /**
     * Returns IDM group with assigned users from database
     *
     * @param name of the IDM group to be loaded
     * @return IDM group with assigned users in database
     * @throws UserAndGroupServiceException if group was not found
     */
    IDMGroup getIDMGroupWithUsers(String name) throws UserAndGroupServiceException;

    /**
     * Returns true if group is internal otherwise false
     *
     * @param id of group
     * @return true if group is internal otherwise false
     * @throws UserAndGroupServiceException if group was not found
     */
    boolean isGroupInternal(Long id) throws UserAndGroupServiceException;

    /**
     * Returns all roles of group with given id
     *
     * @param id of group.
     * @return all roles of group with given id
     * @throws UserAndGroupServiceException if group was not found
     */
    Set<Role> getRolesOfGroup(Long id) throws UserAndGroupServiceException;

    /**
     * Assigns role to group with given groupId
     *
     * @param groupId id of group which will get role with role type
     * @param roleType type of assigning role
     * @return group with assigned role with given role type
     * @throws UserAndGroupServiceException if group or one of the main role could not be find
     */
    IDMGroup assignRole(Long groupId, RoleType roleType) throws UserAndGroupServiceException;

    /**
     * Removes role to group with given groupId
     *
     * @param groupId id of group which will lose role with role type
     * @param roleType type of role
     * @return group with lost role with given role type
     * @throws UserAndGroupServiceException if group or one of the main role could not be find
     * @throws RoleCannotBeRemovedToGroupException if role is GUEST or USER which cannot be removed to groups
     */
    IDMGroup removeRoleToGroup(Long groupId, RoleType roleType) throws UserAndGroupServiceException, RoleCannotBeRemovedToGroupException;

    /**
     * Removes members of group with given userIds from the group
     *
     * @param groupId id of group
     * @param userIds ids of users to be removed from given group
     * @return updated group
     * @throws UserAndGroupServiceException if some group or user could not be found
     * @throws ExternalSourceException if group with given groupID is external and cannot be edited
     */
    IDMGroup removeUsers(Long groupId, List<Long> userIds) throws UserAndGroupServiceException, ExternalSourceException;

    /**
     * Adds users and users from groups to group with given groupId
     *
     * @param groupId id of group
     * @param idsOfGroupsOfImportedUsers users of groups with given ids will be added to given group
     * @param idsOfUsersToBeAdd ids of users to be added to given group
     * @return updated group
     * @throws UserAndGroupServiceException if some group or user could not be found
     * @throws ExternalSourceException if group with given groupID is external and cannot be edited
     */
    IDMGroup addUsers(Long groupId, List<Long> idsOfGroupsOfImportedUsers, List<Long> idsOfUsersToBeAdd) throws UserAndGroupServiceException, ExternalSourceException;
}
