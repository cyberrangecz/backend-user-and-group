package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import cz.muni.ics.kypo.userandgroup.rest.utils.ApiPageableSwagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Jan Duda & Pavel Seda
 */
@RestController
@RequestMapping(path = "/users")
@Api(value = "Endpoint for Users")
public class UsersRestController {

    private static Logger LOGGER = LoggerFactory.getLogger(UsersRestController.class);

    private UserFacade userFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public UsersRestController(UserFacade userFacade, ObjectMapper objectMapper) {
        this.userFacade = userFacade;
        this.objectMapper = objectMapper;
    }

    @ApiOperation(httpMethod = "GET",
            value = "Gets all users.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsers(@QuerydslPredicate(root = Role.class) Predicate predicate,
                                           Pageable pageable,
                                           @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                           @RequestParam MultiValueMap<String, String> parameters,
                                           @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                           @RequestParam(value = "fields", required = false) String fields) {
        try {
            PageResultResource<UserDTO> userDTOs = userFacade.getUsers(predicate, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new InternalServerErrorException(e.getLocalizedMessage());
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException(e.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "GET",
            value = "Gets user with given id.",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUser(@ApiParam(value = "Id of user to be returned.",
            required = true) @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(userFacade.getUser(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("User with id " + id + " could not be found.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException(e.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "GET",
            value = "Gets all users except users in given group.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(path = "/not-in-groups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllUsersNotInGivenGroup(
            @ApiParam(value = "Id of group whose users do not get.", required = true)
            @PathVariable("groupId") final Long groupId,
            Pageable pageable,
            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
            @RequestParam(value = "fields", required = false) String fields) {
        try {
            PageResultResource<UserDTO> userDTOs = userFacade.getAllUsersNotInGivenGroup(groupId, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ServiceUnavailableException("Some error occurred while loading users not in group with id: " + groupId + ". Please, try it later.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException(e.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Tries to delete user with given screen name and returns if it was successful. \n" +
                    "Statuses: 1) SUCCESS - user was deleted\n 2) EXTERNAL_VALID - user is from external source and was not marked as deleted",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDeletionResponseDTO> deleteUser(
            @ApiParam(value = "Screen name of user to be deleted.", required = true)
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

    @ApiOperation(httpMethod = "DELETE",
            value = "Tries to delete users with given ids and returns users and statuses of their deletion. \n" +
                    "Statuses: 1) SUCCESS - user was deleted\n 2) EXTERNAL_VALID - user is from external source and was not marked as deleted\n" +
                    "3) ERROR - user could not be deleted, try it later\n 4) NOT_FOUND - user could not be found",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDeletionResponseDTO>> deleteUsers(@ApiParam(value = "Ids of users to be deleted.", required = true)
                                                                     @RequestBody List<Long> ids) {
        Preconditions.checkNotNull(ids);
        return new ResponseEntity<>(userFacade.deleteUsers(ids), HttpStatus.OK);
    }

    @ApiOperation(httpMethod = "GET",
            value = "Returns all roles of user with given id.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping(path = "/{id}/roles")
    public ResponseEntity<Set<RoleDTO>> getRolesOfUser(
            @ApiParam(value = "id", required = true) @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(userFacade.getRolesOfUser(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("User with id " + id + " could not be found.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException(e.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "GET",
            value = "Returns details of user who is logged in",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping(path = "/info")
    public ResponseEntity<UserInfoDTO> getUserInfo(OAuth2Authentication authentication) {
        try {
            return new ResponseEntity<>(userFacade.getUserInfo(authentication), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
            String sub = credentials.get("sub").getAsString();
            throw new ResourceNotFoundException("Logged in user with login " + sub + " could not be found in database.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException(e.getLocalizedMessage());
        }
    }
}
