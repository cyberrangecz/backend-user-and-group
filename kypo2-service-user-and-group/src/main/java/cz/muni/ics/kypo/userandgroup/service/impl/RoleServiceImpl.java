package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getRoleById(Long id) {
        Assert.notNull(id, "In method getRoleById(id) the input must not be null.");
        return roleRepository.findById(id)
                .orElseThrow(() -> new UserAndGroupServiceException("Role with id " + id + " could not be found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Role getByRoleType(String roleType) {
        Assert.notNull(roleType, "In method getByRoleType(roleType) the input must not be null.");
        return roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new UserAndGroupServiceException("Role with roleType " + roleType + " could not be found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Role getDefaultRoleOfMicroservice(String microserviceName) {
        Assert.notNull(microserviceName, "In method getDefaultRoleOfMicroservice(microserviceName) the input must not be null.");
        return roleRepository.findDefaultRoleOfMicroservice(microserviceName)
                .orElseThrow(() -> new UserAndGroupServiceException("Default role of microservice with name " + microserviceName + " could not be found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Page<Role> getAllRoles(Predicate predicate, Pageable pageable) {
        return roleRepository.findAll(predicate, pageable);
    }

    @Override
    public void createRole(Role role) {
        Assert.notNull(role, "In method createRole(roleType) the input must not be null.");
        if (roleRepository.existsByRoleType(role.getRoleType())) {
            throw new UserAndGroupServiceException("Role with given role type: " + role.getRoleType() + " already exist. " +
                    "Please name the role with different role type.", ErrorCode.RESOURCE_NOT_CREATED);
        }
        roleRepository.save(role);
    }

    @Override
    public Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice) {
        Assert.notNull(nameOfMicroservice, "In method getAllRolesOfMicroservice(nameOfMicroservice) the input must not be null.");
        return roleRepository.getAllRolesByMicroserviceName(nameOfMicroservice);
    }
}
