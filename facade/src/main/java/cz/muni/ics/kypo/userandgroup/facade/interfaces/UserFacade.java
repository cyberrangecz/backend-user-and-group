package cz.muni.ics.kypo.userandgroup.facade.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
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
     */
    PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable);

    /**
     * Returns user with given id
     *
     * @param id of user to be loaded
     * @return user with given id
     */
    UserDTO getUser(Long id);

    /**
     * Returns all users who are not in group with given groupId
     *
     * @param groupId id of group
     * @param pageable parameter with information about pagination
     * @return page of users who are not in group with given groupId
     */
    PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable);

    /**
     * Creates user with information from given newUserDTO and returns created user.
     *
     * @param newUserDTO user to be created
     * @return created user
     */
    UserDTO createUser(NewUserDTO newUserDTO);

    /**
     * Updates given user in database.
     *
     * @param updateUserDTO user to be updated
     * @return updated user
     */
    UserDTO updateUser(UpdateUserDTO updateUserDTO);

    /**
     * Deletes user with given id from database and returns status of deletion with user.
     *
     * @param id of user to be deleted
     * @return status of deletion with user
     */
    UserDeletionResponseDTO deleteUser(Long id);

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
     */
    Set<RoleDTO> getRolesOfUser(Long id);

    /**
     * Returns info about currently logged in user
     *
     * @param authentication spring's authentication
     * @return information about logged in user
     */
    UserInfoDTO getUserInfo(OAuth2Authentication authentication);

    /**
     * Returns true if user is internal otherwise false
     *
     * @param id of user
     * @return true if user is internal otherwise false
     * @throws UserAndGroupFacadeException if user was not found
     */
    boolean isUserInternal(Long id) throws UserAndGroupFacadeException;
}
