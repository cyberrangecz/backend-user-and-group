package cz.cyberrange.platform.userandgroup.rest.controller;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.group.AddUsersToGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.GroupViewDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.UpdateGroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiEntityError;
import cz.cyberrange.platform.userandgroup.definition.exceptions.errors.ApiError;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.rest.facade.IDMGroupFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for creating, updating and deleting groups, and for managing the users and roles
 * assigned to each one.
 */
@Tag(name = "groups", description = "Groups of users and the roles granted to their members.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(
    value = {
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
@RequestMapping(path = "/groups")
@Validated
public class GroupsRestController {

  private static final Logger LOG = LoggerFactory.getLogger(GroupsRestController.class);

  private final IDMGroupFacade groupFacade;

  @Autowired
  public GroupsRestController(IDMGroupFacade groupFacade) {
    this.groupFacade = groupFacade;
  }

  /**
   * Creates a group from the given data, copying the members of each group referenced by its
   * imported-user group ids into the new group. The created group leaves its source unset and its
   * deletion flag at the default value of true. Requires the administrator authority; answers with
   * HTTP 401 when the caller is not authenticated, and HTTP 400 when the request body fails
   * validation.
   *
   * @param newGroupDTO data for the group to create, including the ids of the groups whose members
   *     are copied in
   * @return HTTP 201 with the created group
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when a group with that name already exists
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "createNewGroup",
      summary = "Create a group",
      description = "Members of the groups listed for import are copied into the new group.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "The created group.",
            content = @Content(schema = @Schema(implementation = GroupDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "The request body is not valid.",
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
  public ResponseEntity<GroupDTO> createNewGroup(@Valid @RequestBody NewGroupDTO newGroupDTO) {
    return new ResponseEntity<>(groupFacade.createGroup(newGroupDTO), HttpStatus.CREATED);
  }

  /**
   * Updates the name, description and expiration date of the group with the id carried by the given
   * data, leaving its other fields unchanged. Requires the administrator authority; answers with
   * HTTP 401 when the caller is not authenticated, and HTTP 400 when the request body fails
   * validation.
   *
   * @param updateGroupDTO id of the group to update, together with its new name, description and
   *     expiration date
   * @return HTTP 204 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when the group is one of the groups created automatically by the service and its name
   *     is being changed
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "updateGroup",
      summary = "Update a group's name, description and expiry",
      description = "The members and roles of the group stay as they are.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "The group was updated."),
        @ApiResponse(
            responseCode = "400",
            description = "The request body is not valid.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "A group the service created itself cannot be renamed.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateGroup(@Valid @RequestBody UpdateGroupDTO updateGroupDTO) {
    groupFacade.updateGroup(updateGroupDTO);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Removes each given user from the group with the given id. User ids that match no user are
   * ignored. Requires the administrator authority; answers with HTTP 401 when the caller is not
   * authenticated, and HTTP 400 when groupId is not a number or the list of user ids contains a
   * null value.
   *
   * @param id id of the group to update
   * @param userIds ids of the users to remove
   * @return HTTP 204 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when the group is the administrator group and a user to remove is the user
   *     authenticated for the current request, or when the group holds the default role of every
   *     microservice
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "removeUsers",
      summary = "Remove users from a group",
      description = "Ids that match no user are ignored.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "The users were removed."),
        @ApiResponse(
            responseCode = "400",
            description = "The group id is not a number, or a user id in the body is null.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
        @ApiResponse(
            responseCode = "409",
            description =
                "The group is the default group, or an administrator would remove themselves"
                    + " from the administrator group.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @DeleteMapping(path = "/{groupId}/users")
  public ResponseEntity<Void> removeUsers(
      @PathVariable("groupId") final Long id, @RequestBody List<@NotNull Long> userIds) {
    groupFacade.removeUsers(id, userIds);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Adds each user with the given ids, and every member of each group with the given ids, to the
   * group with the given id. Ids that match no user or no group are ignored. Requires the
   * administrator authority; answers with HTTP 401 when the caller is not authenticated, HTTP 400
   * when groupId is not a number, and HTTP 400 when the request body fails validation.
   *
   * @param groupId id of the group to update
   * @param addUsers ids of the users to add directly, and ids of the groups whose members are
   *     copied in
   * @return HTTP 204 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "addUsersToGroup",
      summary = "Add users to a group",
      description =
          "Members of the groups listed for import are added as well. Ids that match no user"
              + " and no group are ignored.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "The users were added."),
        @ApiResponse(
            responseCode = "400",
            description = "The group id is not a number, or the request body is not valid.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @PutMapping(path = "/{groupId}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> addUsers(
      @PathVariable("groupId") final Long groupId,
      @Valid @RequestBody AddUsersToGroupDTO addUsers) {
    groupFacade.addUsersToGroup(groupId, addUsers);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Deletes the group with the given id. Requires the administrator authority; answers with HTTP
   * 401 when the caller is not authenticated, and HTTP 400 when id is not a number.
   *
   * @param id id of the group to delete
   * @return HTTP 200 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when the group is one of the groups created automatically by the service, or the group
   *     has any users
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "deleteGroup",
      summary = "Delete a group",
      description = "The group must have no members left.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "The group was deleted."),
        @ApiResponse(
            responseCode = "400",
            description = "The group id is not a number.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "The group still has members, or the service created it itself.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @DeleteMapping(path = "/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteGroup(@PathVariable("groupId") final Long id) {
    groupFacade.deleteGroup(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Deletes each group with the given ids. Ids that match no group are ignored. Requires the
   * administrator authority; answers with HTTP 401 when the caller is not authenticated, and HTTP
   * 400 when the list of ids contains a null value.
   *
   * @param ids ids of the groups to delete
   * @return HTTP 200 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when any of the groups is one of the groups created automatically by the service, or
   *     has any users
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "deleteGroups",
      summary = "Delete several groups",
      description = "Each group must have no members left. Ids that match no group are ignored.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "The groups were deleted."),
        @ApiResponse(
            responseCode = "400",
            description = "An id in the body is null.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "A group still has members, or the service created it itself.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @DeleteMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteGroups(@RequestBody List<@NotNull Long> ids) {
    LOG.debug("deleteGroups({})", ids);
    groupFacade.deleteGroups(ids);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns every group that matches the given predicate, as basic group views. Requires the
   * administrator authority; answers with HTTP 401 when the caller is not authenticated.
   *
   * @param predicate optional filter on group attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @return HTTP 200 with the matching page of group views; empty page when none match
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getGroups",
      summary = "List groups",
      description =
          "Text filters match any part of a value and ignore case. Every other filter must match"
              + " the whole value.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The matching page of groups, empty when nothing matches."),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PageResultResource<GroupViewDTO>> getGroups(
      @QuerydslPredicate(root = IDMGroup.class) Predicate predicate,
      @ParameterObject Pageable pageable) {
    PageResultResource<GroupViewDTO> groupsDTOs = groupFacade.getAllGroups(predicate, pageable);
    return ResponseEntity.ok(groupsDTOs);
  }

  /**
   * Returns the group with the given id. Requires the administrator authority; answers with HTTP
   * 401 when the caller is not authenticated, and HTTP 400 when id is not a number.
   *
   * @param id id of the group
   * @return HTTP 200 with the matching group
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getGroupById",
      summary = "Get a single group with its members and roles")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The requested group.",
            content = @Content(schema = @Schema(implementation = GroupDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "The group id is not a number.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @GetMapping(path = "/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GroupDTO> getGroup(@PathVariable("groupId") Long id) {
    return ResponseEntity.ok(groupFacade.getGroupById(id));
  }

  /**
   * Returns the roles assigned to the group with the given id, with each role's microservice id and
   * name filled in. Requires the administrator authority; answers with HTTP 401 when the caller is
   * not authenticated, and HTTP 400 when id is not a number.
   *
   * @param predicate optional filter on role attributes: each string property matches
   *     case-insensitively as a substring of the given value, every other property must equal it
   *     exactly
   * @param pageable page and sort request
   * @param id id of the group
   * @return HTTP 200 with the matching page of roles; empty page when none match
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "getRolesOfGroup",
      summary = "List the roles of a group",
      description =
          "Text filters match any part of a value and ignore case. Every other filter must match"
              + " the whole value.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The matching page of roles, empty when nothing matches."),
        @ApiResponse(
            responseCode = "400",
            description = "The group id is not a number.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @GetMapping(path = "/{id}/roles")
  public ResponseEntity<PageResultResource<RoleDTO>> getRolesOfGroup(
      @QuerydslPredicate(root = Role.class) Predicate predicate,
      @ParameterObject Pageable pageable,
      @Parameter(description = "Id of the group whose roles are listed.") @PathVariable("id")
          final Long id) {
    return ResponseEntity.ok(groupFacade.getRolesOfGroup(id, pageable, predicate));
  }

  /**
   * Assigns the role with the given id to the group with the given id. Requires the administrator
   * authority; answers with HTTP 401 when the caller is not authenticated, and HTTP 400 when either
   * id is not a number.
   *
   * @param groupId id of the group
   * @param roleId id of the role
   * @return HTTP 204 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id, or no role has that id
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "assignRoleToGroup",
      summary = "Assign a role to a group",
      description = "Every member of the group gains the role.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "The role was assigned."),
        @ApiResponse(
            responseCode = "400",
            description = "The group id or the role id is not a number.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id, or no role has that id.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @PutMapping("/{groupId}/roles/{roleId}")
  public ResponseEntity<Void> assignRoleToGroup(
      @PathVariable("groupId") Long groupId, @PathVariable("roleId") Long roleId) {
    groupFacade.assignRole(groupId, roleId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Removes the role with the given id from the group with the given id. Requires the administrator
   * authority; answers with HTTP 401 when the caller is not authenticated, and HTTP 400 when either
   * id is not a number.
   *
   * @param groupId id of the group
   * @param roleId id of the role
   * @return HTTP 204 with an empty body
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException (HTTP
   *     404) when no group has that id, or the group does not have a role with that id
   * @throws cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException (HTTP
   *     409) when the role is the main role of one of the groups created automatically by the
   *     service: the trainee role of the default group, the administrator role of the administrator
   *     group, or the power user role of the power user group
   * @throws org.springframework.security.access.AccessDeniedException (HTTP 403) when the caller
   *     does not hold that authority
   */
  @Operation(
      operationId = "removeRoleFromGroup",
      summary = "Remove a role from a group",
      description = "Every member of the group loses the role.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "The role was removed."),
        @ApiResponse(
            responseCode = "400",
            description = "The group id or the role id is not a number.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Caller lacks the ROLE_USER_AND_GROUP_ADMINISTRATOR role.",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No group has that id, or the group does not hold that role.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "The role is the main role of a group the service created itself.",
            content = @Content(schema = @Schema(implementation = ApiEntityError.class)))
      })
  @DeleteMapping("/{groupId}/roles/{roleId}")
  public ResponseEntity<Void> removeRoleFromGroup(
      @PathVariable("groupId") Long groupId, @PathVariable("roleId") Long roleId) {
    groupFacade.removeRoleFromGroup(groupId, roleId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
