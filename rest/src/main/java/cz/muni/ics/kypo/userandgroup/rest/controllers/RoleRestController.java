package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup.ROLES_URL;

@RestController
@RequestMapping(path = ROLES_URL)
@Api(value = "Endpoint for roles")
public class RoleRestController {

    private static Logger LOGGER = LoggerFactory.getLogger(RoleRestController.class);

    private RoleFacade roleFacade;

    private ObjectMapper objectMapper;

    @Autowired
    public RoleRestController(RoleFacade roleFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
        this.roleFacade = roleFacade;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get all roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getRoles(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        PageResultResource<RoleDTO> roleDTOs = roleFacade.getAllRoles(pageable);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, roleDTOs), HttpStatus.OK);

    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get role with given id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getRole(
            @ApiParam(value = "Id of role to be returned", required = true) @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(roleFacade.getById(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException ex) {
            throw new ResourceNotFoundException("Role with given id " + id + " could not be found");
        }

    }
}
