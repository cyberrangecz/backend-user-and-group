package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.exception.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IDMGroupFacadeImpl implements IDMGroupFacade {

    private static Logger LOG = LoggerFactory.getLogger(IDMGroupFacadeImpl.class.getName());

    private IDMGroupService groupService;
    private MicroserviceService microserviceService;
    private RestTemplate restTemplate;
    private BeanMapping beanMapping;

    @Autowired
    public IDMGroupFacadeImpl(IDMGroupService groupService, MicroserviceService microserviceService,
                              BeanMapping beanMapping, RestTemplate restTemplate) {
        this.groupService = groupService;
        this.microserviceService = microserviceService;
        this.beanMapping = beanMapping;
        this.restTemplate = restTemplate;
    }

    @Override
    public GroupDTO createGroup(NewGroupDTO newGroupDTO) throws UserAndGroupFacadeException {
        IDMGroup group = beanMapping.mapTo(newGroupDTO, IDMGroup.class);
        try {
            IDMGroup createdGroup = groupService.create(group, newGroupDTO.getGroupIdsOfImportedUsers());
            return beanMapping.mapTo(createdGroup, GroupDTO.class);
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void updateGroup(UpdateGroupDTO updateGroupDTO) throws ExternalSourceException {
        groupService.update(beanMapping.mapTo(updateGroupDTO, IDMGroup.class));
    }

    @Override
    public void removeUsers(Long groupId, List<Long> userIds) throws UserAndGroupFacadeException, ExternalSourceException {
        try {
            beanMapping.mapTo(groupService.removeUsers(groupId, userIds), GroupDTO.class);
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void addUsers(AddUsersToGroupDTO addUsers) throws UserAndGroupFacadeException, ExternalSourceException {
        try {
            groupService.addUsers(addUsers.getGroupId(),
                    addUsers.getIdsOfGroupsOfImportedUsers(), addUsers.getIdsOfUsersToBeAdd());
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public GroupDeletionResponseDTO deleteGroup(Long id) {
        IDMGroup group = groupService.get(id);
        GroupDeletionStatus deletionStatus = groupService.delete(group);
        GroupDeletionResponseDTO groupDeletionResponseDTO = beanMapping.mapTo(group, GroupDeletionResponseDTO.class);
        groupDeletionResponseDTO.setStatus(deletionStatus);
        return groupDeletionResponseDTO;
    }

    @Override
    public List<GroupDeletionResponseDTO> deleteGroups(List<Long> ids) {
        Map<IDMGroup, GroupDeletionStatus> mapOfResults = groupService.deleteGroups(ids);
        List<GroupDeletionResponseDTO> response = new ArrayList<>();

        mapOfResults.forEach((group, status) -> {
            GroupDeletionResponseDTO r = new GroupDeletionResponseDTO();
            r.setId(group.getId());
            r.setName(group.getName());
            r.setStatus(status);
            response.add(r);
        });
        return response;
    }

    @Override
    public PageResultResource<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable) {
        PageResultResource<GroupDTO> groups = beanMapping.mapToPageResultDTO(groupService.getAllIDMGroups(predicate, pageable), GroupDTO.class);
        List<GroupDTO> groupsWithRoles = groups.getContent().stream()
                .peek(groupDTO -> groupDTO.setRoles(this.getRolesOfGroup(groupDTO.getId())))
                .collect(Collectors.toList());
        groups.setContent(groupsWithRoles);
        return groups;
    }

    @Override
    public GroupDTO getGroup(Long id) throws UserAndGroupFacadeException {
        try {
            GroupDTO groupDTO = beanMapping.mapTo(groupService.get(id), GroupDTO.class);
            groupDTO.setRoles(this.getRolesOfGroup(id));
            return groupDTO;
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public Set<RoleDTO> getRolesOfGroup(Long id) throws UserAndGroupFacadeException, RestClientException {
        try {
            GroupDTO group = beanMapping.mapTo(groupService.get(id), GroupDTO.class);
            Set<RoleDTO> roles = group.getRoles().stream()
                    .peek(roleDTO -> roleDTO.setNameOfMicroservice("User and Group"))
                    .collect(Collectors.toSet());
            OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                String uri = microservice.getEndpoint() + "/of/group/{groupId}";

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                HttpEntity<String> entity = new HttpEntity<>(null, headers);
                try {
                    ResponseEntity<Role[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Role[].class, id);
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
                        LOG.error("Some error occured during getting roles of group with id {} from microservice {}", id, microservice.getName());
                        throw new UserAndGroupFacadeException("Some error occured during getting roles of group with id " + id + " from microservice " + microservice.getName());
                    }
                } catch (RestClientException e) {
                    LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                }
            }

            return roles;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void assignRole(Long groupId, RoleType roleType) throws UserAndGroupFacadeException {
        try {
            groupService.assignRole(groupId, roleType);
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        Assert.notNull(microserviceId, "Input microserviceId must not be null");

        try {
            Microservice microservice = microserviceService.get(microserviceId);
            final String uri = microservice.getEndpoint() + "/{roleId}/assign/to/{groupId}";
            OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            try {
                ResponseEntity<Void> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, Void.class, roleId, groupId);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new UserAndGroupFacadeException("Some error occured during assigning role with " + roleId + " to group with id " + groupId + " in microservice " +
                            "with name " + microservice.getName());
                }
            } catch (RestClientException e) {
                LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
            }
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean isGroupInternal(Long id) throws UserAndGroupFacadeException {
        try {
            return groupService.isGroupInternal(id);
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }
}
