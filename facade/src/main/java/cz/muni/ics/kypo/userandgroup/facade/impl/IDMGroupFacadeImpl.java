package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.IDMGroupFacade;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public class IDMGroupFacadeImpl implements IDMGroupFacade {

    @Override
    public GroupDTO createGroup(NewGroupDTO newGroupDTO) {
        return null;
    }

    @Override
    public GroupDTO updateGroup(UpdateGroupDTO updateGroupDTO) {
        return null;
    }

    @Override
    public GroupDTO removeMembers(Long groupId, List<Long> userIds) {
        return null;
    }

    @Override
    public GroupDTO addMembers(AddMembersToGroupDTO addMembers) {
        return null;
    }

    @Override
    public GroupDeletionResponseDTO deleteGroup(Long id) {
        return null;
    }

    @Override
    public List<GroupDeletionResponseDTO> deleteGroups(List<Long> ids) {
        return null;
    }

    @Override
    public List<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable) {
        return null;
    }

    @Override
    public GroupDTO getGroup(Long id) {
        return null;
    }

    @Override
    public Set<RoleDTO> getRolesOfGroup(Long id) {
        return null;
    }

    @Override
    public GroupDTO assignRole(Long groupId, RoleType roleType) {
        return null;
    }

    @Override
    public GroupDTO assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId) {
        return null;
    }
}
