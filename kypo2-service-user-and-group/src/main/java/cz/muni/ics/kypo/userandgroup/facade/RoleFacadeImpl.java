package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleFacadeImpl implements RoleFacade {

    @Value("${service.name}")
    private String nameOfUserAndGroupService;
    Logger LOG = LoggerFactory.getLogger(RoleFacadeImpl.class);

    private RoleService roleService;
    private RoleMapper roleMapper;

    @Autowired
    public RoleFacadeImpl(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    @Override
    @TransactionalRO
    public RoleDTO getById(Long id) {
        try {
            Role role = roleService.getById(id);
            return convertToRoleDTO(role);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public RoleDTO getByRoleType(String roleType) {
        try {
            Role role = roleService.getByRoleType(roleType.toUpperCase());
            return convertToRoleDTO(role);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException("Role with role type: " + roleType + " could not be found.", ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
        Page<Role> roles = roleService.getAllRoles(predicate, pageable);
        List<RoleDTO> roleDtos = roles.getContent().stream().map(this::convertToRoleDTO).collect(Collectors.toList());
        PageResultResource.Pagination pagination = roleMapper.createPagination(roles);
        return new PageResultResource<>(roleDtos, pagination);
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = roleMapper.mapToDTO(role);
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        return roleDTO;
    }
}
