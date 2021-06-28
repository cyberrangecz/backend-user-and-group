package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserRepositoryCustom {

    /**
     * Find all users, not in the given {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup} with the given ID.
     *
     * @param groupId  unique identifier of the group whose users will be omitted.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable abstract interface for pagination information.
     * @return returns list of all {@link User}s except those in {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup} with given ID wrapped in {@link Page}
     */
    Page<User> usersNotInGivenGroup(@Param("groupId") Long groupId, Predicate predicate, Pageable pageable);

    /**
     * Find all users in given {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup}s with given IDs.
     *
     * @param groupsIds unique identifiers of groups whose users will be included in the resulting list
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  abstract interface for pagination information
     * @return returns list of all {@link User}s who are in {@link cz.muni.ics.kypo.userandgroup.entities.IDMGroup} with given ID wrapped up in {@link Page}
     */
    Page<User> usersInGivenGroups(@Param("groupsIds") Set<Long> groupsIds, Predicate predicate, Pageable pageable);


    /**
     * Find all users with given {@link Role} with the given ID.
     *
     * @param roleId   unique identifiers of the role.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable abstract interface for pagination information.
     * @return returns list of all {@link User}s who have a {@link Role} with given role ID.
     */
    Page<User> findAllByRoleId(@Param("roleId") Long roleId, Predicate predicate, Pageable pageable);

    /**
     * Find all users with given {@link Role} with the given role type.
     *
     * @param roleType   type of the role.
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable abstract interface for pagination information.
     * @return returns list of all {@link User}s who have a {@link Role} with given role type.
     */
    Page<User> findAllByRoleType(@Param("roleType") String roleType, Predicate predicate, Pageable pageable);

    /**
     * Find all users with given {@link Role} with the given role type and not in given IDs.
     *
     * @param roleType   unique type of the role.
     * @param userIds   ids of the users not included in the result list
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable abstract interface for pagination information.
     * @return returns list of all {@link User}s who have a {@link Role} with given role type and do not have ID in the given ID list.
     */
    Page<User> findAllByRoleAndNotWithIds(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds);


}
