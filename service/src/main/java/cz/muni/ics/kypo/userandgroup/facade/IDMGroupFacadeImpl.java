package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.ResponseRoleToGroupInMicroservicesDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.RoleAndMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AssignRoleToGroupStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.IDMGroupMapper;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional
public class IDMGroupFacadeImpl implements IDMGroupFacade {

    @Value("${service.name}")
    private String nameOfUserAndGroupService;
    private static Logger LOG = LoggerFactory.getLogger(IDMGroupFacadeImpl.class.getName());

    private IDMGroupService groupService;
    private MicroserviceService microserviceService;
    private IDMGroupMapper groupMapper;
    private RoleMapper roleMapper;

    @Autowired
    public IDMGroupFacadeImpl(IDMGroupService groupService, MicroserviceService microserviceService,
                              RoleMapper roleMapper, IDMGroupMapper groupMapper) {
        this.groupService = groupService;
        this.microserviceService = microserviceService;
        this.groupMapper = groupMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    @TransactionalWO
    public GroupDTO createGroup(NewGroupDTO newGroupDTO) {
        LOG.debug("createGroup({})", newGroupDTO);
        IDMGroup group = groupMapper.mapCreateToEntity(newGroupDTO);
        try {
            IDMGroup createdGroup = groupService.create(group, newGroupDTO.getGroupIdsOfImportedUsers());
            GroupDTO createdGroupDTO = groupMapper.mapToDTO(createdGroup);
            List<Microservice> microservices = microserviceService.getMicroservices();
            for (Microservice microservice : microservices) {
                if (microservice.getName().equals(nameOfUserAndGroupService)) {
                    createdGroupDTO.getRoles()
                            .forEach(roleDTO -> {
                                roleDTO.setNameOfMicroservice(microservice.getName());
                                roleDTO.setIdOfMicroservice(microservice.getId());
                            });
                }
            }
            return createdGroupDTO;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalWO
    public void updateGroup(UpdateGroupDTO updateGroupDTO) {
        LOG.debug("updateGroup({})", updateGroupDTO);
        groupService.update(groupMapper.mapUpdateToEntity(updateGroupDTO));
    }

    @Override
    @TransactionalWO
    public void removeUsers(Long groupId, List<Long> userIds) {
        LOG.debug("removeUsers({}, {})", groupId, userIds);
        try {
            groupService.removeUsers(groupId, userIds);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalWO
    public void addUsers(Long groupId, AddUsersToGroupDTO addUsers) {
        LOG.debug("addUsers({})", addUsers);
        try {
            groupService.addUsers(groupId,
                    addUsers.getIdsOfGroupsOfImportedUsers(), addUsers.getIdsOfUsersToBeAdd());
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalWO
    public GroupDeletionResponseDTO deleteGroup(Long id) {
        LOG.debug("deleteGroup({})", id);
        GroupDeletionResponseDTO groupDeletionResponseDTO = new GroupDeletionResponseDTO();
        try {
            IDMGroup group = groupService.get(id);
            List<User> users = group.getUsers();
            if (users == null || users.isEmpty()) {
                groupDeletionResponseDTO.setStatus(groupService.delete(group));
            } else {
                throw new UserAndGroupFacadeException("It is not possible to delete this group. This group must be empty (without persons)");
            }
        } catch (UserAndGroupServiceException e) {
            groupDeletionResponseDTO.setStatus(GroupDeletionStatusDTO.NOT_FOUND);
        }
        groupDeletionResponseDTO.setId(id);
        return groupDeletionResponseDTO;
    }

    @Override
    @TransactionalWO
    public List<GroupDeletionResponseDTO> deleteGroups(List<Long> ids) {
        LOG.debug("deleteGroups({})", ids);
        List<GroupDeletionResponseDTO> response = new ArrayList<>();
        for (Long id : ids) {
            response.add(this.deleteGroup(id));
        }
        return response;
    }

    @Override
    @TransactionalRO
    public PageResultResource<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable) {
        LOG.debug("getAllGroups()");
        PageResultResource<GroupDTO> groups = groupMapper.mapToPageResultResource(groupService.getAllIDMGroups(predicate, pageable));
        List<GroupDTO> groupsWithRoles = groups.getContent().stream()
                .map(groupDTO -> {
                    groupDTO.setRoles(this.getRolesOfGroup(groupDTO.getId()));
                    return groupDTO;
                })
                .collect(Collectors.toList());
        groups.setContent(groupsWithRoles);
        return groups;
    }

    @Override
    @TransactionalRO
    public GroupDTO getGroup(Long id) {
        LOG.debug("getGroup({})", id);
        try {
            GroupDTO groupDTO = groupMapper.mapToDTO(groupService.get(id));
            groupDTO.setRoles(this.getRolesOfGroup(id));
            return groupDTO;
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
        }
    }

    @Override
    @TransactionalRO
    public Set<RoleDTO> getRolesOfGroup(Long id) {
        LOG.debug("getRolesOfGroup({})", id);
        try {
            return groupService.getRolesOfGroup(id).stream()
                    .map(this::convertToRoleDTO)
                    .collect(Collectors.toSet());
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalWO
    public void assignRole(Long groupId, Long roleId) {
        LOG.debug("assignRole({}, {})", groupId, roleId);
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        try {
            groupService.assignRole(groupId, roleId);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
        }
    }

    @Override
    @TransactionalRO
    public boolean isGroupInternal(Long id) {
        LOG.debug("isGroupInternal({})", id);
        try {
            return groupService.isGroupInternal(id);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
        }
    }

    @Override
    @TransactionalWO
    public void removeRoleFromGroup(Long groupId, Long roleId) {
        LOG.debug("removeRoleFromGroup({}, {})", groupId, roleId);
        Assert.notNull(groupId, "Input groupId must not be null");
        Assert.notNull(roleId, "Input roleId must not be null");
        try {
            groupService.removeRoleFromGroup(groupId, roleId);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e);
        }
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = roleMapper.mapToDTO(role);
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        return roleDTO;
    }
}
