package cz.muni.ics.kypo.userandgroup.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.swagger.ApiPageableSwagger;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserBasicViewDto;
import cz.muni.ics.kypo.userandgroup.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.exceptions.BadRequestException;
import cz.muni.ics.kypo.userandgroup.exceptions.errors.ApiError;
import cz.muni.ics.kypo.userandgroup.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.facade.UserFacade;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.Encoded;
import java.util.List;
import java.util.Set;

/**
 * Rest controller for the Role resource.
 */
@Api(value = "Endpoint for Roles",
        tags = "roles",
        authorizations = @Authorization(value = "bearerAuth"))
@RestController
@RequestMapping(path = "/roles")
public class RolesRestController {

    private final RoleFacade roleFacade;
    private final UserFacade userFacade;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new RolesRestController.
     *
     * @param roleFacade   the role facade
     * @param userFacade   the user facade
     * @param objectMapper the object mapper
     */
    @Autowired
    public RolesRestController(RoleFacade roleFacade, UserFacade userFacade, ObjectMapper objectMapper) {
        this.roleFacade = roleFacade;
        this.objectMapper = objectMapper;
        this.userFacade = userFacade;
    }

    /**
     * Gets all roles.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @return the roles
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all roles",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All roles found.", response = RoleRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getRoles(@ApiParam(value = "Filtering on Role entity attributes", required = false)
                                           @QuerydslPredicate(root = Role.class) Predicate predicate,
                                           Pageable pageable,
                                           @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                           @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<RoleDTO> roleDTOs = roleFacade.getAllRoles(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, roleDTOs));
    }

    /**
     * Gets the role with the given ID.
     *
     * @param id the ID of the role.
     * @return the {@link ResponseEntity} with body type {@link RoleDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get role with given id",
            nickname = "getRole",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Role found.", response = RoleDTO.class),
            @ApiResponse(code = 404, message = "Role cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(@ApiParam(value = "Id of role to be returned", required = true)
                                           @PathVariable("roleId") final Long id) {
        return ResponseEntity.ok(roleFacade.getRoleById(id));
    }


    /**
     * Gets all roles, not in the group with the given group ID.
     *
     * @param groupId  the ID of the group
     * @param pageable pageable parameter with information about pagination.
     * @param fields   attributes of the object to be returned as the result.
     * @return the {@link ResponseEntity} with body type {@link RoleDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all roles except roles of given group.",
            nickname = "getAllRolesNotInGivenGroup",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Roles found.", response = RoleRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/not-in-group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllRolesNotInGivenGroup(@ApiParam(value = "Filtering on Role entity attributes",required = false)
                                                             @QuerydslPredicate(root = Role.class) Predicate predicate,
                                                             Pageable pageable,
                                                             @ApiParam(value = "Id of group whose roles not to include",
                                                                     required = true)
                                                             @PathVariable("groupId") final Long groupId,
                                                             @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                             @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<RoleDTO> roleDTOs = roleFacade.getAllRolesNotInGivenGroup(groupId, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, roleDTOs));
    }


    /**
     * Gets users with a given role ID.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @param roleId    the ID of the role
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users with given role ID.",
            nickname = "getUsersWithGivenRole",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Users with specific role ID found.", response = UsersRestController.UserRestResource.class),
            @ApiResponse(code = 404, message = "Role cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/{roleId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenRole(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                        @QuerydslPredicate(root = User.class) Predicate predicate,
                                                        Pageable pageable,
                                                        @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                        @RequestParam(value = "fields", required = false) String fields,
                                                        @ApiParam(value = "Type of role to getGroupById users for.", required = true)
                                                        @PathVariable("roleId") Long roleId) {
        PageResultResource<UserDTO> userDTOs = userFacade.getUsersWithGivenRole(roleId, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }

    /**
     * Gets users with a given role type.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @param roleType  the type of the role
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users with given role type.",
            nickname = "getUsersWithGivenRoleType",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Users with specific role type found.", response = UsersRestController.UserRestResource.class),
            @ApiResponse(code = 404, message = "Role cannot be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenRoleType(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                            @QuerydslPredicate(root = User.class) Predicate predicate,
                                                            Pageable pageable,
                                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                            @RequestParam(value = "fields", required = false) String fields,
                                                            @ApiParam(value = "Type of role to getGroupById users for.", required = true)
                                                            @NotBlank @RequestParam("roleType") @Encoded String roleType) {
        PageResultResource<UserDTO> userDTOs = userFacade.getUsersWithGivenRoleType(roleType, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }


    /**
     * Gets users with a given role type and not with given ids.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @param roleType  the type of the role
     * @param userIds   ids of the users to be excluded from the result.
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users with given role and not with given ids.",
            nickname = "getUsersWithGivenRoleTypeAndNotWithGivenIds",
            notes = "Page size cannot be higher than 1000",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All roles found.", response = UsersRestController.UserRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/users-not-with-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenRoleTypeAndNotWithGivenIds(
            @ApiParam(value = "Filtering on User entity attributes", required = false)
            @QuerydslPredicate(root = User.class) Predicate predicate,
            Pageable pageable,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields,
            @ApiParam(value = "Type of role to getGroupById users for.", required = true)
            @RequestParam("roleType") String roleType,
            @ApiParam(value = "Ids of the users to be excluded from the result.", required = true)
            @RequestParam("ids") Set<Long> userIds) {
        if (pageable.getPageSize() >= 1000) {
            throw new BadRequestException("Choose page size lower than 1000");
        }
        PageResultResource<UserBasicViewDto> userDTOs = userFacade.getUsers(predicate, pageable, roleType, userIds);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }

    @ApiModel(value = "RoleRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages.")
    public static class RoleRestResource extends PageResultResource<RoleDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Roles from databases.")
        private List<RoleDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }


}
