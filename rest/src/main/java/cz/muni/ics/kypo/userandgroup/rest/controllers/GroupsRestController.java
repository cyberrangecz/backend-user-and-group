package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.base.Preconditions;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.exception.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.*;

import static cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup.GROUPS_URL;

@RestController
@RequestMapping(path = GROUPS_URL)
@Api(value = "Endpoint for Groups")
public class GroupsRestController {

    private static Logger LOGGER = LoggerFactory.getLogger(GroupsRestController.class);

    private IDMGroupFacade groupFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public GroupsRestController(IDMGroupFacade groupFacade, @Qualifier("objMapperRESTApi") ObjectMapper objectMapper) {
        this.groupFacade = groupFacade;
        this.objectMapper = objectMapper;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "POST", value = "Creates new group.", consumes = "application/json", produces = "application/json")
    public ResponseEntity<GroupDTO> createNewGroup(@ApiParam(value = "Group to be created.", required = true) @RequestBody NewGroupDTO newGroupDTO) {
        Preconditions.checkNotNull(newGroupDTO);
        try {
            return new ResponseEntity<>(groupFacade.createGroup(newGroupDTO), HttpStatus.CREATED);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Some of given groups could not be found in database.");
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Updates input group.", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> updateGroup(
            @ApiParam(value = "Group to be updated.", required = true) @RequestBody UpdateGroupDTO updateGroupDTO) {
        Preconditions.checkNotNull(updateGroupDTO);

        if (updateGroupDTO.getDescription() == null || updateGroupDTO.getName() == null) {
            throw new InvalidParameterException("Group name neither group description cannot be null.");
        }

        try {
            groupFacade.updateGroup(updateGroupDTO);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ExternalSourceException e) {
            throw new ResourceNotModifiedException("Group is external therefore they could not be updated");
        }
    }

    @PutMapping(path = "/{id}/removeUsers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Remove users from input group.")
    public ResponseEntity<Void> removeUsers(
            @ApiParam(value = "Id of group to remove users.", required = true) @PathVariable("id") final Long id,
            @ApiParam(value = "Ids of members to be removed from group.", required = true) @RequestBody List<Long> userIds) {
        Preconditions.checkNotNull(userIds);

        try {
            groupFacade.removeUsers(id, userIds);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Given group or users could not be found");
        } catch (ExternalSourceException e) {
            throw new ResourceNotModifiedException("Group is external therefore it could not be edited");
        }
    }

    @PutMapping(path = "/addUsers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Add users to group.")
    public ResponseEntity<GroupDTO> addUsers(
            @ApiParam(value = "Ids of members to be added and ids of groups of imported members to group.", required = true) @RequestBody AddUsersToGroupDTO addUsers) {
        Preconditions.checkNotNull(addUsers);

        try {
            groupFacade.addUsers(addUsers);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Given group or users could not be found");
        } catch (ExternalSourceException e) {
            throw new ResourceNotModifiedException("Group is external therefore it could not be edited");
        }
    }

    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "DELETE", value = "Tries to delete group with given id and returns if it was successful. \n" +
            "Statuses: 1) SUCCESS - group was deleted\n 2) HAS_ROLE - group has at least one role \n" +
            "3) EXTERNAL_VALID - group is from external source and was not marked as deleted",
            produces = "application/json")
    public ResponseEntity<GroupDeletionResponseDTO> deleteGroup(@ApiParam(value = "Id of group to be deleted.",
            required = true) @PathVariable("id") final Long id) {
        GroupDeletionResponseDTO groupDeletionResponseDTO = groupFacade.deleteGroup(id);

        switch (groupDeletionResponseDTO.getStatus()) {
            case SUCCESS:
                return new ResponseEntity<>(groupDeletionResponseDTO, HttpStatus.OK);
            case EXTERNAL_VALID:
            default:
                throw new MethodNotAllowedException("Group with id " + id + " cannot be deleted because is from external source and is valid group.");
        }
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "DELETE", value = "Tries to delete groups with given ids and returns groups and statuses of their deletion. \n" +
            "Statuses: 1) SUCCESS - group was deleted\n 2) HAS_ROLE - group has at least one role\n" +
            "3) EXTERNAL_VALID - group is from external source and was not marked as deleted\n" +
            "4) ERROR - group could not be deleted, try it later\n 5) NOT_FOUND - group could not be found",
            consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<GroupDeletionResponseDTO>> deleteGroups(@ApiParam(value = "Ids of groups to be deleted.", required = true)
                                                                       @RequestBody List<Long> ids) {
        Preconditions.checkNotNull(ids);
        return new ResponseEntity<>(groupFacade.deleteGroups(ids), HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get groups.", produces = "application/json")
    public ResponseEntity<Object> getGroups(@QuerydslPredicate(root = Role.class) Predicate predicate,
                                            @PageableDefault(size = 10, page = 0) Pageable pageable,
                                            @RequestParam MultiValueMap<String, String> parameters,
                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                            @RequestParam(value = "fields", required = false) String fields) {
        PageResultResource<GroupDTO> groupsDTOs = groupFacade.getAllGroups(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, groupsDTOs), HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get group with given id", produces = "application/json")
    public ResponseEntity<GroupDTO> getGroup(@ApiParam(value = "Id of group to be returned.",
            required = true) @PathVariable("id") Long id) {
        try {
            return new ResponseEntity<>(groupFacade.getGroup(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException ex) {
            throw new ResourceNotFoundException("Group with id " + id + " could not be found.");
        }
    }

    @GetMapping(path = "/{id}/roles")
    @ApiOperation(httpMethod = "GET", value = "Returns all roles of group with given id.")
    public ResponseEntity<Set<RoleDTO>> getRolesOfGroup(
            @ApiParam(value = "id", required = true) @PathVariable("id") final Long id) {
        try {
            return new ResponseEntity<>(groupFacade.getRolesOfGroup(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Group with id " + id + " could not be found or some of microservice did not return status code 2xx.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException("client error occurs during calling other microservice, probably due to wrong URL");
        }
    }

    @PutMapping(path = "/{groupId}/assign/{roleType}")
    @ApiOperation(httpMethod = "PUT", value = "Assigns role to group with given groupId")
    public ResponseEntity<Void> assignRole(
            @ApiParam(value = "groupId", required = true) @PathVariable("groupId") Long groupId,
            @ApiParam(value = "roleType", required = true) @PathVariable("roleType") RoleType roleType) {
        try {
            groupFacade.assignRole(groupId, roleType);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Group with id: " + groupId + " or one of the main roles (ADMINISTRATOR, USER, GUEST) could not be found.");
        }
    }

    @PutMapping("/{groupId}/assign/{roleId}/in/microservice/{microserviceId}")
    @ApiOperation(httpMethod = "PUT", value = "Assign role with given role ID to group with given ID in chosen microservice")
    public ResponseEntity<Void> assignRoleInMicroservice(
            @ApiParam(value = "groupId", required = true) @PathVariable("groupId") Long groupId,
            @ApiParam(value = "roleId", required = true) @PathVariable("roleId") Long roleId,
            @ApiParam(value = "microserviceId", required = true) @PathVariable("microserviceId") Long microserviceId) {
        try {
            groupFacade.assignRoleInMicroservice(groupId, roleId, microserviceId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Group with id: " + groupId + " or service with id " + microserviceId + " could not be found.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException("client error occurs during calling other microservice, probably due to wrong URL");
        }
    }

    @PutMapping("/{groupId}/cancel/{roleId}/in/microservice/{microserviceId}")
    @ApiOperation(httpMethod = "PUT", value = "Cancel role with given role ID to group with given ID in chosen microservice")
    public ResponseEntity<Void> cancelRoleInMicroservice(
            @ApiParam(value = "groupId", required = true) @PathVariable("groupId") Long groupId,
            @ApiParam(value = "roleId", required = true) @PathVariable("roleId") Long roleId,
            @ApiParam(value = "microserviceId", required = true) @PathVariable("microserviceId") Long microserviceId) {
        try {
            groupFacade.cancelRoleInMicroservice(groupId, roleId, microserviceId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Group with id: " + groupId + " or service with id " + microserviceId + " could not be found.");
        } catch (MicroserviceException e) {
            throw new ServiceUnavailableException("client error occurs during calling other microservice, probably due to wrong URL");
        }
    }
}
