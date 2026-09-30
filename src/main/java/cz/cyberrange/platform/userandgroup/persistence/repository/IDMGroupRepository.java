package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.QIDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link IDMGroup} instances. */
@Repository
public interface IDMGroupRepository
    extends JpaRepository<IDMGroup, Long>,
        QuerydslPredicateExecutor<IDMGroup>,
        QuerydslBinderCustomizer<QIDMGroup> {

  /**
   * Binds every string-typed property so QueryDSL predicates match its values case-insensitively.
   *
   * @param querydslBindings bindings being customized for this repository
   * @param qIDMGroup unused
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QIDMGroup qIDMGroup) {
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
   * Finds the group with the given name, loading its users and roles together with each role's
   * microservice.
   *
   * @param name name to match
   * @return the matching group, or an empty Optional when no group has that name
   */
  @EntityGraph(value = "IDMGroup.usersRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
  Optional<IDMGroup> findByName(String name);

  /**
   * Finds the group with the given name that has at least one role, loading its roles.
   *
   * @param name name to match
   * @return the matching group, or an empty Optional when no group with that name has any role
   */
  Optional<IDMGroup> findByNameWithRoles(@Param("name") String name);

  /**
   * Finds the group with the given ID, loading its users and roles together with each role's
   * microservice.
   *
   * @param id id to match
   * @return the matching group, or an empty Optional when no group has that ID
   */
  @EntityGraph(value = "IDMGroup.usersRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
  Optional<IDMGroup> findById(Long id);

  /**
   * Finds every group whose ID is in the given list, loading its users.
   *
   * @param groupIds ids to match
   * @return the matching groups, loaded with users; empty list when none match
   */
  @EntityGraph(value = "IDMGroup.users", type = EntityGraph.EntityGraphType.FETCH)
  List<IDMGroup> findByIdIn(List<Long> groupIds);

  /**
   * Finds every group that matches the given predicate.
   *
   * @param predicate filter applied to the groups
   * @param pageable page and sort request
   * @return the matching page of groups; empty page when none match
   */
  Page<IDMGroup> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds every group that has a role with the given role type, loading its roles.
   *
   * @param roleType role type to match
   * @return the matching groups with their roles loaded; empty list when none match
   */
  List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

  /**
   * Finds the group whose roles include the administrator role, loading its roles.
   *
   * @return the matching group, or an empty Optional when no group has that role
   */
  Optional<IDMGroup> findAdministratorGroup();

  /**
   * Finds the group with the given name, loading its users.
   *
   * @param name name to match
   * @return the matching group, or an empty Optional when no group has that name
   */
  Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);

  /** Deletes every group whose expiration date is at or before the current time. */
  @Modifying
  void deleteExpiredIDMGroups();

  /**
   * Finds every distinct user belonging to any of the given groups.
   *
   * @param groupIds ids of the groups to match
   * @return the matching users; empty set when none match
   */
  Set<User> findUsersOfGivenGroups(@Param("groupsIds") List<Long> groupIds);

  /**
   * Checks whether a group with the given name exists.
   *
   * @param groupName name to match
   * @return true when a group has that name, false otherwise
   */
  boolean existsByName(String groupName);
}
