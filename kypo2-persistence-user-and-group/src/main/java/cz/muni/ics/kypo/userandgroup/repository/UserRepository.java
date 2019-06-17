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
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        QuerydslPredicateExecutor<User> {

    @EntityGraph(attributePaths = {"groups", "groups.roles", "groups.roles.microservice"})
    Optional<User> findByLogin(String login);

    @EntityGraph(attributePaths = {"groups", "groups.roles", "groups.roles.microservice"})
    Optional<User> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"groups", "groups.roles", "groups.roles.microservice"})
    Page<User> findAll(Predicate predicate, Pageable pageable);

    @Query("SELECT u.login FROM User u WHERE u.id = :userId")
    Optional<String> getLogin(@Param("userId") Long id);

    @Query("SELECT CASE WHEN u.externalId IS NULL THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean isUserInternal(@Param("userId") Long id);

    @Query("SELECT r FROM User u INNER JOIN u.groups g INNER JOIN g.roles r JOIN FETCH r.microservice WHERE u.id = :userId")
    Set<Role> getRolesOfUser(@Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN FETCH u.groups WHERE u.login = :login")
    Optional<User> getUserByLoginWithGroups(@Param("login") String login);

    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.groups g LEFT JOIN FETCH g.roles r LEFT JOIN FETCH r.microservice WHERE (SELECT g FROM IDMGroup g WHERE g.id = :groupId) NOT MEMBER OF u.groups",
    countQuery = "SELECT u FROM User u LEFT OUTER JOIN u.groups g  LEFT OUTER JOIN g.roles r LEFT OUTER JOIN r.microservice WHERE (SELECT g FROM IDMGroup g WHERE g.id = :groupId)  NOT MEMBER OF u.groups")
    Page<User> usersNotInGivenGroup(@Param("groupId") Long groupId, Pageable pageable);

    @Query(value = "SELECT u FROM User u JOIN FETCH u.groups g WHERE g.id IN :groupsIds",
            countQuery = "SELECT u FROM User u INNER JOIN u.groups g WHERE g.id IN :groupsIds")
    Page<User> usersInGivenGroups(@Param("groupsIds") Set<Long> groupsIds, Pageable pageable);

    @Query(value = "SELECT u FROM User u JOIN FETCH u.groups g JOIN FETCH g.roles r JOIN FETCH r.microservice WHERE r.id = :roleId",
            countQuery = "SELECT u FROM User u INNER JOIN u.groups g  INNER JOIN g.roles r INNER JOIN r.microservice WHERE r.id = :roleId")
    Page<User> findAllByRoleId(@Param("roleId") Long roleId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN FETCH u.groups WHERE u.id = :userId")
    Optional<User> getUserByIdWithGroups(@Param("userId") Long userId);

    @Query(value = "SELECT u FROM User u WHERE u.login IN :logins")
    Set<User> findAllWithGivenLogins(@Param("logins") Set<String> logins);
}
