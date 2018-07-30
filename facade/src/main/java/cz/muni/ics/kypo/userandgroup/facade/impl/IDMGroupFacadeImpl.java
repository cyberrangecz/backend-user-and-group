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
import org.springframework.http.ResponseEntity;
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
    public GroupDTO updateGroup(UpdateGroupDTO updateGroupDTO) {
        IDMGroup group = beanMapping.mapTo(updateGroupDTO, IDMGroup.class);
        return beanMapping.mapTo(groupService.update(group), GroupDTO.class);
    }

    @Override
    public GroupDTO removeUsers(Long groupId, List<Long> userIds) {
        try {
            return beanMapping.mapTo(groupService.removeUsers(groupId, userIds), GroupDTO.class);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public GroupDTO addUsers(AddUsersToGroupDTO addUsers) {
        try {
            IDMGroup group = groupService.addUsers(addUsers.getGroupId(),
                    addUsers.getIdsOfGroupsOfImportedUsers(), addUsers.getIdsOfUsersToBeAdd());
            return beanMapping.mapTo(group, GroupDTO.class);
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
            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                String uri = microservice.getEndpoint() + "/of/group/{groupId}";

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
                    throw new UserAndGroupFacadeException("Some error occured during getting roles of group with id " + id + " from microservice " + microservice.getName());
                }
            }

            return roles;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public GroupDTO assignRole(Long groupId, RoleType roleType) {
        try {
            return beanMapping.mapTo(groupService.assignRole(groupId, roleType), GroupDTO.class);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public GroupDTO assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) {
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        Assert.notNull(microserviceId, "Input microserviceId must not be null");

        try {
            Microservice microservice = microserviceService.get(microserviceId);
            IDMGroup group = groupService.get(groupId);
            final String uri = microservice.getEndpoint() + "/{roleId}/assign/to/{groupId}";
            restTemplate.put(uri, null, roleId, groupId);

            GroupDTO groupDTO = beanMapping.mapTo(group, GroupDTO.class);
            groupDTO.setRoles(getRolesOfGroup(groupId));
            return groupDTO;
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
