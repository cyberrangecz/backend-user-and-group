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
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long>,
        QuerydslPredicateExecutor<IDMGroup> {

    @EntityGraph(attributePaths = {"roles", "roles.microservice", "users"})
    Optional<IDMGroup> findByName(String name);

    @EntityGraph(attributePaths = {"roles", "roles.microservice", "users"})
    Optional<IDMGroup> findById(Long id);

    @EntityGraph(attributePaths = {"roles", "roles.microservice", "users"})
    Page<IDMGroup> findAll(Predicate predicate, Pageable pageable);

    @Query("SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = :roleType")
    List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

    @Query("SELECT g FROM IDMGroup AS g JOIN FETCH g.roles AS r WHERE r.roleType = 'ROLE_USER_AND_GROUP_ADMINISTRATOR'")
    Optional<IDMGroup> findAdministratorGroup();

    @Query("SELECT g FROM IDMGroup g JOIN FETCH g.users WHERE g.name = :name")
    Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);

    @Query("SELECT CASE WHEN g.externalId IS NULL THEN true ELSE false END FROM IDMGroup g WHERE g.id = :groupId")
    boolean isIDMGroupInternal(@Param("groupId") Long id);

    @Modifying
    @Query("DELETE FROM IDMGroup g WHERE g.expirationDate <= CURRENT_TIMESTAMP")
    void deleteExpiredIDMGroups();

    @Query("SELECT DISTINCT u FROM IDMGroup AS g INNER JOIN g.users AS u WHERE g.id IN :groupsIds")
    Set<User> findUsersOfGivenGroups(@Param("groupsIds") List<Long> groupIds);
}
