package cz.muni.ics.kypo.userandgroup.service.impl;

import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Role getById(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        Optional<Role> optionalRole = roleRepository.findById(id);
        Role r = optionalRole.orElseThrow(() -> new UserAndGroupServiceException("Role with id " + id + " could not be found"));
        log.info(r + " loaded");
        return r;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Role getByRoleType(RoleType roleType) throws UserAndGroupServiceException {
        Assert.notNull(roleType, "Input role type must not be null");
        Optional<Role> optionalRole = roleRepository.findByRoleType(roleType);
        Role r = optionalRole.orElseThrow(() -> new UserAndGroupServiceException("Role with roleType " + roleType + " could not be found"));
        log.info(r + " loaded");
        return r;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ADMINISTRATOR)")
    public Page<Role> getAllRoles(Pageable pageable) {
        Page<Role> roles = roleRepository.findAll(pageable);
        log.info("All Roles loaded");
        return roles;
    }
}
