package cz.cyberrange.platform.userandgroup.rest.controller;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.UsersImportDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserBasicViewDto;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.BadRequestException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for querying, importing and deleting users, for retrieving a user's roles or the
 * caller's own profile, and for exporting the configured initial OIDC users.
 */
@Tag(name = "users")
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
@RequestMapping(path = "/users")
@Validated
public class UsersRestController {

  private static final Logger LOG = LoggerFactory.getLogger(UsersRestController.class);

  private final UserFacade userFacade;

  @Autowired
  public UsersRestController(UserFacade userFacade) {
    this.userFacade = userFacade;
  }

  /**
   * Returns every user that matches the given predicate, as basic views. Requires the administrator
   * authority; answers with HTTP 401 when the caller is not authenticated.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @return HTTP 200 with the matching page of basic user views; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUsers",
      summary = "List users",
      description = "Text filters match any part of a value, ignoring case.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserBasicViewDto>> getUsers(
      @QuerydslPredicate(root = User.class) Predicate predicate,
      @ParameterObject Pageable pageable) {
    PageResultResource<UserBasicViewDto> userDTOs = userFacade.getUsers(predicate, pageable);
    return ResponseEntity.ok(userDTOs);
  }

  /**
   * Returns every user that belongs to any of the given groups. Requires the administrator
   * authority; answers with HTTP 401 when the caller is not authenticated, and HTTP 400 when ids is
   * missing.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param groupsIds ids of the groups to match
   * @return HTTP 200 with the matching page of users; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUsersInGroups",
      summary = "List users of the given groups",
      description =
          "A user is returned when it belongs to any of the groups. Text filters match any part"
              + " of a value, ignoring case.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "The ids parameter is missing or is not a list of numbers.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserForGroupsDTO>> getUsersInGroups(
      @QuerydslPredicate(root = User.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @Parameter(description = "Ids of the groups whose members are returned.") @RequestParam("ids")
          Set<Long> groupsIds) {
    PageResultResource<UserForGroupsDTO> userDTOs =
        userFacade.getUsersInGroups(groupsIds, predicate, pageable);
    return ResponseEntity.ok(userDTOs);
  }

  /**
   * Returns the user with the given id, with the roles held through any of their groups filled in.
   * A caller without the administrator or power user authority may only request their own id;
   * requesting a different id fails with HTTP 403. Requires at least the trainee authority; answers
   * with HTTP 401 when the caller is not authenticated, and HTTP 400 when id is not a number.
   *
   * @param id id of the user
   * @return HTTP 200 with the matching user
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no user has that id, or when no user matches the current request's sub and issuer
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.UagAccessForbiddenException
   *     (HTTP 403) when the caller lacks the administrator or power user authority and requests a
   *     user other than itself
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUserById",
      summary = "Get a user by id",
      description =
          "An administrator or power user may ask for any id. Other callers may ask only for"
              + " their own. A different id is forbidden.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The requested user.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserDTO.class))),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description =
            "Caller lacks the ROLE_USER_AND_GROUP_TRAINEE role, or a non-privileged caller asked"
                + " for another user's id.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No user has that id, or no user matches the caller's sub and issuer.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDTO> getUser(@PathVariable("userId") final Long id) {
    return ResponseEntity.ok(userFacade.getUserById(id));
  }

  /**
   * Returns every user that does not belong to the group with the given id, with each user's roles
   * filled in from every group it belongs to and each role's microservice id and name filled in.
   * Requires the administrator authority; answers with HTTP 401 when the caller is not
   * authenticated, and HTTP 400 when groupId is not a number.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param groupId id of the group to exclude
   * @return HTTP 200 with the matching page of users; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getAllUsersNotInGivenGroup",
      summary = "List users outside a group",
      description = "Text filters match any part of a value, ignoring case.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
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
  public ResponseEntity<PageResultResource<UserDTO>> getAllUsersNotInGivenGroup(
      @QuerydslPredicate(root = User.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @PathVariable("groupId") final Long groupId) {
    PageResultResource<UserDTO> userDTOs =
        userFacade.getAllUsersNotInGivenGroup(groupId, predicate, pageable);
    return ResponseEntity.ok(userDTOs);
  }

  /**
   * Deletes the user with the given id, first removing it from each of its groups. Requires the
   * administrator authority; answers with HTTP 401 when the caller is not authenticated, and HTTP
   * 400 when id is not a number.
   *
   * @param id id of the user to delete
   * @return HTTP 200 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no user has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "deleteUser",
      summary = "Delete a user",
      description = "The user is removed from each of its groups first.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The user was deleted."),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No user has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @DeleteMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteUser(@PathVariable("userId") final Long id) {
    userFacade.deleteUser(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Deletes each user with the given ids, first removing each one from its groups. Ids that match
   * no user are ignored. Requires the administrator authority; answers with HTTP 401 when the
   * caller is not authenticated, and HTTP 400 when the list of ids contains a null value.
   *
   * @param ids ids of the users to delete
   * @return HTTP 200 with an empty body
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "deleteUsers",
      summary = "Delete several users",
      description = "Ids matching no user are skipped.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The users were deleted."),
    @ApiResponse(
        responseCode = "400",
        description = "The body holds a null id.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @DeleteMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteUsers(@RequestBody List<@NotNull Long> ids) {
    userFacade.deleteUsers(ids);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns the roles assigned to a group the user with the given id belongs to, with each role's
   * microservice id and name filled in. Requires the administrator or power user authority; answers
   * with HTTP 401 when the caller is not authenticated, and HTTP 400 when id is not a number.
   *
   * @param predicate optional filter on role attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param id id of the user
   * @return HTTP 200 with the matching page of roles; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no user has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching roles; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a number.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description =
            "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR or"
                + " ROLE_USER_AND_GROUP_POWER_USER role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No user has that id.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/{id}/roles")
  public ResponseEntity<PageResultResource<RoleDTO>> getRolesOfUser(
      @QuerydslPredicate(root = Role.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @Parameter(description = "Id of the user whose roles are listed.") @PathVariable("id")
          final Long id) {
    return ResponseEntity.ok(userFacade.getRolesOfUserWithPagination(id, pageable, predicate));
  }

  /**
   * Returns the user authenticated for the current request, with the roles held through any of
   * their groups filled in. Requires at least the trainee authority; answers with HTTP 401 when the
   * caller is not authenticated.
   *
   * @return HTTP 200 with the authenticated user
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no user matches the current request's sub and issuer
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(operationId = "getUserInfo", summary = "Get the signed-in user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "The signed-in user.",
        content = @Content(schema = @Schema(implementation = UserDTO.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_TRAINEE role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No user matches the current request's sub and issuer.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/info")
  public ResponseEntity<UserDTO> getUserInfo() {
    return ResponseEntity.ok(userFacade.getUserInfo());
  }

  /**
   * Returns every user whose id is in the given list and that matches the given predicate, as basic
   * views. A caller without the administrator or power user authority receives every other user's
   * full name, sub, given name, family name and mail replaced by a placeholder; the caller's own
   * record, and every user's id, issuer and picture, stay unmasked. When ids is empty, this returns
   * an empty page without evaluating the predicate. Requires at least the trainee authority;
   * answers with HTTP 401 when the caller is not authenticated, and HTTP 400 when the requested
   * page size is 1000 or more.
   *
   * @param predicate optional filter on user attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param ids ids to match
   * @return HTTP 200 with the matching page of basic user views; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.BadRequestException (HTTP
   *     400) when the requested page size is 1000 or more
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when the caller lacks the administrator or power user authority and no user matches
   *     the current request's sub and issuer
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getUsersWithGivenIds",
      summary = "List users by id",
      description =
          "Other users' names, sub and mail come back masked. Administrators and power users see"
              + " them in full. Page size must be below 1000.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matching users; empty when none match."),
    @ApiResponse(
        responseCode = "400",
        description = "Page size is 1000 or more.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_TRAINEE role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "No user matches the current request's sub and issuer.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @GetMapping(path = "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<UserBasicViewDto>> getUsersWithGivenIds(
      @QuerydslPredicate(root = User.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @RequestParam(value = "ids") List<Long> ids) {
    if (pageable.getPageSize() >= 1000) {
      throw new BadRequestException("Choose page size lower than 1000");
    }
    PageResultResource<UserBasicViewDto> userDTOs;
    if (ids.isEmpty()) {
      userDTOs =
          new PageResultResource<>(
              Collections.emptyList(), new PageResultResource.Pagination(0, 0, 0, 0, 0));
    } else {
      userDTOs = userFacade.getUsersWithGivenIds(ids, pageable, predicate);
    }
    return ResponseEntity.ok(userDTOs);
  }

  /**
   * Returns the raw bytes of the file configured as the source of initial OIDC users, as an
   * attachment named oidc-initial-users.yaml. Requires the administrator authority; answers with
   * HTTP 401 when the caller is not authenticated.
   *
   * @return HTTP 200 with the file's bytes
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileNotFoundException (HTTP
   *     404) when the configured file does not exist, or reading it raises an IOException
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.FileCannotReadException (HTTP
   *     500) when the configured file cannot be read
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getInitialOIDCUsers",
      summary = "Download the initial OIDC users file",
      description = "The file is sent as an attachment named oidc-initial-users.yaml.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Contents of the configured file.",
        content =
            @Content(
                mediaType = "application/octet-stream",
                schema = @Schema(type = "string", format = "binary"))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "The configured file is missing or could not be read.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping(path = "/initial-oidc-users", produces = "application/octet-stream")
  public ResponseEntity<byte[]> getInitialOIDCUsers() {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .header("Content-Disposition", "attachment; filename=oidc-initial-users.yaml")
        .body(userFacade.getInitialOIDCUsers());
  }

  /**
   * Creates the users carried by the given data and adds each one to the group that holds every
   * microservice's default role, and also to a new group with the given name when one is given. Two
   * entries with the same subject and issuer collapse into a single created user. Each created user
   * leaves its external id and mail unset, and is given a generated identicon picture derived from
   * its subject and issuer. Requires the administrator authority; answers with HTTP 401 when the
   * caller is not authenticated, and HTTP 400 when the request body fails validation.
   *
   * @param usersImportDTO users to import, and the optional name of a group to add them to
   * @return HTTP 204 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when a group with the given name already exists
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "importUsers",
      summary = "Import users",
      description =
          "Every imported user joins the group holding the default roles. A group name creates"
              + " that group and adds them to it as well. Entries sharing a subject and issuer"
              + " become one user.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "The users were imported."),
    @ApiResponse(
        responseCode = "400",
        description = "A user entry is missing its sub or iss.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "A group with that name already exists.",
        content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
  })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> importUsers(@Valid @RequestBody UsersImportDTO usersImportDTO) {
    userFacade.importUsers(usersImportDTO);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
