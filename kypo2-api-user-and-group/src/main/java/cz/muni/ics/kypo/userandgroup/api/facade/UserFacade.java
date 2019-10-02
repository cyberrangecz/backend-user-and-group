package cz.muni.ics.kypo.userandgroup.api.facade;

import cz.muni.ics.kypo.userandgroup.api.config.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.querydsl.core.types.Predicate;

import java.util.List;
import java.util.Set;

/**
 * The interface for the User facade layer.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public interface UserFacade {

    /**
     * Gets user with given ID from the database.
     *
     * @param userId the ID of the user to be loaded.
     * @return the user encapsulated in {@link UserDTO}.
     * @throws UserAndGroupFacadeException if user was not found.
     */
    UserDTO getUser(Long userId);

    /**
     * Deletes user with given ID from database and returns status of deletion.
     *
     * @param userId the ID of user to be deleted.
     * @return the status of the deletion {@link UserDeletionStatusDTO}.
     * @throws UserAndGroupFacadeException if an error occurs during deleting user.
     */
    UserDeletionResponseDTO deleteUser(Long userId);

    /**
     * Deletes users with given IDs from the database and returns statuses of user deletions.
     *
     * @param userIds list of IDs of users to be deleted.
     * @return list of statuses about user deletion.
     */
    List<UserDeletionResponseDTO> deleteUsers(List<Long> userIds);

    /**
     * Gets a page of users specified by given predicate and pageable.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of the {@link UserDTO}s wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable);

    /**
     * Gets a page of users who are not in the group with given a group ID.
     *
     * @param groupId  the ID of the group.
     * @param pageable pageable parameter with information about pagination.
     * @return a list of {@link UserDTO}s who are not in the group with a given group ID wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable);

    /**
     * Check if the user is internal or not.
     *
     * @param id the ID of the user.
     * @return true if tthe user is internal, otherwise false.
     * @throws UserAndGroupFacadeException if the user was not found.
     */
    boolean isUserInternal(Long id);

    /**
     * Returns all roles from all registered microservices of the user with the given ID.
     *
     * @param userId the ID of the user.
     * @return set of the {@link RoleDTO}s of the user with the given ID.
     * @throws UserAndGroupFacadeException if the user was not found.
     */
    Set<RoleDTO> getRolesOfUser(Long userId);

    /**
     * Returns info about currently logged in user.
     *
     * @param authentication spring's authentication.
     * @return encapsulated info about logged in user in {@link UserDTO}.
     * @throws UserAndGroupFacadeException if logged in user could not be found in database.
     */
    UserDTO getUserInfo(OAuth2Authentication authentication);

    /**
     * Returns a page of users in given groups specified by pageable.
     *
     * @param groupsIds IDs of groups to which are users assigned.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of the {@link UserForGroupsDTO}s wrapped up in  {@link PageResultResource}.
     */
    PageResultResource<UserForGroupsDTO> getUsersInGroups(Set<Long> groupsIds, Pageable pageable);

    /**
     * Returns page of users specified by given role ID and pageable.
     *
     * @param roleId the ID of the role to get users for.
     * @param pageable pageable parameter with information about pagination.
     * @return a list of the {@link UserDTO}s specified by given and pageable.
     * @throws UserAndGroupFacadeException if the role could not be found in DB.
     */
    PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Pageable pageable);

    /**
     * Returns a page of users specified by given role type, predicate and pageable.
     *
     * @param roleType a type of the role to get users for.
     * @param pageable pageable parameter with information about pagination.
     * @return a list of the {@link UserDTO}s specified by given predicate and pageable.
     * @throws UserAndGroupFacadeException if the role could not be found in DB.
     */
    PageResultResource<UserDTO> getUsersWithGivenRole(String roleType, Pageable pageable);

    /**
     * Gets users with given ids.
     *
     * @param ids ids of users.
     * @return set of {@link UserDTO}s with given ids wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getUsersWithGivenIds(Set<Long> ids, Pageable pageable);

    /**
     *  Returns a page of users specified by given role type and not with given ids.
     *
     * @param roleType a type of the role to get users for.
     * @param ids ids of users excluded from the result page.
     * @return set of {@link UserDTO}s with given role type and not with given ids wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getUsersWithGivenRoleAndNotWithGivenIds(String roleType, Set<Long> ids, Pageable pageable);
}
