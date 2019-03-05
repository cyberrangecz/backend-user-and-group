package cz.muni.ics.kypo.userandgroup.facade;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.MicroserviceForGroupDeletionDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.ResponseRoleToGroupInMicroservicesDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.RoleAndMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AssignRoleToGroupStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.ExternalSourceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.MicroserviceException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.RoleCannotBeRemovedToGroupException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
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
            GroupDTO createdGroupDTO = beanMapping.mapTo(createdGroup, GroupDTO.class);
            Set<RoleDTO> roles = new HashSet<>();
            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                if (microservice.getName().equals(NAME_OF_USER_AND_GROUP_SERVICE)) {
                    roles.addAll(createdGroupDTO.getRoles().stream()
                            .peek(roleDTO -> roleDTO.setNameOfMicroservice(microservice.getName()))
                            .peek(roleDTO -> roleDTO.setIdOfMicroservice(microservice.getId()))
                            .collect(Collectors.toSet()));
                }
            }
            createdGroupDTO.setRoles(roles);
            return createdGroupDTO;
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
            List<User> users = group.getUsers();
            if (users == null || users.isEmpty()) {
                try {
                    groupDeletionResponseDTO = deleteGroupInAllMicroservices(id);
                } catch (MicroserviceException e) {
                    group.setStatus(UserAndGroupStatus.DIRTY);
                    groupService.update(group);
                    groupDeletionResponseDTO.setStatus(GroupDeletionStatusDTO.MICROSERVICE_ERROR);
                }
                groupDeletionResponseDTO.setStatus(groupService.delete(group));
            } else {
                throw new MicroserviceException("It is not possible to delete this group. This group must be empty (without persons)");
            }
            //What does this VALID status means? All groups are by default valid, thus it is not possible to delete any....

//            if (group.getStatus().equals(UserAndGroupStatus.VALID) && group.getExternalId() != null) {
//                groupDeletionResponseDTO.setStatus(GroupDeletionStatusDTO.EXTERNAL_VALID);
//            } else {
//                groupDeletionResponseDTO = deleteGroupInAllMicroservices(id);
//                if (groupDeletionResponseDTO.getStatus() == null) {
//                    groupDeletionResponseDTO.setStatus(groupService.delete(group));
//                } else {
//                    group.setStatus(UserAndGroupStatus.DIRTY);
//                    groupService.update(group);
//                }
//            }
        } catch (UserAndGroupServiceException e) {
            groupDeletionResponseDTO.setStatus(GroupDeletionStatusDTO.NOT_FOUND);
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
                        responseDTO.setStatus(GroupDeletionStatusDTO.MICROSERVICE_ERROR);
                        throw new MicroserviceException("Some error occured during getting roles of group with id " + id + "from microservice: " + microservice.getName());
                    }

                } catch (HttpClientErrorException.NotFound ex) {
                    microserviceForGroupDeletionDTO.setHttpStatus(HttpStatus.NOT_FOUND);
                    microserviceForGroupDeletionDTO.setResponseMessage(ex.getMessage());
                }catch (HttpClientErrorException e) {
                    responseDTO.setStatus(GroupDeletionStatusDTO.MICROSERVICE_ERROR);
                    microserviceForGroupDeletionDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                    microserviceForGroupDeletionDTO.setResponseMessage("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.");
                } catch (RestClientException e) {
                    responseDTO.setStatus(GroupDeletionStatusDTO.MICROSERVICE_ERROR);
                    microserviceForGroupDeletionDTO.setHttpStatus(HttpStatus.BAD_REQUEST);
                    microserviceForGroupDeletionDTO.setResponseMessage(e.getCause().getMessage());
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
            throw new UserAndGroupFacadeException(e);
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
                            throw new UserAndGroupFacadeException("Some error occured during getting roles of group with id " + id + " from microservice " + microservice.getName());
                        }
                    } catch (HttpClientErrorException e) {
                        throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.", e);
                    } catch (RestClientException e) {
                        throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.", e);
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
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.", e);
                } catch (RestClientException e) {
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.", e);
                }
            }
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
        }
    }
    @Override
    public List<ResponseRoleToGroupInMicroservicesDTO> assignRolesInMicroservices(Long groupId, List<RoleAndMicroserviceDTO> rolesAndMicroservices) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(rolesAndMicroservices, "Input roles and microservices must not be null");
        List<ResponseRoleToGroupInMicroservicesDTO> responses = new ArrayList<>();
        for (RoleAndMicroserviceDTO ram : rolesAndMicroservices) {
            ResponseRoleToGroupInMicroservicesDTO resp = new ResponseRoleToGroupInMicroservicesDTO();
            resp.setRoleId(ram.getRoleId());
            resp.setMicroserviceId(ram.getMicroserviceId());
            try {
                Microservice microservice = microserviceService.get(ram.getMicroserviceId());
                groupService.get(groupId);
                if (microservice.getName().equals(NAME_OF_USER_AND_GROUP_SERVICE)) {
                    groupService.assignRole(groupId, roleService.getById(ram.getRoleId()).getRoleType());
                    resp.setStatus(AssignRoleToGroupStatusDTO.SUCCESS);
                } else {
                    final String url = microservice.getEndpoint() + "/groups/{groupId}/roles/{roleId}";
                    OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", auth.getTokenType() + " " + auth.getTokenValue());
                    HttpEntity<String> entity = new HttpEntity<>(null, headers);
                    try {
                        ResponseEntity<Void> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class, groupId, ram.getRoleId());
                        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                            resp.setStatus(AssignRoleToGroupStatusDTO.MICROSERVICE_ERROR);
                            resp.setResponseMessage("Status code: " + responseEntity.getStatusCode().toString() + " Body: " + responseEntity.getBody());
                        }
                        resp.setStatus(AssignRoleToGroupStatusDTO.SUCCESS);
                    } catch (HttpClientErrorException e) {
                        resp.setStatus(AssignRoleToGroupStatusDTO.MICROSERVICE_ERROR);
                        resp.setResponseMessage("Cannot assign role to group in given microservice. It is return status code: " + e.getStatusCode() + ".");
                    } catch (RestClientException e) {
                        resp.setStatus(AssignRoleToGroupStatusDTO.REST_CLIENT_ERROR);
                        resp.setResponseMessage(e.getCause().getMessage());
                    }
                }
            } catch (UserAndGroupServiceException e) {
                resp.setStatus(AssignRoleToGroupStatusDTO.NOT_FOUND);
                resp.setResponseMessage(e.getMessage());
            }
            responses.add(resp);

        }
        return responses;
    }


    @Override
    public boolean isGroupInternal(Long id) throws UserAndGroupFacadeException {
        try {
            return groupService.isGroupInternal(id);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
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
                        throw new RoleCannotBeRemovedToGroupException(responseBody.get("message").toString(), e);
                    }
                } catch (RestClientException e) {
                    throw new MicroserviceException("Client side error when calling microservice " + microservice.getName() + ". Probably wrong URL of service.", e);
                }
            }
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
        }
    }
}
