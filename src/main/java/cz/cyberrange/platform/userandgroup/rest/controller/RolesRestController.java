package cz.cyberrange.platform.userandgroup.rest.controller;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserBasicViewDto;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.BadRequestException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.rest.facade.RoleFacade;
import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for querying roles, individually, by role type, or as a filtered page, and for finding
 * the users assigned a given role.
 */
@Tag(name = "roles")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
  @ApiResponse(
      responseCode = "401",
      description = "Missing or invalid bearer token.",
      content = @Content(schema = @Schema(implementation = ApiError.class))),
  @ApiResponse(
      responseCode = "500",
      description = "Unexpected server error.",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
})
@RestController
@RequestMapping(path = "/roles")
public class RolesRestController {

  private final RoleFacade roleFacade;
  private final UserFacade userFacade;

  @Autowired
  public RolesRestController(RoleFacade roleFacade, UserFacade userFacade) {
    this.roleFacade = roleFacade;
    this.userFacade = userFacade;
  }

  /**
   * Returns every role that matches the given predicate, with each role's microservice id and name
   * filled in. Requires the administrator authority; answers with HTTP 401 when the caller is not
   * authenticated.
   *
   * @param predicate optional filter on role attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @return HTTP 200 with the matching page of roles; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching roles; empty when none match."),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<RoleDTO>> getRoles(
      @QuerydslPredicate(root = Role.class) Predicate predicate,
      @ParameterObject Pageable pageable) {
    PageResultResource<RoleDTO> roleDTOs = roleFacade.getAllRoles(predicate, pageable);
    return ResponseEntity.ok(roleDTOs);
  }

  /**
   * Returns the role with the given id, with its microservice id and name filled in. Requires the
   * administrator authority; answers with HTTP 401 when the caller is not authenticated, and HTTP
   * 400 when id is not a number.
   *
   * @param id id of the role
   * @return HTTP 200 with the matching role
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no role has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(operationId = "getRole", summary = "Get a role by id")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The requested role.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RoleDTO.class))),
    @ApiResponse(
        responseCode = "400",
        description = "The role id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No role has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RoleDTO> getRole(@PathVariable("roleId") final Long id) {
    return ResponseEntity.ok(roleFacade.getRoleById(id));
  }

  /**
   * Returns every role not assigned to the group with the given id, matching the given predicate,
   * with each role's microservice id and name filled in. Requires the administrator authority;
   * answers with HTTP 401 when the caller is not authenticated, and HTTP 400 when groupId is not a
   * number.
   *
   * @param predicate optional filter on role attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param groupId id of the group whose roles are excluded
   * @return HTTP 200 with the matching page of roles; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getAllRolesNotInGivenGroup",
      summary = "List roles not assigned to a group",
      description = "Text filters match any part of a value, ignoring case.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching roles; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "The group id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/not-in-group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<RoleDTO>> getAllRolesNotInGivenGroup(
      @QuerydslPredicate(root = Role.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @PathVariable("groupId") final Long groupId) {
    PageResultResource<RoleDTO> roleDTOs =
        roleFacade.getAllRolesNotInGivenGroup(groupId, predicate, pageable);
    return ResponseEntity.ok(roleDTOs);
  }

  /**
   * Returns every user assigned, through any of their groups, the role with the given id, with each
   * user's roles filled in from every group it belongs to and each role's microservice id and name
   * filled in. Requires the administrator authority; answers with HTTP 401 when the caller is not
   * authenticated, and HTTP 400 when roleId is not a number.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param roleId id of the role
   * @return HTTP 200 with the matching page of users; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no role has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUsersWithGivenRole",
      summary = "List users holding a role",
      description =
          "A user holds a role through the groups it belongs to. Text filters match any part of"
              + " a value, ignoring case.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "The role id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No role has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{roleId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserDTO>> getUsersWithGivenRole(
      @QuerydslPredicate(root = User.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @PathVariable("roleId") Long roleId) {
    PageResultResource<UserDTO> userDTOs =
        userFacade.getUsersWithGivenRole(roleId, predicate, pageable);
    return ResponseEntity.ok(userDTOs);
  }

  /**
   * Returns every user assigned, through any of their groups, a role with the given role type, with
   * each user's roles filled in from every group it belongs to and each role's microservice id and
   * name filled in. A blank role type is not rejected before the lookup runs; it simply matches no
   * role, producing the not-found error below. Requires the administrator or power user authority;
   * answers with HTTP 401 when the caller is not authenticated, and HTTP 400 when roleType is
   * missing.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param roleType role type to match
   * @return HTTP 200 with the matching page of users; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no role has that type
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUsersWithGivenRoleType",
      summary = "List users holding a role type",
      description =
          "The role type must match in full. A user holds a role through the groups it belongs"
              + " to.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "The roleType parameter is missing.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description =
            "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR or"
                + " ROLE_USER_AND_GROUP_POWER_USER role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No role has that type.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserDTO>> getUsersWithGivenRoleType(
      @QuerydslPredicate(root = User.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @NotBlank @RequestParam("roleType") String roleType) {
    PageResultResource<UserDTO> userDTOs =
        userFacade.getUsersWithGivenRoleType(roleType, predicate, pageable);
    return ResponseEntity.ok(userDTOs);
  }

  /**
   * Returns every user, other than those with the given ids, assigned a role with the given role
   * type, as basic views. Requires the administrator or power user authority; answers with HTTP 401
   * when the caller is not authenticated, and HTTP 400 when roleType or ids is missing.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param roleType role type to match
   * @param userIds ids excluded from the result
   * @return HTTP 200 with the matching page of basic user views; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.BadRequestException (HTTP
   *     400) when the requested page size is 1000 or more
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUsersWithGivenRoleTypeAndNotWithGivenIds",
      summary = "List users of a role type, minus given ids",
      description = "The role type must match in full. Page size must be below 1000.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "The roleType or ids parameter is missing, or page size is 1000 or more.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description =
            "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR or"
                + " ROLE_USER_AND_GROUP_POWER_USER role.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/users-not-with-ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserBasicViewDto>>
      getUsersWithGivenRoleTypeAndNotWithGivenIds(
          @QuerydslPredicate(root = User.class) Predicate predicate,
          @ParameterObject Pageable pageable,
          @RequestParam("roleType") String roleType,
          @Parameter(description = "Ids of the users left out of the result.") @RequestParam("ids")
              Set<Long> userIds) {
    if (pageable.getPageSize() >= 1000) {
      throw new BadRequestException("Choose page size lower than 1000");
    }
    PageResultResource<UserBasicViewDto> userDTOs =
        userFacade.getUsers(predicate, pageable, roleType, userIds);
    return ResponseEntity.ok(userDTOs);
  }
}
