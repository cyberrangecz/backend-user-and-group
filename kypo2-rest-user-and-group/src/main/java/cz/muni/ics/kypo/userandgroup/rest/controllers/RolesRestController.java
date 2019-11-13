package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.BadRequestException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Rest controller for the Role resource.
 *
 * @author Pavel Seda
 * @author Jan Duda
 */
@Api(value = "Endpoint for roles")
@RestController
@RequestMapping(path = "/roles")
public class RolesRestController {

    private static Logger LOG = LoggerFactory.getLogger(RolesRestController.class);

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
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @return the roles
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get all roles",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getRoles(@QuerydslPredicate(root = Role.class) Predicate predicate,
                                           Pageable pageable,
                                           @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                           @RequestParam MultiValueMap<String, String> parameters,
                                           @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                           @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<RoleDTO> roleDTOs = roleFacade.getAllRoles(predicate, pageable);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, roleDTOs), HttpStatus.OK);
    }

    /**
     * Gets the role with the given ID.
     *
     * @param id the ID of the role.
     * @return the {@link ResponseEntity} with body type {@link RoleDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Get role with given id",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(@ApiParam(value = "Id of role to be returned", required = true)
                                           @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(roleFacade.getById(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException ex) {
            throw new ResourceNotFoundException("Role with given id " + id + " could not be found");
        }
    }

    /**
     * Gets users with a given role.
     *
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @param roleId     the ID of the role
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users with given role.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(path = "/{roleId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenRole(@QuerydslPredicate(root = User.class) Predicate predicate,
                                                        Pageable pageable,
                                                        @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                        @RequestParam MultiValueMap<String, String> parameters,
                                                        @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                        @RequestParam(value = "fields", required = false) String fields,
                                                        @ApiParam(value = "Type of role to get users for.", required = true)
                                                        @PathVariable("roleId") Long roleId) {
        try {
            PageResultResource<UserDTO> userDTOs = userFacade.getUsersWithGivenRole(roleId, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }

    /**
     * Gets users with a given role type.
     *
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @param roleType   the type of the role
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users with given role.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenRoleType(@QuerydslPredicate(root = User.class) Predicate predicate,
                                                            Pageable pageable,
                                                            @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                            @RequestParam MultiValueMap<String, String> parameters,
                                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                            @RequestParam(value = "fields", required = false) String fields,
                                                            @ApiParam(value = "Type of role to get users for.", required = true)
                                                            @RequestParam("roleType") String roleType) {
        try {
            Predicate usersWithRoles = QUser.user.groups.any().roles.any().roleType.eq(roleType).and(predicate);
            PageResultResource<UserDTO> userDTOs = userFacade.getUsers(usersWithRoles, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }


    /**
     * Gets users with a given role type and not with given ids.
     *
     * @param predicate  specifies query to database.
     * @param pageable   pageable parameter with information about pagination.
     * @param parameters the parameters
     * @param fields     attributes of the object to be returned as the result.
     * @param roleType   the type of the role
     * @param userIds    ids of the users to be excluded from the result.
     * @return the {@link ResponseEntity} with body type {@link UserDTO} and specific status code and header.
     */
    @ApiOperation(httpMethod = "GET",
            value = "Gets all users with given role and not with given ids.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(path = "/users-not-with-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsersWithGivenRoleTypeAndNotWithGivenIds(@QuerydslPredicate(root = User.class) Predicate predicate,
                                                            Pageable pageable,
                                                            @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                                            @RequestParam MultiValueMap<String, String> parameters,
                                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                                            @RequestParam(value = "fields", required = false) String fields,
                                                            @ApiParam(value = "Type of role to get users for.", required = true)
                                                            @RequestParam("roleType") String roleType,
                                                            @ApiParam(value = "Ids of the users to be excluded from the result.", required = true)
                                                            @RequestParam("ids") Set<Long> userIds) {
        if(pageable.getPageSize() >= 1000) {
            throw new BadRequestException("Choose page size lower than 1000");
        }
        try {
            Predicate usersWithRoles = QUser.user.groups.any().roles.any().roleType.eq(roleType);
            Predicate finalPredicate = QUser.user.id.notIn(userIds).and(usersWithRoles).and(predicate);
            PageResultResource<UserDTO> userDTOs = userFacade.getUsers(finalPredicate, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }
}
