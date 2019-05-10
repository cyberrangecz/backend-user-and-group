package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.Role;
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

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>,
        QuerydslPredicateExecutor<Role> {

    @EntityGraph(attributePaths = {"microservice"})
    Optional<Role> findByRoleType(String roleType);

    @EntityGraph(attributePaths = {"microservice"})
    Page<Role> findAll(Predicate predicate, Pageable pageable);

    boolean existsByRoleType(String roleType);

    @Query(value = "SELECT r FROM Role r JOIN FETCH r.microservice ms WHERE ms.name = :microserviceName" )
    Set<Role> getAllRolesByMicroserviceName(@Param("microserviceName") String microserviceName);
}
