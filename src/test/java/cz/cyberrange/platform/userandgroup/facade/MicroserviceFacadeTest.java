package cz.cyberrange.platform.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.rest.facade.MicroserviceFacade;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.api.dto.PageResultResource;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.MicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.microservice.NewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleForNewMicroserviceDTO;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityConflictException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.definition.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.userandgroup.api.mapping.MicroserviceMapperImpl;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapperImpl;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.MicroserviceService;
import cz.cyberrange.platform.userandgroup.service.RoleService;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@SpringBootTest(classes = { TestDataFactory.class, RoleMapperImpl.class, MicroserviceMapperImpl.class })
class MicroserviceFacadeTest {

    private MicroserviceFacade microserviceFacade;
    @Mock
    private MicroserviceService microserviceService;
    @Mock
    private RoleService roleService;
    @Mock
    private IDMGroupService groupService;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private RoleMapperImpl roleMapper;
    @Autowired
    private MicroserviceMapperImpl microserviceMapper;

    private AutoCloseable closeable;
    private Microservice userAndGroupMicroservice, trainingMicroservice;
    private MicroserviceDTO userAndGroupMicroserviceDTO, trainingMicroserviceDTO;
    private NewMicroserviceDTO newMicroserviceDTO;
    private RoleForNewMicroserviceDTO newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO;
    private Role adminRole, designerRole, organizerRole, traineeRole;
    private IDMGroup defaultGroup;
    private Predicate predicate;
    private Pageable pageable;

    @BeforeEach
    void init() {
        closeable = MockitoAnnotations.openMocks(this);
        microserviceFacade = new MicroserviceFacade(microserviceService, roleService, groupService, microserviceMapper, roleMapper);

        userAndGroupMicroservice = testDataFactory.getCrczpUaGMicroservice();
        userAndGroupMicroservice.setId(1L);
        trainingMicroservice = testDataFactory.getCrczpTrainingMicroservice();
        trainingMicroservice.setId(2L);

        userAndGroupMicroserviceDTO = testDataFactory.getMicroserviceUserAndGroupDTO();
        userAndGroupMicroserviceDTO.setId(userAndGroupMicroservice.getId());
        trainingMicroserviceDTO = testDataFactory.getMicroserviceTrainingDTO();
        trainingMicroserviceDTO.setId(trainingMicroservice.getId());

        newAdminRoleDTO = testDataFactory.getTrainingAdminRoleForNewMicroserviceDTO();
        newDesignerRoleDTO = testDataFactory.getTrainingDesignerRoleForNewMicroserviceDTO();
        newTraineeRoleDTO = testDataFactory.getTrainingTraineeRoleForNewMicroserviceDTO();
        newOrganizerRoleDTO = testDataFactory.getTrainingOrganizerRoleForNewMicroserviceDTO();
        newTraineeRoleDTO.setDefault(true);

        adminRole = testDataFactory.getTrainingAdminRole();
        adminRole.setId(1L);
        designerRole = testDataFactory.getTrainingDesignerRole();
        designerRole.setId(2L);
        organizerRole = testDataFactory.getTrainingOrganizerRole();
        organizerRole.setId(3L);
        traineeRole = testDataFactory.getTrainingTraineeRole();
        traineeRole.setId(4L);

        newMicroserviceDTO = testDataFactory.getNewMicroserviceDTO();
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newTraineeRoleDTO));

        defaultGroup = testDataFactory.getUAGDefaultGroup();
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    void testGetAllMicroservices() {
        Page<Microservice> microservicePage = new PageImpl<>(List.of(userAndGroupMicroservice, trainingMicroservice));
        given(microserviceService.getMicroservices(predicate, pageable)).willReturn(microservicePage);

        PageResultResource<MicroserviceDTO> microservicePageResultResource = microserviceFacade.getAllMicroservices(predicate, pageable);
        assertEquals(2, microservicePageResultResource.getContent().size());
        assertTrue(microservicePageResultResource.getContent().containsAll(Set.of(trainingMicroserviceDTO, userAndGroupMicroserviceDTO)));
    }

    @Test
    void registerMicroserviceCreate() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);

        microserviceFacade.registerMicroservice(newMicroserviceDTO);
        then(roleService).should().createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should().createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should().createRole(mapToRole(newTraineeRoleDTO));
        then(groupService).should(times(1)).getGroupForDefaultRoles();
    }

    @Test
    void registerMicroserviceCreateRoleAlreadyInDB() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        willThrow(EntityConflictException.class).given(roleService).createRole(any());
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);

        assertThrows(EntityConflictException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    void registerMicroserviceCreateNoDefaultGroup() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        willThrow(EntityNotFoundException.class).given(groupService).getGroupForDefaultRoles();

        assertThrows(EntityNotFoundException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    void registerMicroserviceCreateWithMultipleDefaultRoles() {
        newDesignerRoleDTO.setDefault(true);
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);

        assertThrows(UnprocessableEntityException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
        then(roleService).should(never()).createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newTraineeRoleDTO));
    }

    @Test
    void registerMicroserviceUpdateWithSameRoles() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));

        microserviceFacade.registerMicroservice(newMicroserviceDTO);
        then(roleService).should(never()).createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newTraineeRoleDTO));
        then(groupService).should(never()).getGroupForDefaultRoles();
        then(roleService).should(never()).getDefaultRoleOfMicroservice(newMicroserviceDTO.getName());
    }

    @Test
    void registerMicroserviceUpdateWithNewRole() {
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));

        microserviceFacade.registerMicroservice(newMicroserviceDTO);
        then(roleService).should().createRole(mapToRole(newOrganizerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newTraineeRoleDTO));
        then(groupService).should(never()).getGroupForDefaultRoles();
        then(roleService).should(never()).getDefaultRoleOfMicroservice(newMicroserviceDTO.getName());
    }

    @Test
    void registerMicroserviceUpdateRoleAlreadyInDB() {
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));
        willThrow(EntityConflictException.class).given(roleService).createRole(any());

        assertThrows(EntityConflictException.class, () ->microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    void registerMicroserviceUpdateNoDefaultGroup() {
        newTraineeRoleDTO.setDefault(false);
        newOrganizerRoleDTO.setDefault(true);
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));
        willThrow(EntityNotFoundException.class).given(groupService).getGroupForDefaultRoles();

        assertThrows(EntityNotFoundException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    void registerMicroserviceUpdateNoDefaultRoleOfMicroservice() {
        newTraineeRoleDTO.setDefault(false);
        newOrganizerRoleDTO.setDefault(true);
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);
        willThrow(EntityNotFoundException.class).given(roleService).getDefaultRoleOfMicroservice(trainingMicroservice.getName());

        assertThrows(EntityNotFoundException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    void registerMicroserviceUpdateWithMultipleDefaultRoles() {
        newOrganizerRoleDTO.setDefault(true);
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);

        assertThrows(UnprocessableEntityException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
        then(roleService).should(never()).createRole(mapToRole(newOrganizerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newTraineeRoleDTO));
    }

    @Test
    void registerMicroserviceUpdateWithNewDefaultRole() {
        newTraineeRoleDTO.setDefault(false);
        newOrganizerRoleDTO.setDefault(true);
        defaultGroup.addRole(traineeRole);
        newMicroserviceDTO.setRoles(new HashSet<>(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO)));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);
        given(roleService.getDefaultRoleOfMicroservice(trainingMicroservice.getName())).willReturn(traineeRole);

        microserviceFacade.registerMicroservice(newMicroserviceDTO);
        then(roleService).should().createRole(mapToRole(newOrganizerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newTraineeRoleDTO));
        then(groupService).should().getGroupForDefaultRoles();
        then(roleService).should().getDefaultRoleOfMicroservice(newMicroserviceDTO.getName());
        assertTrue(defaultGroup.getRoles().contains(organizerRole));
        assertFalse(defaultGroup.getRoles().contains(traineeRole));
    }

    private Role mapToRole(RoleForNewMicroserviceDTO roleForNewMicroserviceDTO) {
        Role role = new Role();
        role.setMicroservice(trainingMicroservice);
        role.setRoleType(roleForNewMicroserviceDTO.getRoleType());
        role.setDescription(roleForNewMicroserviceDTO.getDescription());
        return role;
    }
}
