package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            throw new UserAndGroupFacadeException(ex.getMessage());
        }

    }

    @Override
    public RoleDTO getByRoleType(RoleType roleType) throws UserAndGroupFacadeException {
        try {
            Role role = roleService.getByRoleType(roleType.toString());
            LOG.info("Role with role type: " + roleType + "has been loaded.");
            RoleDTO roleDTO = beanMapping.mapTo(role, RoleDTO.class);
            roleDTO.setNameOfMicroservice("User and Group");
            return roleDTO;
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Role with role type: " + roleType + " could not be found.");
            throw new UserAndGroupFacadeException(ex.getMessage());
        }
    }

    @Override
    public PageResultResource<RoleDTO> getAllRoles(Pageable pageable) throws UserAndGroupFacadeException, MicroserviceException {
        List<RoleDTO> roles = beanMapping.mapTo(roleService.getAllRoles(pageable).getContent(), RoleDTO.class);
        roles = roles.stream()
                .peek(roleDTO -> roleDTO.setNameOfMicroservice("User and Group"))
                .collect(Collectors.toList());
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();

        List<Microservice> microservices = microserviceService.getMicroservices();
        for (Microservice microservice : microservices) {
            String uri = microservice.getEndpoint();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            try {
                ResponseEntity<Role[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Role[].class);
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
                    LOG.error("Some error occured during getting all roles from microservice " + microservice.getName());
                    throw new UserAndGroupFacadeException("Some error occured during getting all roles from microservice " + microservice.getName());
                }
            } catch (RestClientException e) {
                LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
            }
        }
        LOG.info("All roles have been loaded");
        return beanMapping.mapToPageResultDTO(new PageImpl<>(roles, pageable, roles.size()), RoleDTO.class);
    }
}
