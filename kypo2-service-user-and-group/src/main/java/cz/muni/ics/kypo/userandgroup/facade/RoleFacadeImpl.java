package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
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
        Assert.notNull(id, "In method getRoleById(id) the input id must not be null.");
        try {
            Role role = roleService.getRoleById(id);
            return convertToRoleDTO(role);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex);
        }
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public RoleDTO getByRoleType(String roleType) {
        Assert.hasLength(roleType, "In method getByRoleType(roleType) the input roleType must not be empty.");
        try {
            Role role = roleService.getByRoleType(roleType.toUpperCase());
            return convertToRoleDTO(role);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException("Role with role type: " + roleType + " could not be found.", ex);
        }
    }

    @Override
    @IsAdmin
    @TransactionalRO
    public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
        Page<Role> roles = roleService.getAllRoles(predicate, pageable);
        List<RoleDTO> roleDtos = roles.getContent().stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toCollection(ArrayList::new));
        PageResultResource.Pagination pagination = roleMapper.createPagination(roles);
        return new PageResultResource<>(roleDtos, pagination);
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = roleMapper.mapToDTO(role);
        Microservice microservice = role.getMicroservice();
        if (microservice != null) {
            roleDTO.setIdOfMicroservice(microservice.getId());
            roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        }
        return roleDTO;
    }
}
