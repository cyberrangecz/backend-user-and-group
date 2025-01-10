package cz.cyberrange.platform.userandgroup.rest.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdmin;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapper;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleFacade {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @Autowired
    public RoleFacade(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    @IsAdmin
    @TransactionalRO
    public RoleDTO getRoleById(Long id) {
        return roleMapper.mapToRoleDTOWithMicroservice(roleService.getRoleById(id));
    }

    @IsAdmin
    @TransactionalRO
    public RoleDTO getByRoleType(String roleType) {
        return roleMapper.mapToRoleDTOWithMicroservice(roleService.getByRoleType(roleType.toUpperCase()));
    }
    
    @IsAdmin
    @TransactionalRO
    public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
        Page<Role> roles = roleService.getAllRoles(predicate, pageable);
        List<RoleDTO> roleDTOs = roles.getContent().stream()
                .map(role -> roleMapper.mapToRoleDTOWithMicroservice(role))
                .collect(Collectors.toCollection(ArrayList::new));
        PageResultResource.Pagination pagination = roleMapper.createPagination(roles);
        return new PageResultResource<>(roleDTOs, pagination);
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<RoleDTO> getAllRolesNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        Page<Role> roles = roleService.getAllRolesNotInGivenGroup(groupId, predicate, pageable);
        List<RoleDTO> roleDTOs = roles.getContent().stream()
                .map(roleMapper::mapToRoleDTOWithMicroservice)
                .toList();
        PageResultResource.Pagination pagination = roleMapper.createPagination(roles);
        return new PageResultResource<>(roleDTOs, pagination);
    }


}
