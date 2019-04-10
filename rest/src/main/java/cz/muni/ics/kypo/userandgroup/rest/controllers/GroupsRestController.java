package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.base.Preconditions;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import cz.muni.ics.kypo.userandgroup.rest.utils.ApiPageableSwagger;
import io.swagger.annotations.*;
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
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * @author Jan Duda & Pavel Seda
 */
@Api(value = "Endpoint for Groups")
@RestController
@RequestMapping(path = "/groups")
public class GroupsRestController {

    private static Logger LOG = LoggerFactory.getLogger(GroupsRestController.class);

    private IDMGroupFacade groupFacade;
    private ObjectMapper objectMapper;

    @Autowired
    public GroupsRestController(IDMGroupFacade groupFacade, ObjectMapper objectMapper) {
        this.groupFacade = groupFacade;
        this.objectMapper = objectMapper;
    }

    @ApiOperation(httpMethod = "POST",
            value = "Create new group.",
            response = GroupDTO.class,
            nickname = "createNewGroup",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Given group created.", response = GroupDTO.class),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDTO> createNewGroup(@ApiParam(value = "Group to be created.", required = true)
                                                   @Valid @RequestBody NewGroupDTO newGroupDTO) {
        LOG.debug("createNewGroup({})", newGroupDTO);
        Preconditions.checkNotNull(newGroupDTO);
        try {
            return new ResponseEntity<>(groupFacade.createGroup(newGroupDTO), HttpStatus.CREATED);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException("Some of given groups could not be found in database.");
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Updates input group.",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGroup(@ApiParam(value = "Group to be updated.", required = true)
                                            @Valid @RequestBody UpdateGroupDTO updateGroupDTO) {
        LOG.debug("updateGroup({})", updateGroupDTO);
        Preconditions.checkNotNull(updateGroupDTO);
        try {
            groupFacade.updateGroup(updateGroupDTO);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ExternalSourceException e) {
            throw new ResourceNotModifiedException("Group is external therefore they could not be updated");
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Remove users from input group.",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping(path = "/{id}/users")
    public ResponseEntity<Void> removeUsers(@ApiParam(value = "Id of group to remove users.", required = true)
                                            @PathVariable("id") final Long id,
                                            @ApiParam(value = "Ids of members to be removed from group.", required = true)
                                            @RequestBody List<Long> userIds) {
        LOG.debug("removeUsers({}, {})", id, userIds);
        Preconditions.checkNotNull(userIds);
        try {
            groupFacade.removeUsers(id, userIds);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        } catch (ExternalSourceException e) {
            throw new ResourceNotModifiedException("Group is external therefore it could not be edited");
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Add users to group.",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PutMapping(path = "/{id}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addUsers(@ApiParam(value = "Id of group to add users.", required = true)
                                             @PathVariable("id") final Long groupId,
                                             @ApiParam(value = "Ids of members to be added and ids of groups of imported members to group.", required = true)
                                             @Valid @RequestBody AddUsersToGroupDTO addUsers) {
        LOG.debug("addUsers({})", addUsers);
        Preconditions.checkNotNull(addUsers);
        try {
            groupFacade.addUsers(groupId, addUsers);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        } catch (ExternalSourceException e) {
            throw new ResourceNotModifiedException("Group is external therefore it could not be edited");
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Tries to delete group with given id and returns if it was successful.",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDeletionResponseDTO> deleteGroup(@ApiParam(value = "Id of group to be deleted.", required = true)
                                                                @PathVariable("id") final Long id) {
        LOG.debug("deleteGroup({})", id);
        GroupDeletionResponseDTO groupDeletionResponseDTO = groupFacade.deleteGroup(id);
        switch (groupDeletionResponseDTO.getStatus()) {
            case SUCCESS:
                return new ResponseEntity<>(groupDeletionResponseDTO, HttpStatus.OK);
            case NOT_FOUND:
                throw new ResourceNotFoundException("Group with id " + id + " cannot be found.");
            case ERROR_MAIN_GROUP:
                throw new MethodNotAllowedException("Group with id " + id + " cannot be deleted because is main group.");
            case ERROR:
            default:
                return new ResponseEntity<>(groupDeletionResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Tries to delete groups with given ids and returns groups and statuses of their deletion.",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupDeletionResponseDTO>> deleteGroups(@ApiParam(value = "Ids of groups to be deleted.", required = true)
                                                                       @RequestBody List<Long> ids) {
        LOG.debug("deleteGroups({})", ids);
        Preconditions.checkNotNull(ids);
        return new ResponseEntity<>(groupFacade.deleteGroups(ids), HttpStatus.OK);
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get groups.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiPageableSwagger
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getGroups(@QuerydslPredicate(root = Role.class) Predicate predicate,
                                            Pageable pageable,
                                            @ApiParam(value = "Parameters for filtering the objects.", required = false)
                                            @RequestParam MultiValueMap<String, String> parameters,
                                            @ApiParam(value = "Fields which should be returned in REST API response", required = false)
                                            @RequestParam(value = "fields", required = false) String fields) {
        LOG.debug("getGroups()");
        PageResultResource<GroupDTO> groupsDTOs = groupFacade.getAllGroups(predicate, pageable);
        Squiggly.init(objectMapper, fields);
        return new ResponseEntity<>(SquigglyUtils.stringify(objectMapper, groupsDTOs), HttpStatus.OK);
    }

    @ApiOperation(httpMethod = "GET",
            value = "Get group with given id",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDTO> getGroup(@ApiParam(value = "Id of group to be returned.", required = true)
                                             @PathVariable("id") Long id) {
        LOG.debug("getGroup({})", id);
        try {
            return new ResponseEntity<>(groupFacade.getGroup(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "GET",
            value = "Returns all roles of group with given id.",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping(path = "/{id}/roles")
    public ResponseEntity<Set<RoleDTO>> getRolesOfGroup(@ApiParam(value = "id", required = true)
                                                        @PathVariable("id") final Long id) {
        LOG.debug("getRolesOfGroup({})", id);
        try {
            return new ResponseEntity<>(groupFacade.getRolesOfGroup(id), HttpStatus.OK);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "PUT",
            value = "Assign role with given role ID to group with given ID"
    )
    @PutMapping("/{groupId}/roles/{roleId}")
    public ResponseEntity<Void> assignRoleToGroup(@ApiParam(value = "groupId", required = true)
                                                  @PathVariable("groupId") Long groupId,
                                                  @ApiParam(value = "roleId", required = true)
                                                  @PathVariable("roleId") Long roleId) {
        LOG.debug("assignRoleToGroup");
        try {
            groupFacade.assignRole(groupId, roleId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        }
    }

    @ApiOperation(httpMethod = "DELETE",
            value = "Cancel role with given role ID to group with given ID"
    )
    @DeleteMapping("/{groupId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromGroup(@ApiParam(value = "groupId", required = true)
                                                    @PathVariable("groupId") Long groupId,
                                                    @ApiParam(value = "roleId", required = true)
                                                    @PathVariable("roleId") Long roleId) {
        LOG.debug("removeRoleToGroup()");
        try {
            groupFacade.removeRoleFromGroup(groupId, roleId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserAndGroupFacadeException e) {
            throw new ResourceNotFoundException(e.getLocalizedMessage());
        } catch (RoleCannotBeRemovedToGroupException e) {
            throw new ConflictException(e.getLocalizedMessage());
        }
    }
}
