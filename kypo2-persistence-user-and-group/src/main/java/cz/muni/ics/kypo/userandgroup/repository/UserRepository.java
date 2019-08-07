package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import io.micrometer.core.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link User} instances.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        QuerydslPredicateExecutor<User> {

    /**
     * Find the user by his login and oidc provider used to authenticate user.
     *
     * @param login unique login of the user.
     * @param iss the URI of the oidc provider
     * @return the {@link User} instance with a given login if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(attributePaths = {"groups", "groups.roles", "groups.roles.microservice"})
    Optional<User> findByLoginAndIss(String login, String iss);

    /**
     * Find the user by his ID.
     *
     * @param id unique identifier of the user.
     * @return the {@link User} instance with a given ID if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(attributePaths = {"groups", "groups.roles", "groups.roles.microservice"})
    Optional<User> findById(@NonNull Long id);

    /**
     * Find all users.
     *
     * @return list of {@link User}s wrapped up in {@link Page}.
     */
    @EntityGraph(attributePaths = {"groups", "groups.roles", "groups.roles.microservice"})
    Page<User> findAll(Predicate predicate, Pageable pageable);

    /**
     * Gets the login of the user by the given ID of the user.
     *
     * @param id the unique identifier of the user.
     * @return the login of the {@link User} with a given ID if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query("SELECT u.login FROM User u WHERE u.id = :userId")
    Optional<String> getLogin(@Param("userId") Long id);

    /**
     * Returns true if the user is internal, false otherwise.
     *
     * @param id the unique identifier of the user.
     * @return true if the {@link User} is internal, false otherwise.
     */
    @Query("SELECT CASE WHEN u.externalId IS NULL THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean isUserInternal(@Param("userId") Long id);

    /**
     * Gets roles of the user.
     *
     * @param userId the unique identifier of the user.
     * @return the {@link Role}s of the user with the given ID.
     */
    @Query("SELECT r FROM User u INNER JOIN u.groups g INNER JOIN g.roles r JOIN FETCH r.microservice WHERE u.id = :userId")
    Set<Role> getRolesOfUser(@Param("userId") Long userId);

    /**
     * Find the user by his login also with groups in which he is.
     *
     * @param login unique login of the user.
     * @return the {@link User} instance with groups if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query("SELECT u FROM User u JOIN FETCH u.groups WHERE u.login = :login AND u.iss = :iss")
    Optional<User> getUserByLoginWithGroups(@Param("login") String login, @Param("iss") String iss);

    /**
     * Find all users, not in the given {@link cz.muni.ics.kypo.userandgroup.model.IDMGroup} with the given ID.
     *
     * @param groupId  unique identifier of the group whose users will be omitted.
     * @param pageable abstract interface for pagination information.
     * @return returns list of all {@link User}s except those in {@link cz.muni.ics.kypo.userandgroup.model.IDMGroup} with given ID wrapped in {@link Page}
     */
    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.groups g LEFT JOIN FETCH g.roles r LEFT JOIN FETCH r.microservice WHERE (SELECT g FROM IDMGroup g WHERE g.id = :groupId) NOT MEMBER OF u.groups",
        countQuery = "SELECT COUNT(u) FROM User u LEFT OUTER JOIN u.groups g  LEFT OUTER JOIN g.roles r LEFT OUTER JOIN r.microservice WHERE (SELECT g FROM IDMGroup g WHERE g.id = :groupId)  NOT MEMBER OF u.groups")
    Page<User> usersNotInGivenGroup(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * Find all users in given {@link cz.muni.ics.kypo.userandgroup.model.IDMGroup}s with given IDs.
     *
     * @param groupsIds unique identifiers of groups whose users will be included in the resulting list
     * @param pageable abstract interface for pagination information
     * @return returns list of all {@link User}s who are in {@link cz.muni.ics.kypo.userandgroup.model.IDMGroup} with given ID wrapped up in {@link Page}
     */
    @Query(value = "SELECT u FROM User u JOIN FETCH u.groups g WHERE g.id IN :groupsIds",
            countQuery = "SELECT COUNT(u) FROM User u INNER JOIN u.groups g WHERE g.id IN :groupsIds")
    Page<User> usersInGivenGroups(@Param("groupsIds") Set<Long> groupsIds, Pageable pageable);

    /**
     * Find all users with given {@link Role} with the given ID.
     *
     * @param roleId unique identifiers of the role.
     * @param pageable abstract interface for pagination information.
     * @return returns list of all {@link User}s who have a {@link Role} with given role ID.
     */
    @Query(value = "SELECT u FROM User u JOIN FETCH u.groups g JOIN FETCH g.roles r JOIN FETCH r.microservice WHERE r.id = :roleId",
            countQuery = "SELECT COUNT(u) FROM User u INNER JOIN u.groups g  INNER JOIN g.roles r INNER JOIN r.microservice WHERE r.id = :roleId")
    Page<User> findAllByRoleId(@Param("roleId") Long roleId, Pageable pageable);

    /**
     * Find users by his ID also with groups in which he is.
     *
     * @param userId unique identifier of the user.
     * @return the {@link User} instance with groups if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query("SELECT u FROM User u JOIN FETCH u.groups WHERE u.id = :userId")
    Optional<User> getUserByIdWithGroups(@Param("userId") Long userId);

    /**
     * Find all users by a set of ids.
     *
     * @param ids set of ids of users who we are looking for.
     * @return returns set of all {@link User}s whose ids are in a given set of ids.
     */
    @Query(value = "SELECT u FROM User u WHERE u.id IN :ids")
    Set<User> findAllWithGivenIds(@Param("ids") Set<Long> ids);
}
