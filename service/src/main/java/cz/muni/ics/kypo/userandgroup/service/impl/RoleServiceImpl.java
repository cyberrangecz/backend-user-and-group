package cz.muni.ics.kypo.userandgroup.service.impl;

import cz.muni.ics.kypo.userandgroup.dbmodel.Role;
import cz.muni.ics.kypo.userandgroup.dbmodel.RoleType;
import cz.muni.ics.kypo.userandgroup.exception.IdentityManagementException;
import cz.muni.ics.kypo.userandgroup.persistence.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private static Logger log = LoggerFactory.getLogger(RoleServiceImpl.class.getName());

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public Role create(Role role) {
        Assert.notNull(role, "Input role must not be null");
        Assert.notNull(role.getRoleType(), "Role type of input role must not be null");

        Role r = roleRepository.save(role);
        log.info(r + " was created.");
        return r;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public void delete(Role role) {
        Assert.notNull(role, "Input role must not be null");
        roleRepository.delete(role);
        log.info("Role with id: " + role.getId() + " was successfully deleted.");
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public Role getById(Long id) throws IdentityManagementException {
        Assert.notNull(id, "Input id must not be null");
        Optional<Role> optionalRole = roleRepository.findById(id);
        Role r = optionalRole.orElseThrow(() -> new IdentityManagementException("Role with id " + id + " could not be found"));
        log.info(r + " loaded");
        return r;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public Role getByRoleType(String roleType) throws IdentityManagementException {
        Assert.notNull(roleType, "Input role type must not be null");
        Optional<Role> optionalRole = roleRepository.findByRoleType(roleType);
        Role r = optionalRole.orElseThrow(() -> new IdentityManagementException("Role with roleType " + roleType + " could not be found"));
        log.info(r + " loaded");
        return r;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.dbmodel.RoleType).ADMINISTRATOR)")
    public List<Role> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        log.info("All Roles loaded");
        return roles;
    }
}
