package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleFacadeImpl implements RoleFacade {

    @Value("${service.name}")
    private String nameOfUserAndGroupService;

    private RoleService roleService;
    private RoleMapper roleMapper;
    
    @Autowired
    public RoleFacadeImpl(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public RoleDTO getRoleById(Long id) {
        return roleMapper.mapToRoleDTOWithMicroservice(roleService.getRoleById(id));
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public RoleDTO getByRoleType(String roleType) {
        return roleMapper.mapToRoleDTOWithMicroservice(roleService.getByRoleType(roleType.toUpperCase()));
    }

    @Override
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
}
