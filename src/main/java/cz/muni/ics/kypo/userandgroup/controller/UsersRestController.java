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
import cz.muni.ics.kypo.userandgroup.dto.UsersImportDTO;
import cz.muni.ics.kypo.userandgroup.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.*;
import cz.muni.ics.kypo.userandgroup.exceptions.BadRequestException;
import cz.muni.ics.kypo.userandgroup.exceptions.errors.ApiError;
import cz.muni.ics.kypo.userandgroup.facade.UserFacade;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Rest controller for the User resource.
 */
@Api(value = "Endpoint for Users",
        tags = "users",
        authorizations = @Authorization(value = "bearerAuth"))
@RestController
@RequestMapping(path = "/users")
@Validated
public class UsersRestController {

    private static final Logger LOG = LoggerFactory.getLogger(UsersRestController.class);

    private final UserFacade userFacade;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new UsersRestController.
     *
     * @param userFacade   the user facade
     * @param objectMapper the object mapper
     */
    @Autowired
    public UsersRestController(UserFacade userFacade, ObjectMapper objectMapper) {
        this.userFacade = userFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * Gets users.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @return the users
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users.",
            nickname = "getUsers",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Users found.", response = UserRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsers(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                           @QuerydslPredicate(root = User.class) Predicate predicate,
                                           Pageable pageable,
                                           @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                           @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<UserBasicViewDto> userDTOs = userFacade.getUsers(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }

    /**
     * Gets all users in groups with a given set of IDs.
     *
     * @param predicate specifies query to database.
     * @param pageable  pageable parameter with information about pagination.
     * @param fields    attributes of the object to be returned as the result.
     * @param groupsIds the groups ids
     * @return the users in groups
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets users in given groups.",
            nickname = "getUsersInGroups",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Users in specific groups found.", response = UserRestResource.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersInGroups(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                   @QuerydslPredicate(root = User.class) Predicate predicate,
                                                   Pageable pageable,
                                                   @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                   @RequestParam(value = "fields", required = false) String fields,
                                                   @ApiParam(value = "Ids of groups where users are assigned.", required = true)
                                                   @RequestParam("ids") Set<Long> groupsIds) {
        PageResultResource<UserForGroupsDTO> userDTOs = userFacade.getUsersInGroups(groupsIds, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }

    /**
     * Gets the user with the given ID.
     *
     * @param id the ID of the user.
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets user with given id.",
            nickname = "getUserById",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User found.", response = UserRestResource.class),
            @ApiResponse(code = 404, message = "User not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUser(@ApiParam(value = "Id of user to be returned.", required = true)
                                           @PathVariable("userId") final Long id) {
        return ResponseEntity.ok(userFacade.getUserById(id));
    }


    /**
     * Gets all users, not in the group with the given group ID.
     *
     * @param groupId  the ID of the group
     * @param pageable pageable parameter with information about pagination.
     * @param fields   attributes of the object to be returned as the result.
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users except users in given group.",
            nickname = "getAllUsersNotInGivenGroup",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User found.", response = UserRestResource.class),
            @ApiResponse(code = 404, message = "Some user could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/not-in-group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllUsersNotInGivenGroup(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                             @QuerydslPredicate(root = User.class) Predicate predicate,
                                                             Pageable pageable,
                                                             @ApiParam(value = "Id of group whose users do not getGroupById.", required = true)
                                                             @PathVariable("groupId") final Long groupId,
                                                             @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                             @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<UserDTO> userDTOs = userFacade.getAllUsersNotInGivenGroup(groupId, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }

    /**
     * Delete the user with the given ID.
     *
     * @param id the ID of user to be deleted.
     * @return the response entity with specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete user",
            nickname = "deleteUser",
            notes = "Delete user based on given id.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returned HTTP status OK."),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteUser(@ApiParam(value = "Screen name of user to be deleted.", required = true)
                                           @PathVariable("userId") final Long id) {
        userFacade.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Delete users with a given list of IDs.
     *
     * @param ids a list of IDs of users.
     * @return the response entity with specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "DeleteUsers",
            nickname = "deleteUsers",
            notes = "Delete users based on given ids.",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returned HTTP status OK."),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteUsers(@ApiParam(value = "Ids of users to be deleted.", required = true)
                                            @RequestBody List<@NotNull Long> ids) {
        userFacade.deleteUsers(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets the roles of users with the given ID.
     *
     * @param id the ID of the user.
     * @return the {@link ResponseEntity} with body type set of {@link RoleDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Returns all roles of user with given id.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Roles of the users.", response = RolesRestController.RoleRestResource.class),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{id}/roles")
    @ApiPageableSwagger
    public ResponseEntity<PageResultResource<RoleDTO>> getRolesOfUser(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                                      @QuerydslPredicate(root = Role.class) Predicate predicate,
                                                                      Pageable pageable,
                                                                      @ApiParam(value = "id", required = true)
                                                                      @PathVariable("id") final Long id) {
        return ResponseEntity.ok(userFacade.getRolesOfUserWithPagination(id, pageable, predicate));
    }

    /**
     * Gets info about logged in user.
     *
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get user info",
            nickname = "getUserInfo",
            notes = "Returns details of user who is logged in.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Base user info found.", response = UserDTO.class),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/info")
    public ResponseEntity<UserDTO> getUserInfo() {
        return ResponseEntity.ok(userFacade.getUserInfo());
    }

    /**
     * Gets users with a given set of ids.
     *
     * @param fields attributes of the object to be returned as the result.
     * @param ids    set of ids of users to be loaded.
     * @return the {@link ResponseEntity} with body type set of {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets users with given ids.",
            nickname = "getUsersWithGivenIds",
            notes = "Page size cannot be higher than 1000",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Users with given ids found.", response = UserRestResource.class),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @ApiPageableSwagger
    @GetMapping(path = "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenIds(
            @ApiParam(value = "Filtering on User entity attributes", required = false)
            @QuerydslPredicate(root = User.class) Predicate predicate,
            Pageable pageable,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields,
            @ApiParam(value = "Ids of users to be obtained.", required = true)
            @RequestParam(value = "ids") List<Long> ids) {
        if (pageable.getPageSize() >= 1000) {
            throw new BadRequestException("Choose page size lower than 1000");
        }
        PageResultResource<UserBasicViewDto> userDTOs;
        if (ids.isEmpty()) {
            userDTOs = new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0, 0, 0, 0, 0));
        } else {
            userDTOs = userFacade.getUsersWithGivenIds(ids, pageable, predicate);
        }
        Squiggly.init(objectMapper, fields);
        return ResponseEntity.ok(SquigglyUtils.stringify(objectMapper, userDTOs));
    }

    /**
     * Gets initial OIDC users.
     *
     * @return the {@link ResponseEntity} with body type array of {@link InitialOIDCUserDto} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get initial oidc users",
            nickname = "getInitialOIDCUsers",
            notes = "Returns details of initial OIDC users.",
            produces = "application/octet-stream"
    )
    @GetMapping(path = "/initial-oidc-users", produces = "application/octet-stream")
    public ResponseEntity<byte[]> getInitialOIDCUsers() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("Content-Disposition", "attachment; filename=oidc-initial-users.yaml")
                .body(userFacade.getInitialOIDCUsers());
    }

    /**
     * Import new users. It is possible to assign them to newly created group.
     *
     * @param usersImportDTO object containing users to be imported along with  new group to which they will be assigned.
     * @return the {@link ResponseEntity} with body type {@link GroupDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "POST",
            value = "Import new users.",
            nickname = "importUsers"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Given group created.", response = GroupDTO.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> importUsers(@ApiParam(value = "Object with users to be imported", required = true)
                                                   @Valid @RequestBody UsersImportDTO usersImportDTO) {
        userFacade.importUsers(usersImportDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @ApiModel(value = "UserRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages.")
    public static class UserRestResource extends PageResultResource<UserDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved users from databases.")
        private List<UserDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

}
