package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
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
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Role.class, "id", id.getClass(), id,
                        "Role not found.")));
    }

    @Override
    public Role getByRoleType(String roleType) {
        Assert.notNull(roleType, "In method getByRoleType(roleType) the input must not be null.");
        return roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Role.class, "roleType", roleType.getClass(), roleType,
                        "Role not found.")));
    }

    @Override
    public Role getDefaultRoleOfMicroservice(String microserviceName) {
        Assert.notNull(microserviceName, "In method getDefaultRoleOfMicroservice(microserviceName) the input must not be null.");
        return roleRepository.findDefaultRoleOfMicroservice(microserviceName)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Role.class, "microserviceName", microserviceName.getClass(),
                        microserviceName, "Default role of microservice could not be found")));
    }

    @Override
    public Page<Role> getAllRoles(Predicate predicate, Pageable pageable) {
        return roleRepository.findAll(predicate, pageable);
    }

    @Override
    public void createRole(Role role) {
        Assert.notNull(role, "In method createRole(roleType) the input must not be null.");
        if (roleRepository.existsByRoleType(role.getRoleType())) {
            throw new EntityConflictException(new EntityErrorDetail(Role.class, "roleType", role.getRoleType().getClass(), role.getRoleType(), "Role already exist. " +
                    "Please name the role with different role type."));
        }
        roleRepository.save(role);
    }

    @Override
    public Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice) {
        Assert.notNull(nameOfMicroservice, "In method getAllRolesOfMicroservice(nameOfMicroservice) the input must not be null.");
        return roleRepository.getAllRolesByMicroserviceName(nameOfMicroservice);
    }
}
