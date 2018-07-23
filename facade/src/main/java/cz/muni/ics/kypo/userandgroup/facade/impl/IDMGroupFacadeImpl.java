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
        return beanMapping.mapTo(groupService.create(group), GroupDTO.class);
    }

    @Override
    public GroupDTO updateGroup(UpdateGroupDTO updateGroupDTO) {
        IDMGroup group = beanMapping.mapTo(updateGroupDTO, IDMGroup.class);
        return beanMapping.mapTo(groupService.update(group), GroupDTO.class);
    }

    @Override
    public GroupDTO removeMembers(Long groupId, List<Long> userIds) {
        return beanMapping.mapTo(groupService.removeMembers(groupId, userIds), GroupDTO.class);
    }

    @Override
    public GroupDTO addMembers(AddMembersToGroupDTO addMembers) {
        try {
            IDMGroup group = groupService.addMembers(addMembers.getGroupId(),
                    addMembers.getIdsOfGroupsOfImportedUsers(), addMembers.getIdsOfUsersToBeAdd());
            return beanMapping.mapTo(group, GroupDTO.class);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getMessage());
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
        Page<IDMGroup> groups = groupService.getAllIDMGroups(predicate, pageable);
        return beanMapping.mapToPageResultDTO(groups, GroupDTO.class);
    }

    @Override
    public GroupDTO getGroup(Long id) {
        return beanMapping.mapTo(groupService.get(id), GroupDTO.class);
    }

    @Override
    public Set<RoleDTO> getRolesOfGroup(Long id) {
        return beanMapping.mapToSet(groupService.getRolesOfGroup(id), RoleDTO.class);
    }

    @Override
    public GroupDTO assignRole(Long groupId, RoleType roleType) {
        return beanMapping.mapTo(groupService.assignRole(groupId, roleType), GroupDTO.class);
    }

    @Override
    public GroupDTO assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) {
        return beanMapping.mapTo(groupService.assignRoleInMicroservice(groupId, roleId, microserviceId), GroupDTO.class);
    }
}
