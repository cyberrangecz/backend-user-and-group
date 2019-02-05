package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.exception.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import cz.muni.ics.kypo.userandgroup.util.UserAndGroupConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static cz.muni.ics.kypo.userandgroup.util.UserAndGroupConstants.NAME_OF_USER_AND_GROUP_SERVICE;

@Service
@Transactional
public class RoleFacadeImpl implements RoleFacade {

    Logger LOG = LoggerFactory.getLogger(RoleFacadeImpl.class);

    private RoleService roleService;
    private MicroserviceService microserviceService;
    private RestTemplate restTemplate;
    private BeanMapping beanMapping;

    @Autowired
    public RoleFacadeImpl(RoleService roleService, MicroserviceService microserviceService,
                          RestTemplate restTemplate, BeanMapping beanMapping) {
        this.roleService = roleService;
        this.microserviceService = microserviceService;
        this.restTemplate = restTemplate;
        this.beanMapping = beanMapping;
    }

    @Override
    public RoleDTO getById(Long id) throws UserAndGroupFacadeException {
        try {
            Role role = roleService.getById(id);
            LOG.info("Role with id: " + id + "has been loaded.");
            RoleDTO roleDTO = beanMapping.mapTo(role, RoleDTO.class);
            roleDTO.setNameOfMicroservice("User and Group");
            return roleDTO;
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Role with id: " + id + " could not be found.");
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }

    }

    @Override
    public RoleDTO getByRoleType(RoleType roleType) throws UserAndGroupFacadeException {
        try {
            Role role = roleService.getByRoleType(roleType);
            LOG.info("Role with role type: " + roleType + "has been loaded.");
            RoleDTO roleDTO = beanMapping.mapTo(role, RoleDTO.class);
            roleDTO.setNameOfMicroservice("User and Group");
            return roleDTO;
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Role with role type: " + roleType + " could not be found.");
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    public PageResultResource<RoleDTO> getAllRoles(Pageable pageable) throws UserAndGroupFacadeException, MicroserviceException {
        List<RoleDTO> roles = new ArrayList<>();
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();

        List<Microservice> microservices = microserviceService.getMicroservices();
        for (Microservice microservice : microservices)
            if (microservice.getName().equals(UserAndGroupConstants.NAME_OF_USER_AND_GROUP_SERVICE)) {
                Set<RoleDTO> r = beanMapping.mapToSet(roleService.getAllRoles(pageable).getContent(), RoleDTO.class);
                roles.addAll(r.stream()
                        .peek(roleDTO -> roleDTO.setNameOfMicroservice(NAME_OF_USER_AND_GROUP_SERVICE))
                        .collect(Collectors.toList()));
            } else {
                String url = microservice.getEndpoint();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                HttpEntity<String> entity = new HttpEntity<>(null, headers);
                try {
                    ResponseEntity<PageResultResource<RoleDTO>> responseEntity = restTemplate.exchange(url + "/roles", HttpMethod.GET, entity, new ParameterizedTypeReference<PageResultResource<RoleDTO>>() {});
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        Set<RoleDTO> rolesOfMicroservice = responseEntity.getBody().getContent().stream().peek(
                                role -> role.setNameOfMicroservice(microservice.getName())).collect(Collectors.toSet());
                        roles.addAll(rolesOfMicroservice);
                    } else {
                        LOG.info(responseEntity.toString());
                        LOG.error("Some error occured during getting all roles from microservice {}. Status code: {}. Response {}",
                                microservice.getName(), responseEntity.getStatusCode().toString(), responseEntity.toString());
                        throw new UserAndGroupFacadeException("Some error occured during getting all roles from microservice " + microservice.getName());
                    }
                } catch (HttpClientErrorException e) {
                    LOG.error("Client side error when calling microservice {}. Status code: {}. Response Body {}",
                            microservice.getName(), e.getStatusCode().toString(), e.getResponseBodyAsString());
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                } catch (RestClientException e) {
                    LOG.error("Client side error when calling microservice {}. Status code: {}. Response Body {}",
                            microservice.getName(), e.getCause().toString(), e.getMessage());
                    LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                }
            }
        LOG.info("All roles have been loaded");
        return beanMapping.mapToPageResultDTO(new PageImpl<>(roles, pageable, roles.size()), RoleDTO.class);
    }
}
