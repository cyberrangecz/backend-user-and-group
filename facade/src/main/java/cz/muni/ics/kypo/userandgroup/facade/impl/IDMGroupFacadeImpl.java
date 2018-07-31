package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IDMGroupFacadeImpl implements IDMGroupFacade {

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
    public GroupDTO createGroup(NewGroupDTO newGroupDTO) {
        IDMGroup group = beanMapping.mapTo(newGroupDTO, IDMGroup.class);
        IDMGroup createdGroup = groupService.create(group, newGroupDTO.getGroupIdsOfImportedUsers());
        return beanMapping.mapTo(createdGroup, GroupDTO.class);
    }

    @Override
    public void updateGroup(UpdateGroupDTO updateGroupDTO) {
        groupService.update(beanMapping.mapTo(updateGroupDTO, IDMGroup.class));
    }

    @Override
    public void removeUsers(Long groupId, List<Long> userIds) {
        try {
            beanMapping.mapTo(groupService.removeUsers(groupId, userIds), GroupDTO.class);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public void addUsers(AddUsersToGroupDTO addUsers) {
        try {
            groupService.addUsers(addUsers.getGroupId(),
                    addUsers.getIdsOfGroupsOfImportedUsers(), addUsers.getIdsOfUsersToBeAdd());
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public GroupDeletionResponseDTO deleteGroup(Long id) {
        try {
            IDMGroup group = groupService.get(id);
            GroupDeletionStatus deletionStatus = groupService.delete(group);
            GroupDeletionResponseDTO groupDeletionResponseDTO = beanMapping.mapTo(group, GroupDeletionResponseDTO.class);
            groupDeletionResponseDTO.setStatus(deletionStatus);
            return groupDeletionResponseDTO;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
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
    public GroupDTO getGroup(Long id) {
        try {
            GroupDTO groupDTO = beanMapping.mapTo(groupService.get(id), GroupDTO.class);
            groupDTO.setRoles(this.getRolesOfGroup(id));
            return groupDTO;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public Set<RoleDTO> getRolesOfGroup(Long id) {
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
                HttpEntity<String> entity = new HttpEntity<>( null, headers);
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
                    throw new UserAndGroupFacadeException("Some error occured during getting roles of group with id " + id + " from microservice " + microservice.getName());
                }
            }

            return roles;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public void assignRole(Long groupId, RoleType roleType) {
        try {
            groupService.assignRole(groupId, roleType);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
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
            HttpEntity<String> entity = new HttpEntity<>( null, headers);
            ResponseEntity<Void> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, Void.class, roleId, groupId);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new UserAndGroupFacadeException("Some error occured during assigning role with " + roleId + " to group with id " + groupId + " in microservice " +
                        "with name " + microservice.getName());
            }
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public boolean isGroupInternal(Long id) throws UserAndGroupFacadeException {
        try {
            return groupService.isGroupInternal(id);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }
}
