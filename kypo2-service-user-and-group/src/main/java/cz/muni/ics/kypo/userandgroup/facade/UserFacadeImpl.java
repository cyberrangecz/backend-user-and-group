package cz.muni.ics.kypo.userandgroup.facade;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.api.config.PageResultResource;
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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
    public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable) {
        PageResultResource<UserDTO> users = userMapper.mapToPageResultResource(userService.getAllUsersNotInGivenGroup(groupId, pageable));
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
    public PageResultResource<UserForGroupsDTO> getUsersInGroups(Set<Long> groupsIds, Pageable pageable) {
        return userMapper.mapToPageResultResourceForGroups(userService.getUsersInGroups(groupsIds, pageable));
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRole(Long roleId, Pageable pageable) {
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenRole(roleId, pageable));

        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }

    }

    @Override
    @TransactionalRO
    public PageResultResource<UserDTO> getUsersWithGivenRole(String roleType, Pageable pageable) {
        try {
            return userMapper.mapToPageResultResource(userService.getUsersWithGivenRole(roleType, pageable));

        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }

    }

    @Override
    @TransactionalRO
    public Set<UserDTO> getUsersWithGivenIds(Set<Long> ids) {
        try {
            return userMapper.mapToSetDTO(userService.getUsersWithGivenIds(ids));

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
