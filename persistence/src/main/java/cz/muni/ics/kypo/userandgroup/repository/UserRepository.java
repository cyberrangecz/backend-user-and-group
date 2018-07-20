package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
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
public interface UserRepository extends JpaRepository<User, Long>,
        QuerydslPredicateExecutor<User> {

    Optional<User> findByScreenName(String screenName);

    @Query("SELECT u.screenName FROM User u WHERE u.id = :userId")
    Optional<String> getScreenName(@Param("userId") Long id);

    @Query("SELECT CASE WHEN u.externalId IS NULL THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean isUserInternal(@Param("userId") Long id);

    @Query("SELECT DISTINCT r FROM IDMGroup g INNER JOIN g.roles r INNER JOIN g.users u WHERE u.id = :userId")
    Set<Role> getRolesOfUser(@Param("userId") Long id);

    @EntityGraph(value = "User.groups", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT u FROM User u WHERE u.screenName = :screenName")
    Optional<User> getUserByScreenNameWithUsers(@Param("screenName") String screenName);

    @Query("SELECT u FROM User u WHERE (SELECT g FROM IDMGroup g WHERE g.id = :groupId) NOT MEMBER OF u.groups")
    Page<User> usersNotInGivenGroup(@Param("groupId") Long groupId, Pageable pageable);
}
