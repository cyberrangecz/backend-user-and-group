package cz.muni.ics.kypo.userandgroup.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.NewRoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
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

    Logger LOG = LoggerFactory.getLogger(RoleFacadeImpl.class);

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
        LOG.info("Role with id: " + role.getId() + " and role type: " + role.getRoleType() +" has been created." );
        return beanMapping.mapTo(roleService.create(role), RoleDTO.class);
    }

    @Override
    public void deleteRole(Long id) {
        Role roleToDelete = roleService.getById(id);
        roleService.delete(roleToDelete);
        LOG.info("Role with id: " + id + "has been deleted.");

    }

    @Override
    public RoleDTO getById(Long id) {
        try {
            Role role = roleService.getById(id);
            LOG.info("Role with id: " + id + "has been loaded.");
            return beanMapping.mapTo(role, RoleDTO.class);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while loading role with id: " + id + ".");
            throw new UserAndGroupFacadeException(ex.getMessage());
        }

    }

    @Override
    public RoleDTO getByRoleType(RoleType roleType) {
        try {
            Role role = roleService.getByRoleType(roleType.toString());
            LOG.info("Role with role type: " + roleType + "has been loaded.");
            return beanMapping.mapTo(role, RoleDTO.class);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Error while loading role with role type: " + roleType.toString() + ".");
            throw new UserAndGroupFacadeException(ex.getMessage());
        }
    }

    @Override
    public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
        Page<Role> roles = roleService.getAllRoles(predicate, pageable);
        LOG.info("All roles have been loaded");
        return beanMapping.mapToPageResultDTO(roles, RoleDTO.class);
    }
}
