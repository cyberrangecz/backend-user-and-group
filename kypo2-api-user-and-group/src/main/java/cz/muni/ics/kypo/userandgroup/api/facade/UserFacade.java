package cz.muni.ics.kypo.userandgroup.api.facade;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserUpdateDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * The interface for the User facade layer.
 */
public interface UserFacade {

    /**
     * Gets user with given ID from the database.
     *
     * @param userId the ID of the user to be loaded.
     * @return the user encapsulated in {@link UserDTO}.
     * @throws UserAndGroupFacadeException if user was not found.
     */
    UserDTO getUserById(Long userId);

    /**
     * Returns info about currently logged in user.
     *
     * @param sub user idenfitication (usually his login).
     * @param iss OIDC provider identification (e.g., MUNI OIDC).
     * @return encapsulated info about logged in user in {@link UserDTO}.
     * @throws UserAndGroupFacadeException if logged in user could not be found in database.
     */
    UserDTO getUserInfo(String sub, String iss);

    /**
     * Create or update user based on OIDC issuer
     *
     * @param sub
     * @param iss
     * @param introspectionResponse
     * @return
     */
    UserDTO createOrUpdateOrGetOIDCUser(String sub, String iss, JsonObject introspectionResponse);

    /**
     * Deletes user with given ID from database.
     *
     * @param userId the ID of user to be deleted.
     * @throws UserAndGroupFacadeException if an error occurs during deleting user.
     */
    void deleteUser(Long userId);

    /**
     * Deletes users with given IDs from the database.
     *
     * @param userIds list of IDs of users to be deleted.
     */
    void deleteUsers(List<Long> userIds);

    /**
     * Update user.
     *
     * @param userUpdateDTO update user.
     * @return newly created user.
     */
    UserDTO updateUser(UserUpdateDTO userUpdateDTO);

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
     * @param groupId   the ID of the group.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of {@link UserDTO}s who are not in the group with a given group ID wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable);

    /**
     * Returns a page of users in given groups specified by pageable.
     *
     * @param groupsIds IDs of groups to which are users assigned.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of the {@link UserForGroupsDTO}s wrapped up in  {@link PageResultResource}.
     */
    PageResultResource<UserForGroupsDTO> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable);

    /**
     * Returns page of users specified by given role ID and pageable.
     *
     * @param roleId    the ID of the role to get users for.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of the {@link UserDTO}s specified by given and pageable.
     * @throws UserAndGroupFacadeException if the role could not be found in DB.
     */
    PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable);

    /**
     * Returns a page of users specified by given role type, predicate and pageable.
     *
     * @param roleType  a type of the role to get users for.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of the {@link UserDTO}s specified by given predicate and pageable.
     * @throws UserAndGroupFacadeException if the role could not be found in DB.
     */
    PageResultResource<UserDTO> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable);

    /**
     * Returns all roles from all registered microservices of the user with the given ID.
     *
     * @param userId the ID of the user.
     * @return set of the {@link RoleDTO}s of the user with the given ID.
     * @throws UserAndGroupFacadeException if the user was not found.
     */
    Set<RoleDTO> getRolesOfUser(Long userId);

    /**
     * Gets users with given ids.
     *
     * @param ids ids of users.
     * @return set of {@link UserDTO}s with given ids wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate);

    /**
     * Gets a page of users specified by given predicate and pageable.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of the {@link UserDTO}s wrapped up in {@link PageResultResource}.
     */
    PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds);
}
