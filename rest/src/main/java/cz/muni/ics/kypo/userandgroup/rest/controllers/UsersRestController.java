package cz.muni.ics.kypo.userandgroup.rest.controllers;

import com.google.common.base.Preconditions;
import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import cz.muni.ics.kypo.userandgroup.dbmodel.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.rest.ApiEndpointsUserAndGroup;
import cz.muni.ics.kypo.userandgroup.rest.DTO.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.rest.DTO.user.*;
import cz.muni.ics.kypo.userandgroup.rest.exceptions.*;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = ApiEndpointsUserAndGroup.USERS_URL)
@Api(value = "Endpoint for Users")
public class UsersRestController {

    private static Logger LOGGER = LoggerFactory.getLogger(UsersRestController.class);

    private IDMGroupService groupService;

    private UserService userService;

    @Autowired
    public UsersRestController(UserService userService, IDMGroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Gets all users.", produces = "application/json")
    public ResponseEntity<List<UserDTO>> getUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOS = users.stream().map(user -> convertToUserDTO(user))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(userDTOS, HttpStatus.OK);
        } catch (IdentityManagementException e) {
            throw new ServiceUnavailableException("Some error occurred while loading all users. Please, try it later.");
        }
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Gets user with given id.", produces = "application/json")
    public ResponseEntity<UserDTO> getUser(@ApiParam(value = "Id of user to be returned.",
            required = true) @PathVariable("id") final Long id) {
        try {
            UserDTO userDTO = convertToUserDTO(userService.get(id));

            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (IdentityManagementException e) {
            throw new ServiceUnavailableException("Some error occurred while loading user with id: " + id + ". Please, try it later.");
        }
    }

    @GetMapping(path = "/except/in/group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "GET", value = "Gets all users except users in given group.", produces = "application/json")
    public ResponseEntity<List<UserDTO>> getAllUsersNotInGivenGroup(@ApiParam(value = "Id of group whose users do not get.",
            required = true) @PathVariable("groupId") final Long groupId) {
        try {
            List<User> users = new ArrayList<>(userService.getAllUsers());
            IDMGroup group = groupService.getIDMGroupWithUsers(groupId);
            for (User u : group.getUsers()) {
                users.remove(u);

            }
            List<UserDTO> userDTOS = users.stream().map(user -> convertToUserDTO(user))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(userDTOS, HttpStatus.OK);
        } catch (IdentityManagementException e) {
            throw new ServiceUnavailableException("Some error occurred while loading users not in group with id: " + groupId + ". Please, try it later.");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "POST", value = "Creates new user.", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserDTO> createNewUser(@ApiParam(value = "User to be created.",
            required = true) @RequestBody NewUserDTO newUserDTO) {
        Preconditions.checkNotNull(newUserDTO);
        try {
            User user = convertToUser(newUserDTO);

            user = userService.create(user);
            UserDTO s = convertToUserDTO(user);
            return new ResponseEntity<>(s, HttpStatus.CREATED);
        } catch (IdentityManagementException e) {
            throw new ResourceNotCreatedException("Invalid user's information or could not be created.");
        }
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "PUT", value = "Updates input internal user.", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserDTO> updateUser(@ApiParam(value = "User to be updated.", required = true)
                                              @RequestBody UpdateUserDTO updateUserDTO) {
        Preconditions.checkNotNull(updateUserDTO);

        if (!userService.isUserInternal(updateUserDTO.getId())) {
            throw new InvalidParameterException("User is external therefore they could not be updated");
        }

        try {
            User updateUser = convertToUser(updateUserDTO);
            User u = userService.update(updateUser);
            UserDTO s = convertToUserDTO(u);
            return new ResponseEntity<>(s, HttpStatus.OK);
        } catch (IdentityManagementException e) {
            throw new ResourceNotModifiedException("User could not be updated");
        }
    }

    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "DELETE", value = "Tries to delete user with given screen name and returns if it was successful. \n" +
            "Statuses: 1) SUCCESS - user was deleted\n 2) EXTERNAL_VALID - user is from external source and was not marked as deleted",
            produces = "application/json")
    public ResponseEntity<UserDeletionResponseDTO> deleteUser(@ApiParam(value = "Screen name of user to be deleted.",
            required = true) @PathVariable("id") final Long id) {

        User user = null;
        try {
            user = userService.get(id);
        } catch (IdentityManagementException e) {
            throw new ResourceNotFoundException("User with id " + id + " could not be found.");
        }

        try {
            UserDeletionStatus deletionStatus = userService.delete(user);
            UserDeletionResponseDTO userDeletionResponseDTO = new UserDeletionResponseDTO();
            userDeletionResponseDTO.setUser(convertToUserDTO(user));
            userDeletionResponseDTO.setStatus(deletionStatus);

            switch (deletionStatus) {
                case SUCCESS:
                    return new ResponseEntity<>(userDeletionResponseDTO, HttpStatus.OK);
                case EXTERNAL_VALID:
                default:
                    throw new MethodNotAllowedException("User with login " + user.getScreenName() + " cannot be deleted because is from external source and is valid user.");
            }
        } catch (IdentityManagementException e) {
            throw new ServiceUnavailableException("Some system error occurred while deleting user with login " + user.getScreenName() + ". Please, try it later.");
        }
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(httpMethod = "DELETE", value = "Tries to delete users with given ids and returns users and statuses of their deletion. \n" +
            "Statuses: 1) SUCCESS - user was deleted\n 2) EXTERNAL_VALID - user is from external source and was not marked as deleted\n" +
            "3) ERROR - user could not be deleted, try it later\n 4) NOT_FOUND - user could not be found",
            consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<UserDeletionResponseDTO>> deleteUsers(@ApiParam(value = "Ids of users to be deleted.", required = true)
                                                                     @RequestBody List<Long> ids) {
        Preconditions.checkNotNull(ids);

        ids.forEach(id -> LOGGER.info(String.valueOf(id)));

        Map<User, UserDeletionStatus> mapOfResults = userService.deleteUsers(ids);
        List<UserDeletionResponseDTO> response = new ArrayList<>();

        mapOfResults.forEach((user, status) -> {
            UserDeletionResponseDTO r = new UserDeletionResponseDTO();
            if (status.equals(UserDeletionStatus.NOT_FOUND)) {
                UserDTO u = new UserDTO();
                u.setId(user.getId());
                r.setUser(u);
            } else {
                r.setUser(convertToUserDTO(user));
            }
            r.setStatus(status);

            response.add(r);
        });

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(path = "/{id}/change-admin-role")
    @ApiOperation(httpMethod = "PUT", value = "Changes admin role to user with given id.")
    public ResponseEntity<Void> changeAdminRole(@ApiParam(value = "Id of user to be changed their admin role.",
            required = true) @PathVariable("id") final Long id) {
        try {
            userService.changeAdminRole(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IdentityManagementException e) {
            throw new ResourceNotFoundException("User or role could not be found.");
        }
    }

    @GetMapping(path = "/{id}/roles")
    @ApiOperation(httpMethod = "GET", value = "Returns all roles of user with given id.")
    public ResponseEntity<Set<RoleDTO>> getRolesOfUser(
            @ApiParam(value = "id", required = true) @PathVariable("id") final Long id) {

        Set<Role> roles = userService.getRolesOfUser(id);
        Set<RoleDTO> roleDTOS = roles.stream().map(this::convertToRoleDTO)
                .collect(Collectors.toSet());
        return new ResponseEntity<>(roleDTOS, HttpStatus.OK);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO u = new UserDTO();
        u.setId(user.getId());
        u.setLogin(user.getScreenName());
        u.setFullName(user.getFullName());
        u.setMail(user.getMail());
        return u;
    }

    private User convertToUser(NewUserDTO newUserDTO) {
        User user = new User(newUserDTO.getLogin());
        user.setFullName(newUserDTO.getFullName());
        user.setMail(newUserDTO.getMail());
        user.setStatus(UserAndGroupStatus.VALID);
        return user;
    }

    private User convertToUser(UpdateUserDTO updateUserDTO) {
        User user = new User(updateUserDTO.getLogin());
        user.setId(updateUserDTO.getId());
        user.setFullName(updateUserDTO.getFullName());
        user.setMail(updateUserDTO.getMail());
        user.setStatus(userService.get(updateUserDTO.getId()).getStatus());
        return user;
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setRoleType(role.getRoleType());
        return roleDTO;
    }
}
