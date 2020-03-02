package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.rest.exceptionhandling.ApiError;
import cz.muni.ics.kypo.userandgroup.api.exceptions.BadRequestException;
import cz.muni.ics.kypo.userandgroup.rest.utils.ApiPageableSwagger;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Rest controller for the Role resource.
 */
@Api(value = "Endpoint for Roles", tags = "roles")
@RestController
@RequestMapping(path = "/roles")
public class RolesRestController {

    private RoleFacade roleFacade;
    private UserFacade userFacade;
    private ObjectMapper objectMapper;

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
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(@ApiParam(value = "Id of role to be returned", required = true)
                                           @PathVariable("id") final Long id) {
        return ResponseEntity.ok(roleFacade.getRoleById(id));
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
                                                            @NotBlank @RequestParam("roleType") String roleType) {
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
        PageResultResource<UserDTO> userDTOs = userFacade.getUsers(predicate, pageable, roleType, userIds);
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
