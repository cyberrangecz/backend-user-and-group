package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.RoleFacade;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.RoleMapper;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
        LOG.debug("getById({})", id);
        try {
            Role role = roleService.getById(id);
            return convertToRoleDTO(role);
        } catch (UserAndGroupServiceException ex) {
            LOG.error("Role with id: {} could not be found.", id);
            throw new UserAndGroupFacadeException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public RoleDTO getByRoleType(String roleType) {
        LOG.debug("getByRoleType({})", roleType);
        try {
            Role role = roleService.getByRoleType(roleType.toUpperCase());
            LOG.info("Role with role type: {} has been loaded.", roleType);
            return convertToRoleDTO(role);
        } catch (UserAndGroupServiceException ex) {
            throw new UserAndGroupFacadeException("Role with role type: " + roleType + " could not be found.", ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable) {
        LOG.debug("getAllRoles()");
        List<Role> roles = roleService.getAllRoles(predicate, pageable).getContent();
        return  new PageResultResource<>(roles.stream().map(this::convertToRoleDTO).collect(Collectors.toList()));
    }

    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = roleMapper.mapToDTO(role);
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        return roleDTO;
    }
}
