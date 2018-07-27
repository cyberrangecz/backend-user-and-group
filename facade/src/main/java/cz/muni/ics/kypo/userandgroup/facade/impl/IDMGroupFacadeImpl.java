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
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class IDMGroupFacadeImpl implements IDMGroupFacade {

    private IDMGroupService groupService;
    private BeanMapping beanMapping;

    @Autowired
    public IDMGroupFacadeImpl(IDMGroupService groupService, BeanMapping beanMapping) {
        this.groupService = groupService;
        this.beanMapping = beanMapping;
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
        Page<IDMGroup> groups = groupService.getAllIDMGroups(predicate, pageable);
        return beanMapping.mapToPageResultDTO(groups, GroupDTO.class);
    }

    @Override
    public GroupDTO getGroup(Long id) {
        try {
            return beanMapping.mapTo(groupService.get(id), GroupDTO.class);
        } catch (UserAndGroupServiceException e) {
            throw new UserAndGroupFacadeException(e.getMessage());
        }
    }

    @Override
    public Set<RoleDTO> getRolesOfGroup(Long id) {
        try {
            return beanMapping.mapToSet(groupService.getRolesOfGroup(id), RoleDTO.class);
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
        try {
            return beanMapping.mapTo(groupService.assignRoleInMicroservice(groupId, roleId, microserviceId), GroupDTO.class);
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
