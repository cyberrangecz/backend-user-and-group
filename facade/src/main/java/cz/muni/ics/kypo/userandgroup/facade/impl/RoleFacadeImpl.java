package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.NewRoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleFacadeImpl implements RoleFacade {

    Logger logger = LoggerFactory.getLogger(RoleFacadeImpl.class);

    private RoleService roleService;
    private BeanMapping beanMapping;

    @Autowired
    public void RoleFacadeImpl(RoleService roleService, BeanMapping beanMapping) {
        this.roleService = roleService;
        this.beanMapping = beanMapping;
    }

    @Override
    public RoleDTO createRole(NewRoleDTO newRoleDTO) {
        Role role = beanMapping.mapTo(newRoleDTO, Role.class);
        logger.info("Role with id: " + role.getId() + " and role type: " + role.getRoleType() +" has been created." );
        return beanMapping.mapTo(roleService.create(role), RoleDTO.class);
    }

    @Override
    public void deleteRole(Long id) {
        Role roleToDelete = roleService.getById(id);
        roleService.delete(roleToDelete);
        logger.info("Role with id: " + id + "has been deleted.");

    }

    @Override
    public RoleDTO getById(Long id) {
        Role role = roleService.getById(id);
        logger.info("Role with id: " + id + "has been loaded.");
        return beanMapping.mapTo(role, RoleDTO.class);
    }

    @Override
    public RoleDTO getByRoleType(RoleType roleType) {
        Role role = roleService.getByRoleType(roleType.toString());
        logger.info("Role with role type: " + roleType + "has been loaded.");
        return beanMapping.mapTo(role, RoleDTO.class);
    }

    @Override
    public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
        Page<Role> roles = roleService.getAllRoles(predicate, pageable);
        logger.info("All roles have been loaded");
        return beanMapping.mapToPageResultDTO(roles, RoleDTO.class);
    }
}
