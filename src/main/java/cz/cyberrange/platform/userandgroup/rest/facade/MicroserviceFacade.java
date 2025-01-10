package cz.cyberrange.platform.userandgroup.rest.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.security.IsAdmin;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalRO;
import cz.cyberrange.platform.userandgroup.rest.facade.annotations.transaction.TransactionalWO;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.userandgroup.api.mapping.MicroserviceMapper;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapper;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.MicroserviceService;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MicroserviceFacade {

    private final MicroserviceService microserviceService;
    private final RoleService roleService;
    private final IDMGroupService groupService;
    private final MicroserviceMapper microserviceMapper;
    private final RoleMapper roleMapper;

    @Autowired
    public MicroserviceFacade(MicroserviceService microserviceService, RoleService roleService,
                              IDMGroupService groupService, MicroserviceMapper microserviceMapper, RoleMapper roleMapper) {
        this.microserviceService = microserviceService;
        this.roleService = roleService;
        this.groupService = groupService;
        this.microserviceMapper = microserviceMapper;
        this.roleMapper = roleMapper;
    }

    @IsAdmin
    @TransactionalRO
    public PageResultResource<MicroserviceDTO> getAllMicroservices(Predicate predicate, Pageable pageable) {
        return microserviceMapper.mapToPageResultResource(microserviceService.getMicroservices(predicate, pageable));
    }

    @TransactionalWO
    public void registerMicroservice(NewMicroserviceDTO newMicroserviceDTO) {
        Microservice microservice = microserviceMapper.mapCreateToEntity(newMicroserviceDTO);
        if (microserviceService.existsByName(newMicroserviceDTO.getName())) {
            // microservice already exists, update roles of microservice
            microservice = microserviceService.getMicroserviceByName(newMicroserviceDTO.getName());
            microservice.setEndpoint(newMicroserviceDTO.getEndpoint());
            updateRolesOfMicroservice(newMicroserviceDTO.getRoles(), microservice);
        } else {
            // microservice does not exist
            microserviceService.createMicroservice(microservice);
            createNewRolesOfMicroservice(newMicroserviceDTO.getRoles(), microservice);
        }
    }

    private void createNewRolesOfMicroservice(Set<RoleForNewMicroserviceDTO> newRolesDTO, Microservice microservice) {
        checkDefaultRole(newRolesDTO);
        newRolesDTO.forEach(newRole -> {
            Role role = roleMapper.mapToEntity(newRole);
            role.setMicroservice(microservice);
            // maybe remove try-catch and let conflict exception bubble up
            roleService.createRole(role);
            if (newRole.isDefault()) {
                IDMGroup defaultGroup = groupService.getGroupForDefaultRoles();
                defaultGroup.addRole(role);
            }
        });
    }

    private void updateRolesOfMicroservice(Set<RoleForNewMicroserviceDTO> newRolesDTO, Microservice microservice) {
        checkDefaultRole(newRolesDTO);
        Set<Role> rolesInDB = roleService.getAllRolesOfMicroservice(microservice.getName());
        newRolesDTO.forEach(newRole -> {
            Role role = roleMapper.mapToEntity(newRole);
            role.setMicroservice(microservice);
            // if role is already in microservice it returns false, otherwise it returns true and the newly created role will be added
            if (rolesInDB.add(role)) {
                roleService.createRole(role);
                if (newRole.isDefault()) {
                    IDMGroup defaultGroup = groupService.getGroupForDefaultRoles();
                    // since it is not possible to have two default roles for particular microservice the old one is removed from the default group
                    // repair since microservice do not have to default role
                    defaultGroup.removeRole(roleService.getDefaultRoleOfMicroservice(microservice.getName()));
                    defaultGroup.addRole(role);
                }
            }
        });
    }

    private void checkDefaultRole(Set<RoleForNewMicroserviceDTO> rolesToCheck) {
        if (rolesToCheck.stream()
                .filter(RoleForNewMicroserviceDTO::isDefault)
                .count() > 1) {
            throw new UnprocessableEntityException(new EntityErrorDetail(Microservice.class, "Microservice which you are trying to register cannot have more than 1 default role."));
        }
    }
}
