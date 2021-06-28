package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.muni.ics.kypo.userandgroup.entities.QUser;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.entities.User;
import io.micrometer.core.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link User} instances.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom, QuerydslPredicateExecutor<User>, QuerydslBinderCustomizer<QUser> {

    /**
     * <p>
     * That method is used to make the query dsl string values case insensitive
     * </p>
     *
     * @param querydslBindings
     * @param qUser
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QUser qUser) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

    /**
     * Find the user by his sub and oidc provider used to authenticate user.
     *
     * @param sub unique sub of the user.
     * @param iss   the URI of the oidc provider
     * @return the {@link User} instance with a given sub if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(value = "User.groupsRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findBySubAndIss(String sub, String iss);

    /**
     * Find the user by his ID.
     *
     * @param id unique identifier of the user.
     * @return the {@link User} instance with a given ID if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(value = "User.groupsRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findById(@NonNull Long id);

    /**
     * Find the user by his userIds.
     *
     * @param userIds the ID of users.
     * @return the list of {@link User} instance based on the given IDs.
     */
    @EntityGraph(value = "User.groupsRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
    List<User> findByIdIn(List<Long> userIds);

    /**
     * Find all users.
     *
     * @return list of {@link User}s wrapped up in {@link Page}.
     */
    @EntityGraph(value = "User.groupsRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
    Page<User> findAll(Predicate predicate, Pageable pageable);

    /**
     * Gets the sub of the user by the given ID of the user.
     *
     * @param id the unique identifier of the user.
     * @return the sub of the {@link User} with a given ID if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<String> getSub(@Param("userId") Long id);

    /**
     * Gets roles of the user.
     *
     * @param userId the unique identifier of the user.
     * @return the {@link Role}s of the user with the given ID.
     */
    Set<Role> getRolesOfUser(@Param("userId") Long userId);

    /**
     * Find the user by his sub also with groups in which he is.
     *
     * @param sub unique sub of the user.
     * @param iss   the iss
     * @return the {@link User} instance with groups if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<User> getUserBySubWithGroups(@Param("sub") String sub, @Param("iss") String iss);

    /**
     * Find users by his ID also with groups in which he is.
     *
     * @param userId unique identifier of the user.
     * @return the {@link User} instance with groups if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<User> getUserByIdWithGroups(@Param("userId") Long userId);

    /**
     * Find all users by a set of ids.
     *
     * @param ids set of ids of users who we are looking for.
     * @return returns set of all {@link User}s whose ids are in a given set of ids.
     */
    Page<User> findAllWithGivenIds(@Param("ids") Set<Long> ids, Pageable pageable);
}
