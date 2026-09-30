package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import java.io.IOException;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface UserRepositoryCustom {

  /**
   * Returns the raw bytes of the file referenced by the configured initial OIDC users path.
   *
   * @return the file's bytes
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileNotFoundException when
   *     the configured file does not exist
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileCannotReadException when
   *     the configured file cannot be read
   * @throws IOException when the file cannot be read
   */
  byte[] getInitialOIDCUsers() throws IOException;

  /**
   * Finds every user that does not belong to the group with the given ID. Each user appears at most
   * once, even when the predicate filters on more than one of their groups or roles.
   *
   * @param groupId id of the group to exclude
   * @param predicate optional filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  Page<User> usersNotInGivenGroup(
      @Param("groupId") Long groupId, Predicate predicate, Pageable pageable);

  /**
   * Finds every user that belongs to any of the given {@link IDMGroup}s. Each user appears once
   * even when they belong to more than one of the given groups.
   *
   * @param groupsIds ids of the groups to match
   * @param predicate optional filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  Page<User> usersInGivenGroups(
      @Param("groupsIds") Set<Long> groupsIds, Predicate predicate, Pageable pageable);

  /**
   * Finds every user assigned, through any of their groups, the {@link Role} with the given ID.
   * Each user appears once even when they hold that role through more than one group.
   *
   * @param roleId id of the role to match
   * @param predicate optional filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  Page<User> findAllByRoleId(@Param("roleId") Long roleId, Predicate predicate, Pageable pageable);

  /**
   * Finds every user assigned, through any of their groups, a {@link Role} with the given role
   * type. Each user appears once even when they hold that role type through more than one group.
   *
   * @param roleType role type to match
   * @param predicate optional filter applied to the users
   * @param pageable page and sort request
   * @return the matching page of users; empty page when none match
   */
  Page<User> findAllByRoleType(
      @Param("roleType") String roleType, Predicate predicate, Pageable pageable);

  /**
   * Finds every user, other than those with the given IDs, assigned a {@link Role} with the given
   * role type.
   *
   * @param predicate optional filter applied to the users
   * @param pageable page and sort request
   * @param roleType role type to match
   * @param userIds ids excluded from the result
   * @return the matching page of users; empty page when none match
   */
  Page<User> findAllByRoleAndNotWithIds(
      Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds);
}
