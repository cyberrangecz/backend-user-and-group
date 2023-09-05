package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Role.class, "id", id.getClass(), id)));
    }

    public Role getByRoleType(String roleType) {
        return roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Role.class, "roleType", roleType.getClass(), roleType)));
    }

    public Role getDefaultRoleOfMicroservice(String microserviceName) {
        return roleRepository.findDefaultRoleOfMicroservice(microserviceName)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Role.class, "microserviceName", microserviceName.getClass(),
                        microserviceName, "Default role of microservice could not be found")));
    }

    public Page<Role> getAllRoles(Predicate predicate, Pageable pageable) {
        return roleRepository.findAll(predicate, pageable);
    }

    public void createRole(Role role) {
        if (roleRepository.existsByRoleType(role.getRoleType())) {
            throw new EntityConflictException(new EntityErrorDetail(Role.class, "roleType", role.getRoleType().getClass(), role.getRoleType(), "Role already exist. " +
                    "Please name the role with different role type."));
        }
        roleRepository.save(role);
    }

    public Set<Role> getAllRolesOfMicroservice(String nameOfMicroservice) {
        return roleRepository.getAllRolesByMicroserviceName(nameOfMicroservice);
    }
}
