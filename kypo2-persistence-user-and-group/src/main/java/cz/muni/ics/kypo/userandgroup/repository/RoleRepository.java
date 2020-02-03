package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.muni.ics.kypo.userandgroup.model.QRole;
import cz.muni.ics.kypo.userandgroup.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link Role} instances.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, QuerydslPredicateExecutor<Role>, QuerydslBinderCustomizer<QRole> {

    /**
     * That method is used to make the query dsl string values case insensitive
     *
     * @param querydslBindings
     * @param qRole
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QRole qRole) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

    /**
     * Find the role by role type.
     *
     * @param roleType the name of the role.
     * @return {@link Role} if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(attributePaths = {"microservice"})
    Optional<Role> findByRoleType(String roleType);

    /**
     * Find all the roles.
     *
     * @return list of {@link Role}s wrapped by {@link Page}.
     */
    @EntityGraph(attributePaths = {"microservice"})
    Page<Role> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find the role by given ID.
     *
     * @param id the ID of the looking Role.
     * @return the {@link Role} if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query(value = "SELECT r FROM Role r JOIN FETCH r.microservice WHERE r.id= :id")
    Optional<Role> findById(@Param("id") Long id);

    /**
     * Returns true if the role with given role type exists, false otherwise.
     *
     * @param roleType the name of the role.
     * @return true if the role with given role type exists, false otherwise.
     */
    boolean existsByRoleType(String roleType);

    /**
     * Gets all roles by microservice name.
     *
     * @param microserviceName the name of {@link cz.muni.ics.kypo.userandgroup.model.Microservice}
     * @return the set of {@link Role}s
     */
    @Query(value = "SELECT r FROM Role r JOIN FETCH r.microservice ms WHERE ms.name = :microserviceName")
    Set<Role> getAllRolesByMicroserviceName(@Param("microserviceName") String microserviceName);

    /**
     * Find the default role by microservice name.
     *
     * @param microserviceName the name of the microservice.
     * @return {@link Role} if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @Query(value = "SELECT r FROM Role r INNER JOIN r.microservice m WHERE m.name = :microserviceName AND r IN (SELECT r FROM IDMGroup g INNER JOIN g.roles r WHERE g.name = 'DEFAULT-GROUP') ")
    Optional<Role> findDefaultRoleOfMicroservice(@Param("microserviceName") String microserviceName);

}
