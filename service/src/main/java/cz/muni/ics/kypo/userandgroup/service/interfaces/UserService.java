/*
 *  Project   : Cybernetic Proving Ground
 *
 *  Tool      : Identity Management Service
 *
 *  Author(s) : Filip Bogyai 395959@mail.muni.cz
 *
 *  Date      : 31.5.2016
 *
 *  (c) Copyright 2016 MASARYK UNIVERSITY
 *  All rights reserved.
 *
 *  This software is freely available for non-commercial use under license
 *  specified in following license agreement in LICENSE file. Please review the terms
 *  of the license agreement before using this software. If you are interested in
 *  using this software commercially orin ways not allowed in aforementioned
 *  license, feel free to contact Technology transfer office of the Masaryk university
 *  in order to negotiate ad-hoc license agreement.
 */
package cz.muni.ics.kypo.userandgroup.service.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    /**
     * Gets user with given id from database.
     *
     * @param id of the user to be loaded
     * @return user with given id
     * @throws UserAndGroupServiceException if user was not found
     */
    User get(Long id) throws UserAndGroupServiceException;

    /**
     * Deletes given user from database and returns status of deletion.
     * Return values: SUCCESS - successfully deleted
     *                EXTERNAL_VALID - user is from external source and is not marked as DELETED
     *
     * @param user to be deleted
     * @return status of deletion
     * @throws UserAndGroupServiceException if some error occurred when deleting user
     */
    UserDeletionStatus delete(User user);

    /**
     * Tries to delete users with given ids from database and returns map of users and statuses of their deletion.
     * Statuses: SUCCESS - successfully deleted
     *           EXTERNAL_VALID - user is from external source and is not marked as DELETED
     *           ERROR - when some exception occurred while deleting user
     *           NOT_FOUND - user could not be found
     *
     * @param idsOfUsers ids of users to be deleted
     * @return map of users and statuses of their deletion
     */
    Map<User, UserDeletionStatus> deleteUsers(List<Long> idsOfUsers);

    /**
     * Add/Cancel admin role to user with given id.
     *
     * @param id of user to be changed their admin role.
     * @throws UserAndGroupServiceException when administrator group could not be  found
     */
    void changeAdminRole(Long id) throws UserAndGroupServiceException;

    /**
     * Returns true if user with given id has administrator role, false otherwise.
     *
     * @param id of user checked if they are administrator
     * @throws UserAndGroupServiceException when administrator group could not be  found
     */
    boolean isUserAdmin(Long id) throws UserAndGroupServiceException;

    /**
     * Gets user with given user identity from database.
     *
     * @param login of the user to be loaded
     * @return user with given user identity
     * @throws UserAndGroupServiceException if user with given login could not be found
     */
    User getUserByLogin(String login) throws UserAndGroupServiceException;

    /**
     * Returns all users from database.
     *
     * @return users in database
     */
    Page<User> getAllUsers(Predicate predicate, Pageable pageable);

    /**
     * Returns all users who are not in group with given groupId
     *
     * @param groupId id of group
     * @param pageable parameter with information about pagination
     * @return page of users who are not in group with given groupId
     */
    Page<User> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable);

    /**
     * Returns user with IDM groups from database
     *
     * @param id of the user to be loaded
     * @return user with IDM groups in database
     * @throws UserAndGroupServiceException if some error occurred when loading user with groups
     */
    User getUserWithGroups(Long id) throws UserAndGroupServiceException;

    /**
     * Returns user with IDM groups from database
     *
     * @param login of the user to be loaded
     * @return user with IDM groups in database
     * @throws UserAndGroupServiceException if some error occurred when loading user with groups
     */
    User getUserWithGroups(String login) throws UserAndGroupServiceException;

    /**
     * Returns true if user is internal otherwise false
     *
     * @param id of user
     * @return true if user is internal otherwise false
     * @throws UserAndGroupServiceException if user could not be found
     */
    boolean isUserInternal(Long id) throws UserAndGroupServiceException;

    /**
     * Returns all roles of user with given id
     *
     * @param id of user.
     * @return all roles of user with given id
     * @throws UserAndGroupServiceException if user could not be found
     */
    Set<Role> getRolesOfUser(Long id) throws UserAndGroupServiceException;
}
