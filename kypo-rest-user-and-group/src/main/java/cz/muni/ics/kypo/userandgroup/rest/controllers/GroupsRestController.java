package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.Role;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.utils.ApiPageableSwagger;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Rest controller for the IDMGroup resource.
*/
@Api(value = "Endpoint for Groups",
     tags = "groups",
     authorizations = @Authorization(value = "bearerAuth"))
@RestController
@RequestMapping(path = "/groups")
@Validated
public class GroupsRestController {

    private static Logger LOG = LoggerFactory.getLogger(GroupsRestController.class);

    private IDMGroupFacade groupFacade;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new GroupsRestController.
     *
     * @param groupFacade  the group facade
     * @param objectMapper the object mapper
     */
    @Autowired
    public GroupsRestController(IDMGroupFacade groupFacade, ObjectMapper objectMapper) {
        this.groupFacade = groupFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new group in the database.
     *
     * @param newGroupDTO new group to be created {@link NewGroupDTO}.
     * @return the {@link ResponseEntity} with body type {@link GroupDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "POST",
            value = "Create new group.",
            response = GroupDTO.class,
            nickname = "createNewGroup",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Given group created.", response = GroupDTO.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDTO> createNewGroup(@ApiParam(value = "Group to be created.", required = true)
                                                   @Valid @RequestBody NewGroupDTO newGroupDTO) {
        return new ResponseEntity<>(groupFacade.createGroup(newGroupDTO), HttpStatus.CREATED);

    }

    /**
     * Update group in the database.
     *
     * @param updateGroupDTO the group to be updated {@link UpdateGroupDTO}.
     * @return the empty response entity with specific status code and header.
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Update group.",
            nickname = "updateGroup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group updated."),
            @ApiResponse(code = 405, message = "Group is external and cannot be modified.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Name of the main group cannot be changed.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGroup(@ApiParam(value = "Group to be updated.", required = true)
                                            @Valid @RequestBody UpdateGroupDTO updateGroupDTO) {
        groupFacade.updateGroup(updateGroupDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Remove users from the group.
     *
     * @param id      the ID of the group.
     * @param userIds a list of IDs of the users to be imported
     * @return the response entity with empty body and with specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Remove users from the group.",
            nickname = "removeUsers",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "User has been removed from the group."),
            @ApiResponse(code = 304, message = "Group is external and cannot be modified.", response = ApiError.class),
            @ApiResponse(code = 404, message = "Group or some user cannot be found.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Users cannot be removed from default group or administrator cannot remove himself.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(path = "/{groupId}/users")
    public ResponseEntity<Void> removeUsers(@ApiParam(value = "Id of group to remove users.", required = true)
                                            @PathVariable("groupId") final Long id,
                                            @ApiParam(value = "Ids of members to be removed from group.", required = true)
                                            @RequestBody List<@NotNull Long> userIds) {
        groupFacade.removeUsers(id, userIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Add users to the group.
     *
     * @param groupId  the ID of the group
     * @param addUsers {@link AddUsersToGroupDTO}.
     * @return the response entity with empty body and with specific status code and header.
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Add users to group.",
            nickname = "addUsersToGroup",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "User has been given into group."),
            @ApiResponse(code = 304, message = "Group is external and cannot be modified.", response = ApiError.class),
            @ApiResponse(code = 404, message = "Group or some user cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PutMapping(path = "/{groupId}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addUsers(@ApiParam(value = "Id of group to add users.", required = true)
                                         @PathVariable("groupId") final Long groupId,
                                         @ApiParam(value = "Ids of members to be added and ids of groups of imported members to group.", required = true)
                                         @Valid @RequestBody AddUsersToGroupDTO addUsers) {
        groupFacade.addUsersToGroup(groupId, addUsers);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Delete group from the database.
     *
     * @param id the ID of the group to be deleted.
     * @return the {@link ResponseEntity} with body type and specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete group",
            nickname = "deleteGroup",
            notes = "Tries to deleteIDMGroup group with given id and returns if it was successful.",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returned deletion status of the group."),
            @ApiResponse(code = 404, message = "Group cannot be found.", response = ApiError.class),
            @ApiResponse(code = 405, message = "Group cannot be deleted because it is a main group.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(path = "/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroup(@ApiParam(value = "Id of group to be deleted.", required = true)
                                            @PathVariable("groupId") final Long id) {
        groupFacade.deleteGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Delete groups from the database.
     *
     * @param ids list of IDs of the group to be deleted.
     * @return the {@link ResponseEntity} with body type and specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete groups",
            notes = "Tries to deleteIDMGroup groups with given ids and returns groups and statuses of their deletion",
            nickname = "deleteGroups",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returned HTTP status OK."),
            @ApiResponse(code = 500, message = "Cannot deleteIDMGroup non-empty group.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroups(@ApiParam(value = "Ids of groups to be deleted.", required = true)
                                             @RequestBody List<@NotNull Long> ids) {
        LOG.debug("deleteGroups({})", ids);
        groupFacade.deleteGroups(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets all groups.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @return the groups.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get groups.",
            nickname = "getGroups",
            response = GroupRestResource.class,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All groups found.", response = GroupRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getGroups(@ApiParam(value = "Filtering on IDMGroup entity attributes", required = false)
                                            @QuerydslPredicate(root = IDMGroup.class) Predicate predicate,
                                            Pageable pageable,
                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                            @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<GroupViewDTO> groupsDTOs = groupFacade.getAllGroups(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, groupsDTOs));
    }

    /**
     * Gets the group with the given ID.
     *
     * @param id the ID of the group.
     * @return the {@link ResponseEntity} with body type {@link GroupDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get group with given id",
            nickname = "getGroupById",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group found.", response = GroupDTO.class),
            @ApiResponse(code = 404, message = "Group cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDTO> getGroup(@ApiParam(value = "Id of group to be returned.", required = true)
                                             @PathVariable("groupId") Long id) {
        return ResponseEntity.ok(groupFacade.getGroupById(id));
    }

    /**
     * Gets the roles of the given group.
     *
     * @param id the ID of the group
     * @return the {@link ResponseEntity} with body type of the given group and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Returns all roles of group",
            nickname = "getRolesOfGroup",
            response = RolesRestController.RoleRestResource.class,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All roles of group found.", response = RolesRestController.RoleRestResource.class),
            @ApiResponse(code = 404, message = "Group cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{id}/roles")
    @ApiPageableSwagger
    public ResponseEntity<Object> getRolesOfGroup(@ApiParam(value = "Filtering on IDMGroup entity attributes", required = false)
                                                        @QuerydslPredicate(root = Role.class) Predicate predicate,
                                                        Pageable pageable,
                                                        @ApiParam(value = "id", required = true)
                                                        @PathVariable("id") final Long id) {
        return ResponseEntity.ok(groupFacade.getRolesOfGroup(id, pageable, predicate));
    }

    /**
     * Assign a new role to the group.
     *
     * @param groupId the ID of the group.
     * @param roleId  the ID of the role to be added.
     * @return the response entity with empty body and with specific status code and header.
     */
    @ApiOperation(httpMethod = "PUT",
            value = "Assign role to the group",
            nickname = "assignRoleToGroup"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Role assigned to group."),
            @ApiResponse(code = 404, message = "Role or group cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PutMapping("/{groupId}/roles/{roleId}")
    public ResponseEntity<Void> assignRoleToGroup(@ApiParam(value = "groupId", required = true)
                                                  @PathVariable("groupId") Long groupId,
                                                  @ApiParam(value = "roleId", required = true)
                                                  @PathVariable("roleId") Long roleId) {
        groupFacade.assignRole(groupId, roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Remove the role from the given group.
     *
     * @param groupId the ID of the group.
     * @param roleId  the ID of the role to be removed from the group.
     * @return the response entity with empty body and with specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Remove role from the group",
            notes = "Role can be removed only if it is not main role of the group.",
            nickname = "removeRoleFromGroup"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Role successfully removed from the group."),
            @ApiResponse(code = 404, message = "Group cannot be found or role cannot be found in group.", response = ApiError.class),
            @ApiResponse(code = 409, message = "Role cannot be removed from the group because it is the main role of the group.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping("/{groupId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromGroup(@ApiParam(value = "groupId", required = true)
                                                    @PathVariable("groupId") Long groupId,
                                                    @ApiParam(value = "roleId", required = true)
                                                    @PathVariable("roleId") Long roleId) {
        groupFacade.removeRoleFromGroup(groupId, roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiModel(value = "GroupRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages.")
    private static class GroupRestResource extends PageResultResource<GroupViewDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved IDMGroups from databases.")
        private List<GroupViewDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }
}
