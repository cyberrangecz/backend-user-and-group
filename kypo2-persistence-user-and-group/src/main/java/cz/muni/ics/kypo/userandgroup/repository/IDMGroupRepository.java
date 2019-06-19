package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link IDMGroup} instances.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long>,
        QuerydslPredicateExecutor<IDMGroup> {

    /**
     * Find a group by its name.
     *
     * @param name the name of the looking IDMGroup.
     * @return {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(attributePaths = {"roles", "roles.microservice", "users"})
    Optional<IDMGroup> findByName(String name);

    /**
     * Find a group by its ID.
     *
     * @param id ID of looking IDMGroup
     * @return the {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(attributePaths = {"roles", "roles.microservice", "users"})
    Optional<IDMGroup> findById(Long id);

    /**
     * Find all groups.
     *
     * @return list of {@link IDMGroup}s wrapped up in  {@link Page}.
     */
    @EntityGraph(attributePaths = {"roles", "roles.microservice", "users"})
    Page<IDMGroup> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all groups which have given role type.
     *
     * @param roleType the role type of the groups.
     * @return the list of {@link IDMGroup}s.
     */
    @Query("SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = :roleType")
    List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

    /**
     * Find main group with role <i>ADMINISTRATOR</i>.
     *
     * @return the {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query("SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = 'ROLE_USER_AND_GROUP_ADMINISTRATOR'")
    Optional<IDMGroup> findAdministratorGroup();

    /**
     * Find a group by its name and with all users.
     *
     * @param name name of the looking IDMGroup.
     * @return the {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query("SELECT g FROM IDMGroup g JOIN FETCH g.users WHERE g.name = :name")
    Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);

    /**
     * Returns true if the group is internal, false otherwise.
     *
     * @param id the ID of the group.
     * @return true if the IDMGroup is internal, false otherwise.
     */
    @Query("SELECT CASE WHEN g.externalId IS NULL THEN true ELSE false END FROM IDMGroup g WHERE g.id = :groupId")
    boolean isIDMGroupInternal(@Param("groupId") Long id);

    /**
     * Delete expired {@link IDMGroup}.
     */
    @Modifying
    @Query("DELETE FROM IDMGroup g WHERE g.expirationDate <= CURRENT_TIMESTAMP")
    void deleteExpiredIDMGroups();

    /**
     * Find all users from all given {@link IDMGroup}s set of IDs.
     *
     * @param groupIds IDs of groups.
     * @return the set of {@link User}s.
     */
    @Query("SELECT DISTINCT u FROM IDMGroup AS g INNER JOIN g.users AS u WHERE g.id IN :groupsIds")
    Set<User> findUsersOfGivenGroups(@Param("groupsIds") List<Long> groupIds);
}
