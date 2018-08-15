package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long>,
        QuerydslPredicateExecutor<IDMGroup> {

    Optional<IDMGroup> findByName(String name);

    Page<IDMGroup> findAllByName(String name, Pageable pageable);

    @Query("SELECT g FROM IDMGroup AS g INNER JOIN g.roles AS r WHERE r.roleType = :roleType")
    List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

    @Query("SELECT g FROM IDMGroup AS g INNER JOIN g.roles AS r WHERE r.roleType = 'ADMINISTRATOR'")
    Optional<IDMGroup> findAdministratorGroup();

    @Query("SELECT r FROM IDMGroup g INNER JOIN g.roles r WHERE g.id = :groupId")
    Set<Role> getRolesOfGroup(@Param("groupId") Long id);

    @EntityGraph(value = "IDMGroup.users", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT g FROM IDMGroup g WHERE g.name = :name")
    Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);

    @Query("SELECT CASE WHEN g.externalId IS NULL THEN true ELSE false END FROM IDMGroup g WHERE g.id = :groupId")
    boolean isIDMGroupInternal(@Param("groupId") Long id);
}
