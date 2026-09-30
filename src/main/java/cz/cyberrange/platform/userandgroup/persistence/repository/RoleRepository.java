package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.QRole;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link Role} instances. */
@Repository
public interface RoleRepository
    extends JpaRepository<Role, Long>,
        RoleRepositoryCustom,
        QuerydslPredicateExecutor<Role>,
        QuerydslBinderCustomizer<QRole> {

  /**
   * Binds every string-typed property so QueryDSL predicates match its values case-insensitively.
   *
   * @param querydslBindings bindings being customized for this repository
   * @param qRole unused
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QRole qRole) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Finds the role with the given role type, loading its microservice.
   *
   * @param roleType role type to match
   * @return the matching role, or an empty Optional when no role has that type
   */
  @EntityGraph(value = "Role.microservice", type = EntityGraph.EntityGraphType.FETCH)
  Optional<Role> findByRoleType(String roleType);

  /**
   * Finds every role that matches the given predicate, loading each role's microservice.
   *
   * @param predicate filter applied to the roles
   * @param pageable page and sort request
   * @return the matching page of roles; empty page when none match
   */
  @EntityGraph(value = "Role.microservice", type = EntityGraph.EntityGraphType.FETCH)
  Page<Role> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds the role with the given ID, loading its microservice.
   *
   * @param id id to match
   * @return the matching role, or an empty Optional when no role has that ID
   */
  Optional<Role> findById(@Param("id") Long id);

  /**
   * Checks whether a role with the given role type exists.
   *
   * @param roleType role type to match
   * @return true when a role has that type, false otherwise
   */
  boolean existsByRoleType(String roleType);

  /**
   * Finds every role belonging to the microservice with the given name, loading each role's
   * microservice.
   *
   * @param microserviceName name of the {@link Microservice} to match
   * @return the matching roles; empty set when none match
   */
  Set<Role> getAllRolesByMicroserviceName(@Param("microserviceName") String microserviceName);

  /**
   * Finds the role of the given microservice that belongs to the group named DEFAULT-GROUP.
   *
   * @param microserviceName name of the microservice to match
   * @return the matching role, or an empty Optional when none matches
   */
  Optional<Role> findDefaultRoleOfMicroservice(@Param("microserviceName") String microserviceName);

  /**
   * Finds every role whose role type is in the given set.
   *
   * @param roleTypes role types to match
   * @return the matching roles; empty set when none match
   */
  Set<Role> getAllByRoleTypeIn(@Param("roleTypes") Set<String> roleTypes);
}
