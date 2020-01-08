package cz.muni.ics.kypo.userandgroup.facade;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.UserMapper;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserFacadeImpl implements UserFacade {

    @Value("${service.name}")
    private String nameOfUserAndGroupService;
    Logger LOG = LoggerFactory.getLogger(UserFacadeImpl.class);

    private UserService userService;
    private UserMapper userMapper;
    private RoleMapper roleMapper;

    @Autowired
    public UserFacadeImpl(UserService userService, UserMapper userMapper, RoleMapper roleMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable) {
        return  userMapper.mapToPageResultResource(userService.getAllUsers(predicate, pageable));
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds) {
        return  userMapper.mapToPageResultResource(userService.getUsersWithGivenRoleAndNotWithGivenIds(roleType, userIds, predicate, pageable));
    }

    @Override
    @TransactionalRO
    public UserDTO getUser(Long id) {
        try {
            User u = userService.get(id);
            return userMapper.mapToUserDTOWithRoles(u);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        PageResultResource<UserDTO> users = userMapper.mapToPageResultResource(userService.getAllUsersNotInGivenGroup(groupId, predicate, pageable));
        List<UserDTO> usersWithRoles = users.getContent().stream()
                .map(userDTO -> {
                    userDTO.setRoles(this.getRolesOfUser(userDTO.getId()));
                    return userDTO;
                })
                .collect(Collectors.toList());
        users.setContent(usersWithRoles);
        return users;
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserForGroupsDTO> getUsersInGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
        return userMapper.mapToPageResultResourceForGroups(userService.getUsersInGroups(groupsIds, predicate, pageable));
    }

    @Override
    @TransactionalWO
    public List<UserDeletionResponseDTO> deleteUsers(List<Long> ids) {
        Map<User, UserDeletionStatusDTO> mapOfResults = userService.deleteUsers(ids);
        List<UserDeletionResponseDTO> response = new ArrayList<>();

        mapOfResults.forEach((user, status) -> {
            UserDeletionResponseDTO deletionResponseDTO = new UserDeletionResponseDTO();
            if (status.equals(UserDeletionStatusDTO.NOT_FOUND)) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                deletionResponseDTO.setUser(userDTO);
            } else {
                deletionResponseDTO.setUser(userMapper.mapToDTO(user));
            }
            deletionResponseDTO.setStatus(status);

            response.add(deletionResponseDTO);
        });
        return response;
    }

    @Override
    @TransactionalWO
    public UserDeletionResponseDTO deleteUser(Long id) {
        User user = null;
        try {
            user = userService.get(id);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
        UserDeletionStatusDTO deletionStatus = userService.delete(user);
        UserDeletionResponseDTO userDeletionResponseDTO = new UserDeletionResponseDTO();
        userDeletionResponseDTO.setUser(userMapper.mapToDTO(user));
        userDeletionResponseDTO.setStatus(deletionStatus);
        return userDeletionResponseDTO;
    }

    @Override
    @TransactionalRO
    public Set<RoleDTO> getRolesOfUser(Long id) {
        try {
            Set<Role> roles = userService.getRolesOfUser(id);
            return roles.stream()
                    .map(role -> roleMapper.mapToRoleDTOWithMicroservice(role))
                    .collect(Collectors.toSet());
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public UserDTO getUserInfo(OAuth2Authentication authentication) {
        return userMapper.mapToUserDTOWithRoles(getLoggedInUser(authentication));
    }


    @Override
    @TransactionalRO
    public boolean isUserInternal(Long id) {
        try {
            return userService.isUserInternal(id);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Predicate predicate, Pageable pageable) {
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenRole(roleId, predicate, pageable));

        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRoleType(String roleType, Predicate predicate, Pageable pageable) {
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenRoleType(roleType, predicate, pageable));

        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenIds(Set<Long> ids, Pageable pageable) {
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenIds(ids, pageable));

        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    public PageResultResource<UserDTO> getUsersWithGivenIds(Set<Long> ids, Pageable pageable, Predicate predicate) {
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenIds(ids, pageable, predicate));

        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }


    private User getLoggedInUser(OAuth2Authentication authentication) {
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        String sub = credentials.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
        String iss = credentials.get(AuthenticatedUserOIDCItems.ISS.getName()).getAsString();
        User loggedInUser = null;
        try {
            loggedInUser = userService.getUserByLoginAndIss(sub, iss);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
        return loggedInUser;
    }


}
