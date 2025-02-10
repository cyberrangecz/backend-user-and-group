package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.QIDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link IDMGroup} instances.
 */
@Repository
public interface IDMGroupRepository extends JpaRepository<IDMGroup, Long>, QuerydslPredicateExecutor<IDMGroup>, QuerydslBinderCustomizer<QIDMGroup> {

    /**
     * That method is used to make the query dsl string values case insensitive
     *
     * @param querydslBindings
     * @param qIDMGroup
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QIDMGroup qIDMGroup) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

    /**
     * Find a group by its name including roles, roles.microservice, and users.
     *
     * @param name the name of the looking IDMGroup.
     * @return {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(value = "IDMGroup.usersRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
    Optional<IDMGroup> findByName(String name);

    /**
     * Find a group by its name including its roles.
     *
     * @param name the name of the looking IDMGroup.
     * @return {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<IDMGroup> findByNameWithRoles(@Param("name") String name);

    /**
     * Find a group by its ID.
     *
     * @param id ID of looking IDMGroup
     * @return the {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    @EntityGraph(value = "IDMGroup.usersRolesMicroservice", type = EntityGraph.EntityGraphType.FETCH)
    Optional<IDMGroup> findById(Long id);

    /**
     * Find the list of groups by its groupIds and fetch also users.
     *
     * @param groupIds the IDs of groups.
     * @return the list of {@link IDMGroup} instance based on the given IDs.
     */
    @EntityGraph(value = "IDMGroup.users", type = EntityGraph.EntityGraphType.FETCH)
    List<IDMGroup> findByIdIn(List<Long> groupIds);

    /**
     * Find all groups.
     *
     * @return list of {@link IDMGroup}s wrapped up in  {@link Page}.
     */
    Page<IDMGroup> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all groups which have given role type.
     *
     * @param roleType the role type of the groups.
     * @return the list of {@link IDMGroup}s.
     */
    List<IDMGroup> findAllByRoleType(@Param("roleType") String roleType);

    /**
     * Find main group with role <i>ADMINISTRATOR</i>.
     *
     * @return the {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<IDMGroup> findAdministratorGroup();

    /**
     * Find a group by its name and with all users.
     *
     * @param name name of the looking IDMGroup.
     * @return the {@link IDMGroup} if the group is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<IDMGroup> getIDMGroupByNameWithUsers(@Param("name") String name);

    /**
     * Delete expired {@link IDMGroup}.
     */
    @Modifying
    void deleteExpiredIDMGroups();

    /**
     * Find all users from all given {@link IDMGroup}s set of IDs.
     *
     * @param groupIds IDs of groups.
     * @return the set of {@link User}s.
     */
    Set<User> findUsersOfGivenGroups(@Param("groupsIds") List<Long> groupIds);

    boolean existsByName(String groupName);
}
