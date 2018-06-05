package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.google.common.base.Preconditions;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.DTO.role.NewRoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotCreatedException;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get all roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RoleDTO>> getRoles() {
        List<Role> roles = roleService.getAllRoles();
        List<RoleDTO> roleDTOS = roles.stream().map(role -> convertToRoleDTO(role)).collect(Collectors.toList());

        return new ResponseEntity<>(roleDTOS, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get role with given id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleDTO> getRole(
            @ApiParam(value = "Id of role to be returned", required = true) @PathVariable("id") final Long id) {
        try {
            RoleDTO roleDTO = convertToRoleDTO(roleService.getById(id));
            return new ResponseEntity<>(roleDTO, HttpStatus.OK);
        } catch (IdentityManagementException ex) {
            throw new ResourceNotFoundException("Role with given id could not be found");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "POST", value = "Creates new user.", consumes = "application/json", produces = "application/json")
    public ResponseEntity<RoleDTO> createNewRole(@ApiParam(value = "Role to be created.",
            required = true) @RequestBody NewRoleDTO newRoleDTO) {
        Preconditions.checkNotNull(newRoleDTO);
        try {
            Role role = convertToRole(newRoleDTO);

            role = roleService.create(role);
            RoleDTO r = convertToRoleDTO(role);
            return new ResponseEntity<>(r, HttpStatus.OK);
        } catch (IdentityManagementException e) {
            throw new ResourceNotCreatedException("Invalid role's information or could not be created.");
        }
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setRoleType(role.getRoleType());
        return roleDTO;
    }

    private Role convertToRole(NewRoleDTO roleDTO) {
        Role role = new Role();
        role.setRoleType(roleDTO.getRoleType());
        return role;
    }
}
