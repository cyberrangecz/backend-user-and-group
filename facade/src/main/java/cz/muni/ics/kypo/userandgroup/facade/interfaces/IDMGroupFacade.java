package cz.muni.ics.kypo.userandgroup.facade.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Set;

public interface IDMGroupFacade {

    /**
     * Creates IDMGroup with information from given newGroupDTO and returns created group.
     *
     * @param newGroupDTO group to be created
     * @return created group
     * @throws UserAndGroupFacadeException if some of group could not be found from given list of group ids
     */
    GroupDTO createGroup(NewGroupDTO newGroupDTO) throws UserAndGroupFacadeException;

    /**
     * Updates given IDM group in database.
     *
     * @param updateGroupDTO group to be updated
     * @throws ExternalSourceException if group with given group is external and cannot be edited
     */
    void updateGroup(UpdateGroupDTO updateGroupDTO) throws ExternalSourceException;

    /**
     * Removes members of group with given userIds from the group
     *
     * @param groupId id of group from which given users should be removed
     * @param userIds ids of users to be removed from given group
     * @throws UserAndGroupFacadeException if some group or user could not be found
     * @throws ExternalSourceException if group with given group is external and cannot be edited
     */
    void removeUsers(Long groupId, List<Long> userIds) throws UserAndGroupFacadeException, ExternalSourceException;

    /**
     * Adds users and users from groups to group with given groupId in input parameter addMember
     *
     * @param addUsers parameter containing users to be added, groups which users will be added to group with groupId which is also specify in this parameter
     * @throws UserAndGroupFacadeException if some group or user could not be found
     * @throws ExternalSourceException if group with given group is external and cannot be edited
     */
    void addUsers(AddUsersToGroupDTO addUsers) throws UserAndGroupFacadeException, ExternalSourceException;

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
    GroupDTO getGroup(Long id) throws UserAndGroupFacadeException;

    /**
     * Returns all roles from all registered microservices of group with given id
     *
     * @param id of group
     * @return all roles of group with given id
     * @throws UserAndGroupFacadeException if some of microservice does not return http code 2xx
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    Set<RoleDTO> getRolesOfGroup(Long id) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Assigns role with given roleType to group with given groupId in this microservice
     *
     * @param groupId of group
     * @param roleType to be assigned to group
     * @throws UserAndGroupFacadeException if group or one of the main role could not be find
     */
    void assignRole(Long groupId, RoleType roleType) throws UserAndGroupFacadeException;

    /**
     * Assigns role with given roleId in microservice with microserviceID to group with given groupId
     *
     * @param groupId of group
     * @param roleId of role to be assigned to group from specific microservice
     * @param microserviceId of microservice from which is role
     * @throws UserAndGroupFacadeException if microservice with given microserviceId could not be found
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    void assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Returns true if group is internal otherwise false
     *
     * @param id of group
     * @return true if group is internal otherwise false
     * @throws UserAndGroupFacadeException if group was not found
     */
    boolean isGroupInternal(Long id) throws UserAndGroupFacadeException;
}
