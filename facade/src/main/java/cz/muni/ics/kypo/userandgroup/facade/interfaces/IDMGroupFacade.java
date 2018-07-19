package cz.muni.ics.kypo.userandgroup.facade.interfaces;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.dto.group.*;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface IDMGroupFacade {

    GroupDTO createGroup(NewGroupDTO newGroupDTO);

    GroupDTO updateGroup(UpdateGroupDTO updateGroupDTO);

    GroupDTO removeMembers(Long groupId, List<Long> userIds);

    GroupDTO addMembers(AddMembersToGroupDTO addMembers);

    GroupDeletionResponseDTO deleteGroup(Long id);

    List<GroupDeletionResponseDTO> deleteGroups(List<Long> ids);

    List<GroupDTO> getAllGroups(Predicate predicate, Pageable pageable);

    GroupDTO getGroup(Long id);

    Set<RoleDTO> getRolesOfGroup(Long id);

    GroupDTO assignRole(Long groupId, RoleType roleType);

    GroupDTO assignRoleInMicroservice(Long groupId, Long roleId, Long microserviceId);
}
