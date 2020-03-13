package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The interface for the User service layer.
 */
public interface UserService {

    /**
     * Gets user with given ID from the database.
     *
     * @param id the ID of the user to be loaded.
     * @return the {@link User} with the given ID.
     * @throws EntityNotFoundException if user was not found.
     */
    User getUserById(Long id);

    /**
     * Get users with given IDs.
     *
     * @param userIds given user IDs.
     * @return List of User entities based on the given IDs.
     */
    List<User> getUsersByIds(List<Long> userIds);

    /**
     * Delete given user from database.
     *
     * @param user user to be deleted.
     */
    void deleteUser(User user);

    /**
     * Create new user.
     *
     * @param user new user info.
     * @return newly created user.
     */
    User createUser(User user);

    /**
     * Update user.
     *
     * @param user update user.
     * @return newly created user.
     */
    User updateUser(User user);

    /**
     * Add/Cancel admin role to the user with the given ID.
     *
     * @param userId the ID of the user whose admin role should be changed.
     * @throws EntityNotFoundException when the administrator group could not be found.
     */
    void changeAdminRole(Long userId);

    /**
     * Check if the user with the given ID is administrator or not.
     *
     * @param userId the ID of the user to be checked.
     * @return true if the {@link User} with the given ID is administrator, false otherwise.
     * @throws EntityNotFoundException when the administrator group could not be found.
     */
    boolean isUserAdmin(Long userId);

    /**
     * Gets user with given user login from the database.
     *
     * @param login login of the user to be loaded.
     * @param iss   issuer - URI of the oidc provider used to authenticate user.
     * @return the {@link User} with given user login.
     * @throws EntityNotFoundException if the user with the given login could not be found.
     */
    Optional<User> getUserByLoginAndIss(String login, String iss);

    /**
     * Gets all users from the database.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return list of {@link User}s wrapped up in {@link Page}.
     */
    Page<User> getAllUsers(Predicate predicate, Pageable pageable);

    /**
     * Gets all users, not in a given IDMGroup with the given ID.
     *
     * @param groupId   the ID of the IDMGroup.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return list of users who are not in the {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup} with the given group ID and wrapped up in {@link Page}.
     */
    Page<User> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable);

    /**
     * Gets all users in given groups.
     *
     * @param groupsIds set of IDs of groups to which are users assigned.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return list of {@link User}s in given groups wrapped up in {@link Page}.
     */
    Page<User> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable);

    /**
     * Gets users with IDMGroups from the database.
     *
     * @param id the ID of the user to be loaded.
     * @return the user with loaded {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup}s from the database.
     * @throws EntityNotFoundException if user could not be found.
     */
    User getUserWithGroups(Long id);

    /**
     * Get all users with the {@link Role} with a given role ID.
     *
     * @param roleId    the ID of the role to getGroupById users for.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of {@link User}s specified by the role wrapped up in {@link Page}.
     * @throws EntityNotFoundException if the role could not be found
     */
    Page<User> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable);

    /**
     * Get all users with the {@link Role} with a given role type.
     *
     * @param roleType  the name of the role to getGroupById users for.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return a list of {@link User}s specified by the role wrapped up in {@link Page}.
     * @throws EntityNotFoundException if the role could not be found.
     */
    Page<User> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable);

    /**
     * Gets users with IDMGroups from the database.
     *
     * @param login the login of the user to be loaded.
     * @param iss   issuer - URI of the oidc provider
     * @return the user with loaded {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup}s from the database.
     * @throws EntityNotFoundException if user could not be found.
     */
    User getUserWithGroups(String login, String iss);

    /**
     * Gets all {@link Role}s of users with the given ID.
     *
     * @param id the ID of the user.
     * @return set of all roles of users with the given ID.
     * @throws EntityNotFoundException if the user could not be found.
     */
    Set<Role> getRolesOfUser(Long id);

    /**
     * Gets users with a given set of ids.
     *
     * @param ids set of ids.
     * @return set of {@link User}s with ids wrapped up in {@link Page}.
     */
    Page<User> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate);


    /**
     * Returns a page of users specified by given role type and not with given ids.
     *
     * @param roleType a type of the role to getGroupById users for.
     * @param userIds  ids of users excluded from the result page.
     * @return set of {@link UserDTO}s with given role type and not with given ids wrapped up in {@link Page}.
     */
    Page<User> getUsersWithGivenRoleAndNotWithGivenIds(String roleType, Set<Long> userIds, Predicate predicate, Pageable pageable);
}
