package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.rest.ApiError;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.BadRequestException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.MethodNotAllowedException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.userandgroup.rest.utils.ApiPageableSwagger;
import cz.muni.ics.kypo.userandgroup.security.enums.AuthenticatedUserOIDCItems;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Rest controller for the User resource.
 *
 * @author Pavel Seda
 * @author Jan Duda
 */
@Api(value = "Endpoint for Users", tags = "users")
@RestController
@RequestMapping(path = "/users")
public class UsersRestController {

    private static Logger LOG = LoggerFactory.getLogger(UsersRestController.class);

    private UserFacade userFacade;
    private ObjectMapper objectMapper;

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
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param fields     attributes of the object to be returned as the result.
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
        PageResultResource<UserDTO> userDTOs = userFacade.getUsers(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
    }

    /**
     * Gets all users in groups with a given set of IDs.
     *
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param fields     attributes of the object to be returned as the result.
     * @param groupsIds  the groups ids
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
    @GetMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersInGroups(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                   @QuerydslPredicate(root = User.class) Predicate predicate,
                                                   Pageable pageable,
                                                   @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                   @RequestParam(value = "fields", required = false) String fields,
                                                   @ApiParam(value = "Ids of groups where users are assigned.", required = true)
                                                   @RequestParam("ids") Set<Long> groupsIds) {
        PageResultResource<UserForGroupsDTO> userDTOs = userFacade.getUsersInGroups(groupsIds, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
    }

    /**
     * Gets the user with the given ID.
     *
     * @param id the ID of the user.
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets user with given id.",
            nickname = "getUser",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User found.", response = UserRestResource.class),
            @ApiResponse(code = 404, message = "User not found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUser(@ApiParam(value = "Id of user to be returned.", required = true)
                                           @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(userFacade.getUser(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }

    /**
     * Delete the user with the given ID.
     *
     * @param id the ID of user to be deleted.
     * @return the {@link ResponseEntity} with body type {@link UserDeletionResponseDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "Delete user",
            nickname = "deleteUser",
            notes = "Tries to delete user with given screen name and returns status of its result.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returned deletion status of the user.", response = UserDeletionResponseDTO.class),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDeletionResponseDTO> deleteUser(@ApiParam(value = "Screen name of user to be deleted.", required = true)
                                                              @PathVariable("id") final Long id) {
        try {
            UserDeletionResponseDTO userDeletionResponseDTO = userFacade.deleteUser(id);

            switch (userDeletionResponseDTO.getStatus()) {
                case SUCCESS:
                    return new ResponseEntity<>(userDeletionResponseDTO, HttpStatus.OK);
                case EXTERNAL_VALID:
                default:
                    throw new MethodNotAllowedException("User with id " + id + " cannot be deleted because is from external source and is valid user.");
            }
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("User with id " + id + " could not be found.");
        }
    }

    /**
     * Gets all users, not in the group with the given group ID.
     *
     * @param groupId the ID of the group
     * @param pageable pageable parameter with information about pagination.
     * @param fields attributes of the object to be returned as the result.
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
    @GetMapping(path = "/not-in-groups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllUsersNotInGivenGroup(@ApiParam(value = "Filtering on User entity attributes", required = false)
                                                             @QuerydslPredicate(root = User.class) Predicate predicate,
                                                             Pageable pageable,
                                                             @ApiParam(value = "Id of group whose users do not get.", required = true)
                                                             @PathVariable("groupId") final Long groupId,
                                                             @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                             @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<UserDTO> userDTOs = userFacade.getAllUsersNotInGivenGroup(groupId, predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
    }

    /**
     * Delete users with a given list of IDs.
     *
     * @param ids a list of IDs of users.
     * @return the {@link ResponseEntity} with body type list of {@link UserDeletionResponseDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "DELETE",
            value = "DeleteUsers",
            nickname = "deleteUsers",
            notes = "Tries to delete users with given ids and returns users and statuses of their deletion.",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returned deletion status of the users.", response = UserDeletionResponseDTO[].class),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDeletionResponseDTO>> deleteUsers(@ApiParam(value = "Ids of users to be deleted.", required = true)
                                                                     @RequestBody List<Long> ids) {
        Preconditions.checkNotNull(ids);
        return new ResponseEntity<>(userFacade.deleteUsers(ids), HttpStatus.OK);
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
            @ApiResponse(code = 200, message = "Roles of the users.", response = RoleDTO[].class),
            @ApiResponse(code = 404, message = "User could not be found.", response = ApiError.class),
            @ApiResponse(code = 500, message = "Unexpected condition was encountered.", response = ApiError.class)
    })
    @GetMapping(path = "/{id}/roles")
    public ResponseEntity<Set<RoleDTO>> getRolesOfUser(
            @ApiParam(value = "id", required = true) @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(userFacade.getRolesOfUser(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("User with id " + id + " could not be found.");
        }
    }

    /**
     * Gets info about logged in user.
     *
     * @param authentication the authentication of the user.
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
    public ResponseEntity<UserDTO> getUserInfo(OAuth2Authentication authentication) {
        try {
            return new ResponseEntity<>(userFacade.getUserInfo(authentication), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
            String sub = credentials.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
            throw new ResourceNotFoundException("Logged in user with login " + sub + " could not be found in database.");
        }
    }

    /**
     * Gets users with a given set of ids.
     *
     * @param fields attributes of the object to be returned as the result.
     * @param ids set of ids of users to be loaded.
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
    @GetMapping(value = "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenIds(
            @ApiParam(value = "Filtering on User entity attributes", required = false)
            @QuerydslPredicate(root = User.class) Predicate predicate,
            Pageable pageable,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields,
            @ApiParam(value = "Ids of users to be obtained.", required = true)
            @RequestParam(value = "ids") Set<Long> ids) {
        if(pageable.getPageSize() >= 1000) {
            throw new BadRequestException("Choose page size lower than 1000");
        }
        PageResultResource<UserDTO> userDTOs;
        if (ids.isEmpty()) {
            userDTOs = new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0, 0, 0, 0, 0 ));
        } else {
            userDTOs = userFacade.getUsersWithGivenIds(ids, pageable, predicate);
        }
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
    }

    @ApiModel(value = "UserRestResource",
            description = "Content (Retrieved data) and meta information about REST API result page. Including page number, number of elements in page, size of elements, total number of elements and total number of pages")
    public static class UserRestResource extends PageResultResource<UserDTO> {
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Retrieved Roles from databases.")
        private List<UserDTO> content;
        @JsonProperty(required = true)
        @ApiModelProperty(value = "Pagination including: page number, number of elements in page, size, total elements and total pages.")
        private Pagination pagination;
    }

}
