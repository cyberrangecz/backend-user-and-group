package cz.cyberrange.platform.userandgroup.rest.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapper;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdmin;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operations for querying roles, individually, by role type, or as a filtered page. */
@Service
@Transactional
public class RoleFacade {

  private final RoleService roleService;
  private final RoleMapper roleMapper;

  @Autowired
  public RoleFacade(RoleService roleService, RoleMapper roleMapper) {
    this.roleService = roleService;
    this.roleMapper = roleMapper;
  }

  /**
   * Returns the role with the given id, with its microservice id and name filled in.
   *
   * @param id id of the role
   * @return the matching role
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no role has that id
   */
  @IsAdmin
  @TransactionalRO
  public RoleDTO getRoleById(Long id) {
    return roleMapper.mapToRoleDTOWithMicroservice(roleService.getRoleById(id));
  }

  /**
   * Returns the role whose role type equals the given value, matched case-insensitively, with its
   * microservice id and name filled in.
   *
   * @param roleType role type to match
   * @return the matching role
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException when
   *     no role has that type
   */
  @IsAdmin
  @TransactionalRO
  public RoleDTO getByRoleType(String roleType) {
    return roleMapper.mapToRoleDTOWithMicroservice(
        roleService.getByRoleType(roleType.toUpperCase()));
  }

  /**
   * Returns every role that matches the given predicate, with each role's microservice id and name
   * filled in.
   *
   * @param predicate filter applied to the roles
   * @param pageable page and sort request
   * @return the matching page of roles; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
    Page<Role> roles = roleService.getAllRoles(predicate, pageable);
    List<RoleDTO> roleDTOs =
        roles.getContent().stream()
            .map(role -> roleMapper.mapToRoleDTOWithMicroservice(role))
            .collect(Collectors.toCollection(ArrayList::new));
    PageResultResource.Pagination pagination = roleMapper.createPagination(roles);
    return new PageResultResource<>(roleDTOs, pagination);
  }

  /**
   * Returns every role not assigned to the group with the given id, matching the given predicate,
   * with each role's microservice id and name filled in.
   *
   * @param groupId id of the group whose roles are excluded
   * @param predicate optional filter further applied to the roles
   * @param pageable page and sort request
   * @return the matching page of roles; empty page when none match
   */
  @IsAdmin
  @TransactionalRO
  public PageResultResource<RoleDTO> getAllRolesNotInGivenGroup(
      Long groupId, Predicate predicate, Pageable pageable) {
    Page<Role> roles = roleService.getAllRolesNotInGivenGroup(groupId, predicate, pageable);
    List<RoleDTO> roleDTOs =
        roles.getContent().stream().map(roleMapper::mapToRoleDTOWithMicroservice).toList();
    PageResultResource.Pagination pagination = roleMapper.createPagination(roles);
    return new PageResultResource<>(roleDTOs, pagination);
  }
}
