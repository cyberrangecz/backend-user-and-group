package cz.muni.ics.kypo.userandgroup.rest.controllers;

import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.DTO.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.userandgroup.rest.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup.ROLES_URL;

@RestController
@RequestMapping(path = ROLES_URL)
@Api(value = "Endpoint for roles")
public class RoleRestController {

    private static Logger LOGGER = LoggerFactory.getLogger(RoleRestController.class);

    private RoleService roleService;

    private BeanMapping beanMapping;

    @Autowired
    public RoleRestController(RoleService roleService, BeanMapping beanMapping) {
        this.roleService = roleService;
        this.beanMapping = beanMapping;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get all roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RoleDTO>> getRoles(Pageable pageable) {
        List<Role> roles = roleService.getAllRoles(pageable).getContent();
        List<RoleDTO> roleDTOS = roles.stream().map(role -> beanMapping.mapTo(role, RoleDTO.class)).collect(Collectors.toList());

        return new ResponseEntity<>(roleDTOS, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get role with given id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(
            @ApiParam(value = "Id of role to be returned", required = true) @PathVariable("id") final Long id) {
        try {
            RoleDTO roleDTO = beanMapping.mapTo(roleService.getById(id), RoleDTO.class);
            return new ResponseEntity<>(roleDTO, HttpStatus.OK);
        } catch (IdentityManagementException ex) {
            throw new ResourceNotFoundException("Role with given id could not be found");
        }
    }
}
