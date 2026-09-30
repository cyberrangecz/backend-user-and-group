package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.userandgroup.persistence.entity.QUser;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import java.util.Collection;
import java.util.List;
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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link User} instances. */
@Repository
public interface UserRepository
    extends JpaRepository<User, Long>,
        UserRepositoryCustom,
        QuerydslPredicateExecutor<User>,
        QuerydslBinderCustomizer<QUser> {

  /**
   * Binds every string-typed property so QueryDSL predicates match its values case-insensitively.
   *
   * @param querydslBindings bindings being customized for this repository
   * @param qUser unused
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QUser qUser) {
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
   * Finds the user with the given sub and issuer, loading the user's groups together with each
   * group's roles and each role's microservice.
   *
   * @param sub subject identifier to match
   * @param iss issuer to match
   * @return the matching user, or an empty Optional when no user has that sub and issuer
   */
  @EntityGraph(value = "User.groupsRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
  Optional<User> findBySubAndIss(String sub, String iss);

  /**
   * Finds the user with the given ID, loading the user's groups together with each group's roles
   * and each role's microservice.
   *
   * @param id id to match
   * @return the matching user, or an empty Optional when no user has that ID
   */
  @EntityGraph(value = "User.groupsRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
  Optional<User> findById(@NonNull Long id);

  /**
   * Finds every user whose ID is in the given list, loading each user's groups.
   *
   * @param userIds ids to match
   * @return the matching users; empty list when none match
   */
  @EntityGraph(value = "User.groups", type = EntityGraph.EntityGraphType.FETCH)
  List<User> findByIdIn(List<Long> userIds);

  /**
   * Finds every user that matches the given predicate.
   *
   * @param predicate filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  Page<User> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds the sub of the user with the given ID.
   *
   * @param id id to match
   * @return the matching sub, or an empty Optional when no user has that ID
   */
  Optional<String> getSub(@Param("userId") Long id);

  /**
   * Finds every role the user with the given ID holds through any of their groups, loading each
   * role's microservice.
   *
   * @param userId id of the user to match
   * @return the matching roles; empty set when the user has none or does not exist
   */
  Set<Role> getRolesOfUser(@Param("userId") Long userId);

  /**
   * Finds the user with the given sub and issuer, loading the user's groups.
   *
   * @param sub subject identifier to match
   * @param iss issuer to match
   * @return the matching user, or an empty Optional when no user has that sub and issuer
   */
  Optional<User> getUserBySubWithGroups(@Param("sub") String sub, @Param("iss") String iss);

  /**
   * Finds the user with the given ID, loading the user's groups.
   *
   * @param userId id to match
   * @return the matching user, or an empty Optional when no user has that ID
   */
  Optional<User> getUserByIdWithGroups(@Param("userId") Long userId);

  /**
   * Finds every user whose ID is in the given set.
   *
   * @param ids ids to match
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  Page<User> findAllWithGivenIds(@Param("ids") Set<Long> ids, Pageable pageable);
}
