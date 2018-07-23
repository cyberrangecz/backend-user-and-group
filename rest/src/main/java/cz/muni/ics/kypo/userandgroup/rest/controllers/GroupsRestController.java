package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.google.common.base.Preconditions;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup.GROUPS_URL;

@RestController
@RequestMapping(path = GROUPS_URL)
@Api(value = "Endpoint for Groups")
public class GroupsRestController {

    private static Logger LOGGER = LoggerFactory.getLogger(GroupsRestController.class);

    private IDMGroupService groupService;

    private UserService userService;

    private BeanMapping beanMapping;

    @Autowired
    public GroupsRestController(IDMGroupService groupService, UserService userService, BeanMapping beanMapping) {
        this.groupService = groupService;
        this.userService = userService;
        this.beanMapping = beanMapping;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "POST", value = "Creates new group.", consumes = "application/json")
    public ResponseEntity<GroupDTO> createNewGroup(@ApiParam(value = "Group to be created.",
            required = true) @RequestBody NewGroupDTO newGroupDTO) {
        Preconditions.checkNotNull(newGroupDTO);
        try {
            IDMGroup group = convertToGroup(newGroupDTO);
            group = groupService.create(group);
            GroupDTO g = convertToGroupDTO(group);
            return new ResponseEntity<>(g, HttpStatus.CREATED);
        } catch (UserAndGroupServiceException e) {
            throw new ResourceNotCreatedException("Invalid group's information or could not be created in database.");
        }
    }


    private IDMGroup convertToGroup(NewGroupDTO newGroupDTO) {
        IDMGroup group = beanMapping.mapTo(newGroupDTO, IDMGroup.class);
        if (newGroupDTO.getMembers() != null) {

            newGroupDTO.getMembers().forEach(userForGroupsDTO -> {
                User user = userService.getUserByLogin(userForGroupsDTO.getLogin());
                group.addUser(user);
            });
        }

        if (newGroupDTO.getGroupIdsOfImportedMembers() != null) {
            addMembersFromGroups(newGroupDTO.getGroupIdsOfImportedMembers(), group);
        }

        return group;
    }

    private void addMembersFromGroups(List<Long> groupIds, IDMGroup group) {
        for (Long groupId : groupIds) {
            IDMGroup groupOfImportedMembers = groupService.getIDMGroupWithUsers(groupId);
            groupOfImportedMembers.getUsers().forEach((u) -> {
                if (!group.getUsers().contains(u)) {
                    group.addUser(u);
                }
            });
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Updates input group.")
    public ResponseEntity<GroupDTO> updateGroup(
            @ApiParam(value = "Group to be updated.", required = true) @RequestBody UpdateGroupDTO groupDTO) {
        Preconditions.checkNotNull(groupDTO);

        if (!groupService.isGroupInternal(groupDTO.getId())) {
            throw new InvalidParameterException("Group is external therefore they could not be updated");
        }
        if (groupDTO.getDescription() == null || groupDTO.getName() == null) {
            throw new InvalidParameterException("Group name neither group description cannot be null.");
        }

        try {
            IDMGroup groupToUpdate = groupService.getIDMGroupWithUsers(groupDTO.getId());
            groupToUpdate.setName(groupDTO.getName());
            groupToUpdate.setDescription(groupDTO.getDescription());
            groupToUpdate = groupService.update(groupToUpdate);
            GroupDTO g = convertToGroupDTO(groupToUpdate);

            return new ResponseEntity<>(g, HttpStatus.OK);
        } catch (UserAndGroupServiceException e) {
            throw new ResourceNotModifiedException("Group could not be modified.");
        }
    }

    @PutMapping(path = "/{id}/removeMembers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Remove members from input group.")
    public ResponseEntity<GroupDTO> removeMembers(
            @ApiParam(value = "Id of group to remove members.", required = true) @PathVariable("id") final Long id,
            @ApiParam(value = "Ids of members to be removed from group.", required = true) @RequestBody List<Long> userIds) {
        Preconditions.checkNotNull(userIds);

        if (!groupService.isGroupInternal(id)) {
            throw new InvalidParameterException("Group is external therefore they could not be updated");
        }

        try {
            IDMGroup groupToUpdate = groupService.getIDMGroupWithUsers(groupService.get(id).getName());
            for (Long userId : userIds) {
                groupToUpdate.removeUser(userService.get(userId));
            }
            groupToUpdate = groupService.update(groupToUpdate);
            GroupDTO g = convertToGroupDTO(groupToUpdate);

            return new ResponseEntity<>(g, HttpStatus.OK);
        } catch (UserAndGroupServiceException e) {
            throw new ResourceNotModifiedException("Group could not be modified.");
        }
    }

    @PutMapping(path = "/addMembers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Add members to group.")
    public ResponseEntity<GroupDTO> addMembers(
            @ApiParam(value = "Ids of members to be added and ids of groups of imported members to group.", required = true) @RequestBody AddMembersToGroupDTO addMembers) {
        Preconditions.checkNotNull(addMembers);


        if (!groupService.isGroupInternal(addMembers.getGroupId())) {
            throw new InvalidParameterException("Group is external therefore they could not be updated");
        }

        try {
            IDMGroup groupToUpdate = groupService.getIDMGroupWithUsers(groupService.get(addMembers.getGroupId()).getName());
            if (addMembers.getIdsOfUsersToBeAdd() != null) {
                for (Long userId : addMembers.getIdsOfUsersToBeAdd()) {
                    groupToUpdate.addUser(userService.get(userId));

                }

            }
            if (addMembers.getIdsOfGroupsOfImportedUsers() != null) {
                addMembersFromGroups(addMembers.getIdsOfGroupsOfImportedUsers(), groupToUpdate);
            }
            groupToUpdate = groupService.update(groupToUpdate);
            GroupDTO g = convertToGroupDTO(groupToUpdate);

            return new ResponseEntity<>(g, HttpStatus.OK);
        } catch (UserAndGroupServiceException e) {
            throw new ResourceNotModifiedException("Group could not be modified.");
        }
    }

    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "DELETE", value = "Tries to delete group with given id and returns if it was successful. \n" +
            "Statuses: 1) SUCCESS - group was deleted\n 2) HAS_ROLE - group has at least one role \n" +
            "3) EXTERNAL_VALID - group is from external source and was not marked as deleted",
            produces = "application/json")
    public ResponseEntity<GroupDeletionResponseDTO> deleteGroup(@ApiParam(value = "Id of group to be deleted.",
            required = true) @PathVariable("id") final Long id) {

        IDMGroup group = null;
        try {
            group = groupService.get(id);
        } catch (UserAndGroupServiceException e) {
            throw new ResourceNotFoundException("Group with id " + id + " could not be found.");
        }

        try {
            GroupDeletionStatus deletionStatus = groupService.delete(group);
            GroupDeletionResponseDTO groupDeletionResponseDTO = beanMapping.mapTo(group, GroupDeletionResponseDTO.class);
            groupDeletionResponseDTO.setStatus(deletionStatus);

            switch (deletionStatus) {
                case SUCCESS:
                    return new ResponseEntity<>(groupDeletionResponseDTO, HttpStatus.OK);
                case EXTERNAL_VALID:
                default:
                    throw new MethodNotAllowedException("Group with name " + group.getName() + " cannot be deleted because is from external source and is valid group.");
            }
        } catch (UserAndGroupServiceException e) {
            throw new ServiceUnavailableException("Some error occurred during deletion of group with name " + group.getName() + ". Please, try it later.");
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

        ids.forEach(id -> LOGGER.info(String.valueOf(id)));

        Map<IDMGroup, GroupDeletionStatus> mapOfResults = groupService.deleteGroups(ids);
        List<GroupDeletionResponseDTO> response = new ArrayList<>();

        mapOfResults.forEach((group, status) -> {
            GroupDeletionResponseDTO r = new GroupDeletionResponseDTO();
            r.setId(group.getId());
            r.setName(group.getName());
            r.setStatus(status);
            response.add(r);
        });

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get groups.", produces = "application/json")
    public ResponseEntity<List<GroupDTO>> getGroups(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<IDMGroup> groups;
        try {
            groups = groupService.getAllIDMGroups(pageable);
        } catch (UserAndGroupServiceException e) {
            throw new ServiceUnavailableException("Error while loading all groups from database.");
        }
        List<GroupDTO> groupDTOs = new ArrayList<>();

        groups.getContent().forEach(group -> {
//            IDMGroup g = groupService.getIDMGroupWithUsers(group.getId());
            groupDTOs.add(convertToGroupDTO(group));
        });

        return new ResponseEntity<>(groupDTOs, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Get group with given id", produces = "application/json")
    public ResponseEntity<GroupDTO> getGroup(@ApiParam(value = "Id of group to be returned.",
            required = true) @PathVariable("id") Long id) {
        try {
            GroupDTO groupDTO = convertToGroupDTO(groupService.get(id));

            return new ResponseEntity<>(groupDTO, HttpStatus.OK);
        } catch (UserAndGroupServiceException ex) {
            throw new ServiceUnavailableException("Some error occurred while loading group with id: " + id + ". Please, try it later.");
        }
    }

    @GetMapping(path = "/{id}/roles")
    @ApiOperation(httpMethod = "GET", value = "Returns all roles of group with given id.")
    public ResponseEntity<Set<RoleDTO>> getRolesOfGroup(
            @ApiParam(value = "id", required = true) @PathVariable("id") final Long id) {

        Set<Role> roles = groupService.getRolesOfGroup(id);
        Set<RoleDTO> roleDTOS = roles.stream().map(role -> beanMapping.mapTo(role, RoleDTO.class))
                .collect(Collectors.toSet());
        return new ResponseEntity<>(roleDTOS, HttpStatus.OK);
    }

    @PutMapping(path = "/{groupId}/assign/{roleType}")
    @ApiOperation(httpMethod = "PUT", value = "Assigns role to group with given groupId")
    public ResponseEntity<GroupDTO> assignRole(
            @ApiParam(value = "groupId", required = true) @PathVariable("groupId") Long groupId,
            @ApiParam(value = "roleType", required = true) @PathVariable("roleType") RoleType roleType) {

        IDMGroup updatedGroup = groupService.assignRole(groupId, roleType);
        return new ResponseEntity<>(convertToGroupDTO(updatedGroup), HttpStatus.OK);
    }

    @PutMapping("/{groupId}/assign/{roleId}/in/microservice/{microserviceId}")
    @ApiOperation(httpMethod = "PUT", value = "Assign role with given role ID to group with given ID in chosen microservice")
    public ResponseEntity<GroupDTO> assignRoleInMicroservice(
            @ApiParam(value = "groupId", required = true) @PathVariable("groupId") Long groupId,
            @ApiParam(value = "roleId", required = true) @PathVariable("roleId") Long roleId,
            @ApiParam(value = "microserviceId", required = true) @PathVariable("microserviceId") Long microserviceId) {

        IDMGroup updatedGroup = groupService.assignRoleInMicroservice(groupId, roleId, microserviceId);
        return new ResponseEntity<>(convertToGroupDTO(updatedGroup), HttpStatus.OK);
    }

    private UserForGroupsDTO convertToUserForGroupsDTO(User user) {
        UserForGroupsDTO userForGroup = beanMapping.mapTo(user, UserForGroupsDTO.class);
        userForGroup.convertScreenNameToLogin(user.getLogin());
        return userForGroup;
    }

    private GroupDTO convertToGroupDTO(IDMGroup g) {
        GroupDTO groupDTO = beanMapping.mapTo(g, GroupDTO.class);
        groupDTO.convertExternalIdToSource(g.getExternalId());
        groupDTO.convertStatusToCanBeDeleted(g.getStatus());

        List<UserForGroupsDTO> users = g.getUsers().stream().map(this::convertToUserForGroupsDTO)
                .collect(Collectors.toList());
        groupDTO.setMembers(users);

        Set<RoleDTO> roleDTOS = g.getRoles().stream().map(role -> beanMapping.mapTo(role, RoleDTO.class))
                .collect(Collectors.toSet());
        groupDTO.setRoles(roleDTOS);

        return groupDTO;
    }
}
