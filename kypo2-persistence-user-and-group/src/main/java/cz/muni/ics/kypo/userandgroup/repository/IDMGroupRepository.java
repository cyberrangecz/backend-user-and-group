package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long>,
        QuerydslPredicateExecutor<IDMGroup> {

    @EntityGraph(attributePaths = {"roles", "users"})
    Optional<IDMGroup> findByName(String name);

    @EntityGraph(attributePaths = {"roles", "users"})
    Optional<IDMGroup> findById(Long id);

    @Query("SELECT g FROM IDMGroup AS g INNER JOIN g.roles AS r WHERE r.roleType = :roleType")
    List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

    @Query("SELECT g FROM IDMGroup AS g INNER JOIN g.roles AS r WHERE r.roleType = 'ROLE_USER_AND_GROUP_ADMINISTRATOR'")
    Optional<IDMGroup> findAdministratorGroup();

    @Query("SELECT g FROM IDMGroup g JOIN FETCH g.users WHERE g.name = :name")
    Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);

    @Query("SELECT CASE WHEN g.externalId IS NULL THEN true ELSE false END FROM IDMGroup g WHERE g.id = :groupId")
    boolean isIDMGroupInternal(@Param("groupId") Long id);
}
