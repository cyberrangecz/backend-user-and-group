package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.annotations.security.IsAdmin;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static Logger LOG = LoggerFactory.getLogger(RoleServiceImpl.class.getName());

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @IsAdmin
    public Role getById(Long id) {
        Assert.notNull(id, "Input id must not be null");
        return roleRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("Role with id " + id + " could not be found"));
    }

    @Override
    @IsAdmin
    public Role getByRoleType(String roleType) {
        Assert.notNull(roleType, "Input role type must not be null");
        return roleRepository.findByRoleType(roleType).orElseThrow(() -> new UserAndGroupServiceException("Role with roleType " + roleType + " could not be found"));
    }

    @Override
    @IsAdmin
    public Page<Role> getAllRoles(Predicate predicate, Pageable pageable) {
        return roleRepository.findAll(predicate, pageable);
    }

    @Override
    public void create(Role role) {
        Assert.notNull(role, "Input role must not be null");
        if (roleRepository.existsByRoleType(role.getRoleType())) {
            throw new UserAndGroupServiceException("Role with given role type: " + role.getRoleType() + " already exist. " +
                    "Please name the role with different role type.");
        }
        roleRepository.save(role);
    }

    @Override
    public Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice) {
        Assert.notNull(nameOfMicroservice, "Input name of microservice must not be null");
        return roleRepository.getAllRolesByMicroserviceName(nameOfMicroservice);
    }
}
