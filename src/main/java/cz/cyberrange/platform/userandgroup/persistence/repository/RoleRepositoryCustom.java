package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface RoleRepositoryCustom {

  /**
   * Finds every role assigned to a group the given user belongs to. Each role appears once, even
   * when the user belongs to more than one group holding it.
   *
   * @param id id of the user to match
   * @param pageable page and sort request
   * @param predicate optional filter further applied to the roles
   * @return the matching page of roles; empty page when none match
   */
  Page<Role> findAllOfUser(Long id, Pageable pageable, Predicate predicate);

  /**
   * Finds every role assigned to the group with the given ID.
   *
   * @param id id of the group to match
   * @param pageable page and sort request
   * @param predicate optional filter further applied to the roles
   * @return the matching page of roles; empty page when none match
   */
  Page<Role> findAllOfGroup(Long id, Pageable pageable, Predicate predicate);

  /**
   * Finds every role not assigned to the {@link IDMGroup} with the given ID.
   *
   * @param groupId id of the group whose roles are excluded
   * @param predicate optional filter further applied to the roles
   * @param pageable page and sort request
   * @return the matching page of roles; empty page when none match
   */
  Page<Role> rolesNotInGivenGroup(
      @Param("groupId") Long groupId, Predicate predicate, Pageable pageable);
}
