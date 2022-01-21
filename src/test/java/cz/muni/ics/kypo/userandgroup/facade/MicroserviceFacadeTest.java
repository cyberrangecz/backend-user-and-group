package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.microservice.MicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.dto.microservice.NewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleForNewMicroserviceDTO;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityConflictException;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.exceptions.UnprocessableEntityException;
import cz.muni.ics.kypo.userandgroup.mapping.IDMGroupMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.MicroserviceMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.service.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.RoleService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@SpringBootTest(classes = { TestDataFactory.class, RoleMapperImpl.class, MicroserviceMapperImpl.class })
public class MicroserviceFacadeTest {

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
    public void init() {
        closeable = MockitoAnnotations.openMocks(this);
        microserviceFacade = new MicroserviceFacade(microserviceService, roleService, groupService, microserviceMapper, roleMapper);

        userAndGroupMicroservice = testDataFactory.getKypoUaGMicroservice();
        userAndGroupMicroservice.setId(1L);
        trainingMicroservice = testDataFactory.getKypoTrainingMicroservice();
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
    public void testGetAllMicroservices() {
        Page<Microservice> microservicePage = new PageImpl<>(List.of(userAndGroupMicroservice, trainingMicroservice));
        given(microserviceService.getMicroservices(predicate, pageable)).willReturn(microservicePage);

        PageResultResource<MicroserviceDTO> microservicePageResultResource = microserviceFacade.getAllMicroservices(predicate, pageable);
        assertEquals(2, microservicePageResultResource.getContent().size());
        assertTrue(microservicePageResultResource.getContent().containsAll(Set.of(trainingMicroserviceDTO, userAndGroupMicroserviceDTO)));
    }

    @Test
    public void registerMicroserviceCreate() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);

        microserviceFacade.registerMicroservice(newMicroserviceDTO);
        then(roleService).should().createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should().createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should().createRole(mapToRole(newTraineeRoleDTO));
        then(groupService).should(times(1)).getGroupForDefaultRoles();
    }

    @Test
    public void registerMicroserviceCreateRoleAlreadyInDB() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        willThrow(EntityConflictException.class).given(roleService).createRole(any());
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);

        assertThrows(EntityConflictException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    public void registerMicroserviceCreateNoDefaultGroup() {
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        willThrow(EntityNotFoundException.class).given(groupService).getGroupForDefaultRoles();

        assertThrows(EntityNotFoundException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    public void registerMicroserviceCreateWithMultipleDefaultRoles() {
        newDesignerRoleDTO.setDefault(true);
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(false);
        given(groupService.getGroupForDefaultRoles()).willReturn(defaultGroup);

        assertThrows(UnprocessableEntityException.class, () -> microserviceFacade.registerMicroservice(newMicroserviceDTO));
        then(roleService).should(never()).createRole(mapToRole(newAdminRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newDesignerRoleDTO));
        then(roleService).should(never()).createRole(mapToRole(newTraineeRoleDTO));
    }

    @Test
    public void registerMicroserviceUpdateWithSameRoles() {
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
    public void registerMicroserviceUpdateWithNewRole() {
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
    public void registerMicroserviceUpdateRoleAlreadyInDB() {
        newMicroserviceDTO.setRoles(Set.of(newAdminRoleDTO, newDesignerRoleDTO, newOrganizerRoleDTO, newTraineeRoleDTO));
        given(microserviceService.existsByName(newMicroserviceDTO.getName())).willReturn(true);
        given(microserviceService.getMicroserviceByName(newMicroserviceDTO.getName())).willReturn(trainingMicroservice);
        given(roleService.getAllRolesOfMicroservice(trainingMicroservice.getName())).willReturn(new HashSet<>(Set.of(adminRole, designerRole, traineeRole)));
        willThrow(EntityConflictException.class).given(roleService).createRole(any());

        assertThrows(EntityConflictException.class, () ->microserviceFacade.registerMicroservice(newMicroserviceDTO));
    }

    @Test
    public void registerMicroserviceUpdateNoDefaultGroup() {
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
    public void registerMicroserviceUpdateNoDefaultRoleOfMicroservice() {
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
    public void registerMicroserviceUpdateWithMultipleDefaultRoles() {
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
    public void registerMicroserviceUpdateWithNewDefaultRole() {
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
