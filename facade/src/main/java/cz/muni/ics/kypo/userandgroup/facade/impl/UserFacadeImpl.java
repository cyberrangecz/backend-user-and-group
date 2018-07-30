package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.UserFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import cz.muni.ics.kypo.userandgroup.util.UserDeletionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserFacadeImpl implements UserFacade {

    Logger LOG = LoggerFactory.getLogger(UserFacadeImpl.class);

    private UserService userService;
    private MicroserviceService microserviceService;
    private RestTemplate restTemplate;
    private BeanMapping beanMapping;

    @Autowired
    public UserFacadeImpl(UserService userService, MicroserviceService microserviceService,
                          RestTemplate restTemplate, BeanMapping beanMapping) {
        this.userService = userService;
        this.microserviceService = microserviceService;
        this.restTemplate = restTemplate;
        this.beanMapping = beanMapping;
    }

    @Override
    public PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable) {
        Page<User> users = userService.getAllUsers(predicate, pageable);
        return beanMapping.mapToPageResultDTO(users, UserDTO.class);
    }

    @Override
    public UserDTO getUser(Long id) {
        try {
            User user = userService.get(id);
            LOG.info("User with id: " + id + " has been loaded.");
            return beanMapping.mapTo(user, UserDTO.class);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while loading user with id: " + id + "." );
            throw new UserAndGroupFacadeException(ex.getMessage());
        }
    }

    @Override
    public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable) {
        Page<User> users = userService.getAllUsersNotInGivenGroup(groupId, pageable);
        return beanMapping.mapToPageResultDTO(users, UserDTO.class);
    }

    @Override
    public UserDeletionResponseDTO deleteUser(Long id) {
        User user = null;
        try {
            user = userService.get(id);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while deleting user with id: " + id + ".");
            throw new UserAndGroupFacadeException(ex.getMessage());
        }
        UserDeletionStatus deletionStatus = userService.delete(user);
        UserDeletionResponseDTO userDeletionResponseDTO = beanMapping.mapTo(user, UserDeletionResponseDTO.class);
        userDeletionResponseDTO.setStatus(deletionStatus);
        return userDeletionResponseDTO;
    }

    @Override
    public List<UserDeletionResponseDTO> deleteUsers(List<Long> ids) {
        Map<User, UserDeletionStatus> mapOfResults = userService.deleteUsers(ids);
        List<UserDeletionResponseDTO> response = new ArrayList<>();

        mapOfResults.forEach((user, status) -> {
            UserDeletionResponseDTO r = new UserDeletionResponseDTO();
            if (status.equals(UserDeletionStatus.NOT_FOUND)) {
                UserDTO u = new UserDTO();
                u.setId(user.getId());
                r.setUser(u);
            } else {
                r.setUser(beanMapping.mapTo(user, UserDTO.class));
            }
            r.setStatus(status);

            response.add(r);
        });
        return response;
    }

    @Override
    public Set<RoleDTO> getRolesOfUser(Long id) throws UserAndGroupFacadeException {
        try {
            Set<RoleDTO> roles = beanMapping.mapToSet(userService.getRolesOfUser(id), RoleDTO.class);
            roles = roles.stream()
                    .peek(roleDTO -> roleDTO.setNameOfMicroservice("User and Group"))
                    .collect(Collectors.toSet());

            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                String uri = microservice.getEndpoint() + "/of/user/{userId}";

                ResponseEntity<Role[]> responseEntity = restTemplate.getForEntity(uri, Role[].class, id);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    Set<RoleDTO> rolesOfMicroservice = Arrays.stream(responseEntity.getBody())
                            .map(role -> {
                                RoleDTO roleDTO = beanMapping.mapTo(role, RoleDTO.class);
                                roleDTO.setNameOfMicroservice(microservice.getName());
                                return roleDTO;
                            })
                            .collect(Collectors.toSet());

                    roles.addAll(rolesOfMicroservice);
                } else {
                    throw new UserAndGroupFacadeException("Some error occured during getting roles of user with id " + id + " from microservice " + microservice.getName());
                }
            }

            return roles;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public UserInfoDTO getUserInfo(OAuth2Authentication authentication) throws UserAndGroupFacadeException {
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        String sub = credentials.get("sub").getAsString();
        User loggedInUser = null;
        try {
            loggedInUser = userService.getUserByLogin(sub);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while getting info about user with sub: " + sub + ".");
            throw new UserAndGroupFacadeException(ex.getMessage());
        }
        Set<Role> rolesOfUser = userService.getRolesOfUser(loggedInUser.getId());

        return convertToUserInfoDTO(loggedInUser, rolesOfUser);
    }

    @Override
    public boolean isUserInternal(Long id) throws UserAndGroupFacadeException {
        try {
            return userService.isUserInternal(id);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    private UserInfoDTO convertToUserInfoDTO(User user, Set<Role> roles) {
        UserInfoDTO u = beanMapping.mapTo(user, UserInfoDTO.class);

        Set<RoleDTO> rolesDTOs = roles.stream().map(role -> beanMapping.mapTo(role, RoleDTO.class)).collect(Collectors.toSet());
        u.setRoles(rolesDTOs);

        return u;
    }
}
