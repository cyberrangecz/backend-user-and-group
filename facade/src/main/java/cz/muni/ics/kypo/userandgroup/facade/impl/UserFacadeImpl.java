package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.*;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static cz.muni.ics.kypo.userandgroup.util.UserAndGroupConstants.NAME_OF_USER_AND_GROUP_SERVICE;

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
    public PageResultResource<UserDTO> getUsers(Predicate predicate, Pageable pageable) throws MicroserviceException {
        PageResultResource<UserDTO> users = beanMapping.mapToPageResultDTO(userService.getAllUsers(predicate, pageable), UserDTO.class);
        List<UserDTO> usersWithRoles = users.getContent().stream()
                .peek(userDTO -> userDTO.setRoles(this.getRolesOfUser(userDTO.getId())))
                .collect(Collectors.toList());
        users.setContent(usersWithRoles);
        return users;
    }

    @Override
    public UserDTO getUser(Long id) {
        try {
            UserDTO userDTO = beanMapping.mapTo(userService.get(id), UserDTO.class);
            LOG.info("User with id: " + id + " has been loaded.");
            userDTO.setRoles(getRolesOfUser(id));
            return userDTO;
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while loading user with id: " + id + "." );
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    public PageResultResource<UserDTO> getAllUsersNotInGivenGroup(Long groupId, Pageable pageable) throws UserAndGroupFacadeException, MicroserviceException {
        PageResultResource<UserDTO> users = beanMapping.mapToPageResultDTO(userService.getAllUsersNotInGivenGroup(groupId, pageable), UserDTO.class);
        List<UserDTO> usersWithRoles = users.getContent().stream()
                .peek(userDTO -> userDTO.setRoles(this.getRolesOfUser(userDTO.getId())))
                .collect(Collectors.toList());
        users.setContent(usersWithRoles);
        return users;
    }

    @Override
    public UserDeletionResponseDTO deleteUser(Long id) throws UserAndGroupFacadeException {
        User user = null;
        try {
            user = userService.get(id);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while deleting user with id: " + id + ".");
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
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
    public Set<RoleDTO> getRolesOfUser(Long id) throws UserAndGroupFacadeException, MicroserviceException {
        try {
            Set<RoleDTO> roles = new HashSet<>();
            OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();

            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                if (microservice.getName().equals(NAME_OF_USER_AND_GROUP_SERVICE)) {
                    Set<RoleDTO> r = beanMapping.mapToSet(userService.getRolesOfUser(id), RoleDTO.class);
                    roles.addAll(r.stream()
                            .peek(roleDTO -> roleDTO.setNameOfMicroservice(NAME_OF_USER_AND_GROUP_SERVICE))
                            .collect(Collectors.toSet()));
                } else {
                    User u = userService.get(id);
                    StringBuilder groupsIds = new StringBuilder();
                    if (u.getGroups().size() > 0) {
                        groupsIds.append(String.valueOf(u.getGroups().get(0).getId()));
                    }
                    for (int i = 1; i < u.getGroups().size(); i++) {
                        groupsIds.append(",").append(u.getGroups().get(i).getId());
                    }
                    String url = microservice.getEndpoint() + "/roles/of/groups?ids=" + groupsIds.toString();
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                    HttpEntity<List<Long>> entity = new HttpEntity<>(null, headers);
                    try {
                        ResponseEntity<RoleDTO[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, RoleDTO[].class, id);
                        if (responseEntity.getStatusCode().is2xxSuccessful()) {
                            Set<RoleDTO> rolesOfMicroservice = Arrays.stream(responseEntity.getBody())
                                    .peek(roleDTO -> roleDTO.setNameOfMicroservice(microservice.getName()))
                                    .collect(Collectors.toSet());

                            roles.addAll(rolesOfMicroservice);
                        } else {
                            LOG.error("Some error occured during getting roles of user with id {} from microservice {}. Status code: {}. Response {}",
                                    microservice.getName(), responseEntity.getStatusCode().toString(), responseEntity.toString());
                            throw new UserAndGroupFacadeException("Some error occured during getting roles of user with id " + id + " from microservice " + microservice.getName());
                        }
                    } catch (HttpClientErrorException e) {
                        LOG.error("Client side error when calling microservice {}. Status code: {}. Response Body {}",
                                microservice.getName(), e.getStatusCode().toString(), e.getResponseBodyAsString());
                        throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                    } catch (RestClientException e) {
                        LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                        throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                    }
                }
            }

            return roles;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public UserInfoDTO getUserInfo(OAuth2Authentication authentication) throws UserAndGroupFacadeException, MicroserviceException {
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        String sub = credentials.get("sub").getAsString();
        User loggedInUser = null;
        try {
            loggedInUser = userService.getUserByLogin(sub);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while getting info about user with sub: " + sub + ".");
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
        Set<RoleDTO> rolesOfUser = getRolesOfUser(loggedInUser.getId());

        return convertToUserInfoDTO(loggedInUser, rolesOfUser);
    }

    @Override
    public boolean isUserInternal(Long id) throws UserAndGroupFacadeException {
        try {
            return userService.isUserInternal(id);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    private UserInfoDTO convertToUserInfoDTO(User user, Set<RoleDTO> roles) {
        UserInfoDTO u = beanMapping.mapTo(user, UserInfoDTO.class);

        Set<RoleDTO> rolesDTOs = roles.stream().map(role -> beanMapping.mapTo(role, RoleDTO.class)).collect(Collectors.toSet());
        u.setRoles(roles);

        return u;
    }
}
