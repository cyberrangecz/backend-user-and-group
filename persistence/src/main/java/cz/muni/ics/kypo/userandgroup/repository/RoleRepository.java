package cz.muni.ics.kypo.userandgroup.repository;

import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>,
        QuerydslPredicateExecutor<Role> {

    Optional<Role> findByRoleType(RoleType roleType);

    boolean existsByRoleType(RoleType roleType);
}
