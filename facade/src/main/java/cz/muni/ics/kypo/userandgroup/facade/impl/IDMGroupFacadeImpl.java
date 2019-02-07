package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.MicroserviceForGroupDeletionDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.*;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.*;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import cz.muni.ics.kypo.userandgroup.util.UserAndGroupConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import static cz.muni.ics.kypo.userandgroup.util.UserAndGroupConstants.NAME_OF_USER_AND_GROUP_SERVICE;

@Service
@Transactional
public class IDMGroupFacadeImpl implements IDMGroupFacade {

    private static Logger LOG = LoggerFactory.getLogger(IDMGroupFacadeImpl.class.getName());

    private IDMGroupService groupService;
    private MicroserviceService microserviceService;
    private RoleService roleService;
    private RestTemplate restTemplate;
    private BeanMapping beanMapping;

    @Autowired
    public IDMGroupFacadeImpl(IDMGroupService groupService, MicroserviceService microserviceService,
                              RoleService roleService, BeanMapping beanMapping, RestTemplate restTemplate) {
        this.groupService = groupService;
        this.microserviceService = microserviceService;
        this.roleService = roleService;
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
        GroupDeletionResponseDTO groupDeletionResponseDTO = new GroupDeletionResponseDTO();
        try {
            IDMGroup group = groupService.get(id);

            if (group.getStatus().equals(UserAndGroupStatus.VALID) && group.getExternalId() != null) {
                groupDeletionResponseDTO.setStatus(GroupDeletionStatus.EXTERNAL_VALID);
            } else {
                groupDeletionResponseDTO = deleteGroupInAllMicroservices(id);
                if (groupDeletionResponseDTO.getStatus() == null) {
                    groupDeletionResponseDTO.setStatus(groupService.delete(group));
                } else {
                    group.setStatus(UserAndGroupStatus.DIRTY);
                    groupService.update(group);
                }
            }
        } catch (UserAndGroupServiceException e) {
            groupDeletionResponseDTO.setStatus(GroupDeletionStatus.NOT_FOUND);
        }
        groupDeletionResponseDTO.setId(id);
        return groupDeletionResponseDTO;
    }

    private GroupDeletionResponseDTO deleteGroupInAllMicroservices(Long id) {
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        GroupDeletionResponseDTO responseDTO = new GroupDeletionResponseDTO();
        List<MicroserviceForGroupDeletionDTO> microserviceForGroupDeletionDTOs = new ArrayList<>();
        List<Microservice> microservices = microserviceService.getMicroservices();
        for (Microservice microservice : microservices) {
            if (!microservice.getName().equals(UserAndGroupConstants.NAME_OF_USER_AND_GROUP_SERVICE)) {
                final String url = microservice.getEndpoint() + "/groups/{groupId}";
                MicroserviceForGroupDeletionDTO microserviceForGroupDeletionDTO = new MicroserviceForGroupDeletionDTO();
                microserviceForGroupDeletionDTO.setName(microservice.getName());
                microserviceForGroupDeletionDTO.setId(microservice.getId());
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class, id);
                    microserviceForGroupDeletionDTO.setHttpStatus(responseEntity.getStatusCode());
                    microserviceForGroupDeletionDTO.setResponseMessage(responseEntity.getBody());
                    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                        responseDTO.setStatus(GroupDeletionStatus.MICROSERVICE_ERROR);
                        LOG.error("Some error occured during getting roles of group with id {} from microservice {}", id, microservice.getName());
                    }
                } catch (HttpClientErrorException e) {
                    responseDTO.setStatus(GroupDeletionStatus.MICROSERVICE_ERROR);
                    microserviceForGroupDeletionDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                    microserviceForGroupDeletionDTO.setResponseMessage("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                    LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                } catch (RestClientException e) {
                    responseDTO.setStatus(GroupDeletionStatus.MICROSERVICE_ERROR);
                    microserviceForGroupDeletionDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                    LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                }
                microserviceForGroupDeletionDTOs.add(microserviceForGroupDeletionDTO);
            }
        }
        responseDTO.setMicroserviceForGroupDeletionDTOs(microserviceForGroupDeletionDTOs);
        return responseDTO;
    }

    @Override
    public List<GroupDeletionResponseDTO> deleteGroups(List<Long> ids) {
        List<GroupDeletionResponseDTO> response = new ArrayList<>();
        for (Long id : ids) {
            response.add(this.deleteGroup(id));
        }
        return response;
    }

    @Override
    public PageResultResource<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable) throws UserAndGroupFacadeException, RestClientException {
        PageResultResource<GroupDTO> groups = beanMapping.mapToPageResultDTO(groupService.getAllIDMGroups(predicate, pageable), GroupDTO.class);
        List<GroupDTO> groupsWithRoles = groups.getContent().stream()
                .peek(groupDTO -> groupDTO.setRoles(this.getRolesOfGroup(groupDTO.getId())))
                .collect(Collectors.toList());
        groups.setContent(groupsWithRoles);
        return groups;
    }

    @Override
    public GroupDTO getGroup(Long id) throws UserAndGroupFacadeException, RestClientException {
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
            Set<RoleDTO> roles = new HashSet<>();
            OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                if (microservice.getName().equals(NAME_OF_USER_AND_GROUP_SERVICE)) {
                    roles.addAll(group.getRoles().stream()
                            .peek(roleDTO -> roleDTO.setNameOfMicroservice(microservice.getName()))
                            .peek(roleDTO -> roleDTO.setIdOfMicroservice(microservice.getId()))
                            .collect(Collectors.toSet()));
                } else {
                    String url = microservice.getEndpoint() + "/roles/roles-of-groups?ids=" + id;

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                    HttpEntity<String> entity = new HttpEntity<>(null, headers);
                    try {
                        ResponseEntity<RoleDTO[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, RoleDTO[].class, id);
                        if (responseEntity.getStatusCode().is2xxSuccessful()) {
                            Set<RoleDTO> rolesOfMicroservice = Arrays.stream(responseEntity.getBody())
                                    .peek(roleDTO -> roleDTO.setNameOfMicroservice(microservice.getName()))
                                    .peek(roleDTO -> roleDTO.setIdOfMicroservice(microservice.getId()))
                                    .collect(Collectors.toSet());

                            roles.addAll(rolesOfMicroservice);
                        } else {
                            LOG.error("Some error occured during getting roles of group with id {} from microservice {}. Status code: {}. Response {}",
                                    microservice.getName(), responseEntity.getStatusCode().toString(), responseEntity.toString());
                            throw new UserAndGroupFacadeException("Some error occured during getting roles of group with id " + id + " from microservice " + microservice.getName());
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
    public void assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) throws UserAndGroupFacadeException, MicroserviceException {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        Assert.notNull(microserviceId, "Input microserviceId must not be null");
        try {
            Microservice microservice = microserviceService.get(microserviceId);
            if (microservice.getName().equals(NAME_OF_USER_AND_GROUP_SERVICE)) {
                groupService.assignRole(groupId, roleService.getById(roleId).getRoleType());
            } else {
                final String url = microservice.getEndpoint() + "/groups/{groupId}/roles/{roleId}";
                OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                HttpEntity<String> entity = new HttpEntity<>(null, headers);
                try {
                    ResponseEntity<Void> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class, groupId, roleId);
                    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                        throw new UserAndGroupFacadeException("Some error occured during assigning role with " + roleId + " to group with id " + groupId + " in microservice " +
                                "with name " + microservice.getName());
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

    @Override
    public void removeRoleToGroupInMicroservice(Long groupId, Long roleId, Long microserviceId) throws UserAndGroupFacadeException,
            MicroserviceException, RoleCannotBeRemovedToGroupException {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        Assert.notNull(microserviceId, "Input microserviceId must not be null");

        try {
            Microservice microservice = microserviceService.get(microserviceId);
            if (microservice.getName().equals(NAME_OF_USER_AND_GROUP_SERVICE)) {
                groupService.removeRoleToGroup(groupId, roleService.getById(roleId).getRoleType());
            } else {
                final String url = microservice.getEndpoint() + "/groups/{groupId}/roles/{roleId}";
                OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                HttpEntity<String> entity = new HttpEntity<>(null, headers);
                try {
                    ResponseEntity<Void> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class, groupId, roleId);
                } catch (HttpClientErrorException e) {
                    JsonObject responseBody = new JsonParser().parse(e.getResponseBodyAsString()).getAsJsonObject();
                    if (e.getStatusCode().equals(HttpStatus.CONFLICT)) {
                        LOG.error(responseBody.get("message").toString());
                        throw new RoleCannotBeRemovedToGroupException(responseBody.get("message").toString());
                    }
                    LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                } catch (RestClientException e) {
                    LOG.error("Client side error when calling microservice {}. Probably wrong URL of service.", microservice.getName());
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                }
            }
        } catch (UserAndGroupServiceException e) {
            LOG.error(e.getLocalizedMessage());
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }
}
