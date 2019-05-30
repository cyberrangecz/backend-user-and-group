package cz.muni.ics.kypo.userandgroup.api.facade;

import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.ResponseRoleToGroupInMicroservicesDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.RoleAndMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestClientException;

import com.querydsl.core.types.Predicate;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface IDMGroupFacade {

    /**
     * Creates IDMGroup with information from given newGroupDTO and returns created group.
     *
     * @param newGroupDTO group to be created
     * @return created group
     * @throws UserAndGroupFacadeException if some of group could not be found from given list of group ids
     */
    GroupDTO createGroup(NewGroupDTO newGroupDTO);

    /**
     * Updates given IDM group in database.
     *
     * @param updateGroupDTO group to be updated
     * @throws ExternalSourceException if group with given group is external and cannot be edited
     */
    void updateGroup(UpdateGroupDTO updateGroupDTO);

    /**
     * Removes members of group with given userIds from the group
     *
     * @param groupId id of group from which given users should be removed
     * @param userIds ids of users to be removed from given group
     * @throws UserAndGroupFacadeException if some group or user could not be found
     * @throws ExternalSourceException if group with given group is external and cannot be edited
     */
    void removeUsers(Long groupId, List<Long> userIds);

    /**
     * Adds users and users from groups to group with given groupId in input parameter addMember
     *
     * @param groupId id of group to add users
     * @param addUsers parameter containing users to be added, groups which users will be added to group with groupId
     * @throws UserAndGroupFacadeException if some group or user could not be found
     * @throws ExternalSourceException if group with given group is external and cannot be edited
     */
    void addUsers(Long groupId, AddUsersToGroupDTO addUsers);

    /**
     * Deletes group with given id from database and returns status of deletion with group name and id.
     *
     * @param id of group to be deleted
     * @return status of deletion with name and id of group
     */
    GroupDeletionResponseDTO deleteGroup(Long id);

    /**
     * Deletes groups with given ids from database and returns statuses of deletion with groups name and id.
     *
     * @param ids of groups to be deleted
     * @return statuses of deletion with name and id of groups
     */
    List<GroupDeletionResponseDTO> deleteGroups(List<Long> ids);

    /**
     * Returns page of groups specified by given predicate and pageable
     *
     * @param predicate specifies query to databse
     * @param pageable parameter with information about pagination
     * @return page of groups specified by given predicate and pageable
     */
    PageResultResource<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable);

    /**
     * Returns group with given id
     *
     * @param id of group to be loaded
     * @return group with given id
     * @throws UserAndGroupFacadeException if group with given id could not be found
     */
    GroupDTO getGroup(Long id);

    /**
     * Returns all roles from all registered microservices of group with given id
     *
     * @param id of group
     * @return all roles of group with given id
     * @throws UserAndGroupFacadeException if some of microservice does not return http code 2xx
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    Set<RoleDTO> getRolesOfGroup(Long id);

    /**
     * Assigns role with given roleId in microservice with microserviceID to group with given groupId
     *
     * @param groupId of group
     * @param roleId of role to be assigned to group
     * @throws UserAndGroupFacadeException if group or role could not be found
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    void assignRole(Long groupId, Long roleId);

    /**
     * Returns true if group is internal otherwise false
     *
     * @param id of group
     * @return true if group is internal otherwise false
     * @throws UserAndGroupFacadeException if group was not found
     */
    boolean isGroupInternal(Long id);

    /**
     * Cancel role with given roleId from group with given groupId
     *
     * @param groupId of group
     * @param roleId of role to be assigned to group
     * @throws UserAndGroupFacadeException if group or role could not be found
     */
    void removeRoleFromGroup(Long groupId, Long roleId);

}
