package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
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

/**
 * @author Jan Duda
 * @author Pavel Seda
 */
@Api(value = "Endpoint for roles")
@RestController
@RequestMapping(path = "/roles")
public class RolesRestController {

    private static Logger LOG = LoggerFactory.getLogger(RolesRestController.class);

    private RoleFacade roleFacade;
    private UserFacade userFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public RolesRestController(RoleFacade roleFacade, UserFacade userFacade, ObjectMapper objectMapper) {
        this.roleFacade = roleFacade;
        this.objectMapper = objectMapper;
        this.userFacade = userFacade;
    }


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
        LOG.debug("getRoles()");
        PageResultResource<RoleDTO> roleDTOs = roleFacade.getAllRoles(predicate, pageable);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, roleDTOs), HttpStatus.OK);
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get role with given id",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(@ApiParam(value = "Id of role to be returned", required = true)
                                           @PathVariable("id") final Long id) {
        LOG.debug("getRole({})", id);
        try {
            return new ResponseEntity<>(roleFacade.getById(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException ex) {
            throw new ResourceNotFoundException("Role with given id " + id + " could not be found");
        }
    }

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
        LOG.debug("getUsersWithGivenRole()");
        try {
            PageResultResource<UserDTO> userDTOs = userFacade.getUsersWithGivenRole(roleId, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }

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
        LOG.debug("getUsersWithGivenRoleType()");
        try {
            PageResultResource<UserDTO> userDTOs = userFacade.getUsersWithGivenRole(roleType, pageable);
            Squiggly.init(objectMapper, fields);
            return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, userDTOs), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }
}
