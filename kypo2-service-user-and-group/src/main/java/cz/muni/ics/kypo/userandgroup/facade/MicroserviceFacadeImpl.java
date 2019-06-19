package cz.muni.ics.kypo.userandgroup.facade;

import cz.muni.ics.kypo.userandgroup.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.api.exceptions.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.api.facade.MicroserviceFacade;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.mapping.mapstruct.MicroserviceMapper;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Service
@Transactional
public class MicroserviceFacadeImpl implements MicroserviceFacade {

    private static Logger LOG = LoggerFactory.getLogger(MicroserviceFacadeImpl.class.getName());

    private MicroserviceService microserviceService;
    private RoleService roleService;
    private IDMGroupService groupService;
    private MicroserviceMapper microserviceMapper;

    @Autowired
    public MicroserviceFacadeImpl(MicroserviceService microserviceService, RoleService roleService,
                                  IDMGroupService groupService, MicroserviceMapper microserviceMapper) {
        this.microserviceService = microserviceService;
        this.roleService = roleService;
        this.groupService = groupService;
        this.microserviceMapper = microserviceMapper;
    }

    @Override
    @TransactionalWO
    public void registerNewMicroservice(NewMicroserviceDTO newMicroserviceDTO) {
        LOG.debug("registerNewMicroservice({})", newMicroserviceDTO);
        Microservice microservice = microserviceMapper.mapCreateToEntity(newMicroserviceDTO);
        microserviceService.create(microservice);
        Set<Role> rolesOfMicroservice = roleService.getAllRolesOfMicroservice(newMicroserviceDTO.getName());
        if (!rolesOfMicroservice.isEmpty()) {
            Set<String> rolesInDB = rolesOfMicroservice.stream()
                    .map(Role::getRoleType)
                    .collect(Collectors.toSet());
            Set<String> rolesInDTO = newMicroserviceDTO.getRoles().stream()
                    .map(RoleForNewMicroserviceDTO::getRoleType)
                    .collect(Collectors.toSet());
            if (!rolesInDTO.containsAll(rolesInDB) || rolesInDTO.size() != rolesInDB.size()) {
                throw new UserAndGroupFacadeException("Microservice which you are trying register is not same as " +
                        "microservice in DB. Change name or roles or contact administrator.");
            }
        } else {
            for (RoleForNewMicroserviceDTO newRole : newMicroserviceDTO.getRoles()) {
                Role role = new Role();
                role.setRoleType(newRole.getRoleType());
                role.setDescription(newRole.getDescription());
                role.setMicroservice(microservice);
                try {
                    roleService.create(role);
                    if (newRole.isDefault()) {
                        IDMGroup defaultGroup = groupService.getGroupForDefaultRoles();
                        defaultGroup.addRole(role);
                    }

                } catch (UserAndGroupServiceException e) {
                    throw new UserAndGroupFacadeException(e);
                }
            }
        }


    }
}
