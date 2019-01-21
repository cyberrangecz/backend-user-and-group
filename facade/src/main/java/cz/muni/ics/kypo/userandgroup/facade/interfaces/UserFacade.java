package cz.muni.ics.kypo.userandgroup.facade.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;
import java.util.Set;

public interface UserFacade {

    /**
     * Returns page of users specified by given predicate and pageable
     *
     * @param predicate specifies query to database
     * @param pageable parameter with information about pagination
     * @return page of users specified by given predicate and pageable
     * @throws UserAndGroupFacadeException if some of microservice does not return http code 2xx
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Returns user with given id
     *
     * @param id of user to be loaded
     * @return user with given id
     * @throws UserAndGroupFacadeException if user with id could not be found
     * @throws MicroserviceException if client error occurs during getting roles from other microservices, probably due to wrong URL
     */
    UserDTO getUser(Long id) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Returns all users who are not in group with given groupId
     *
     * @param groupId id of group
     * @param pageable parameter with information about pagination
     * @return page of users who are not in group with given groupId
     * @throws UserAndGroupFacadeException if some of microservice does not return http code 2xx
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Deletes user with given id from database and returns status of deletion with user.
     *
     * @param id of user to be deleted
     * @return status of deletion with user
     * @throws UserAndGroupFacadeException if error occurs during deleting user
     */
    UserDeletionResponseDTO deleteUser(Long id) throws UserAndGroupFacadeException;

    /**
     * Deletes users with given ids from database and returns statuses of deletion users
     *
     * @param ids of users to be deleted
     * @return statuses of deletion with users
     */
    List<UserDeletionResponseDTO> deleteUsers(List<Long> ids);

    /**
     * Returns all roles from all registered microservices of user with given id
     *
     * @param id of user
     * @return all roles of user with given id
     * @throws UserAndGroupFacadeException if user was not found
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    Set<RoleDTO> getRolesOfUser(Long id) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Returns info about currently logged in user
     *
     * @param authentication spring's authentication
     * @return information about logged in user
     * @throws UserAndGroupFacadeException if logged in user could not be found in database
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    UserInfoDTO getUserInfo(OAuth2Authentication authentication) throws UserAndGroupFacadeException, MicroserviceException;

    /**
     * Returns true if user is internal otherwise false
     *
     * @param id of user
     * @return true if user is internal otherwise false
     * @throws UserAndGroupFacadeException if user was not found
     */
    boolean isUserInternal(Long id) throws UserAndGroupFacadeException;

    /**
     * Returns basic info about currently logged in user
     *
     * @param authentication spring's authentication
     * @return basic information about logged in user (ids of groups to which is assigned and basic roles)
     * @throws UserAndGroupFacadeException if logged in user could not be found in database
     * @throws MicroserviceException if client error occurs during calling other microservice, probably due to wrong URL
     */
    UserBasicInfoDTO getUserBasicInfo(OAuth2Authentication authentication) throws UserAndGroupFacadeException, MicroserviceException;
}
