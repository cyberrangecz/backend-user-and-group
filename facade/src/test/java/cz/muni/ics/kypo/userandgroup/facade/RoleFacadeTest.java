package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.config.FacadeTestConfig;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupFacadeException;
import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.RoleFacade;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.service.interfaces.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.service.interfaces.RoleService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup.repository"})
@ComponentScan(basePackages = {
        "cz.muni.ics.kypo.userandgroup.facade",
        "cz.muni.ics.kypo.userandgroup.service",
        "cz.muni.ics.kypo.userandgroup.mapping"
})
@Import(FacadeTestConfig.class)
public class RoleFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private RoleFacade roleFacade;

    @MockBean
    private RoleService roleService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private MicroserviceService microserviceService;

    @MockBean
    private IDMGroupService idmGroupService;

    private Role r1 ;
    private RoleDTO roleDTO1;
    private Predicate predicate;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        r1 = new Role();
        r1.setId(1L);
        r1.setRoleType(RoleType.ADMINISTRATOR);

        roleDTO1 = new RoleDTO();
        roleDTO1.setId(1L);
        roleDTO1.setRoleType(RoleType.ADMINISTRATOR.toString());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void testGetById() {
        given(roleService.getById(anyLong())).willReturn(r1);
        RoleDTO roleDTO = roleFacade.getById(1L);

        assertEquals(r1.getRoleType().name(), roleDTO.getRoleType());

    }

    @Test
    public void testGetByIdWithServiceException() {
        given(roleService.getById(anyLong())).willThrow(new UserAndGroupServiceException());
        thrown.expect(UserAndGroupFacadeException.class);
        roleFacade.getById(1L);
    }

    @Test
    public void testGetByRoleType() {
        given(roleService.getByRoleType(any(RoleType.class))).willReturn(r1);
        RoleDTO roleDTO = roleFacade.getByRoleType(RoleType.ADMINISTRATOR);

        assertEquals(RoleType.ADMINISTRATOR.toString(), roleDTO.getRoleType().toString());
    }

    @Test
    public void testGetByRoleTypeWithServiceException() {
        given(roleService.getByRoleType(any(RoleType.class))).willThrow(new UserAndGroupFacadeException());
        thrown.expect(UserAndGroupFacadeException.class);
        roleFacade.getByRoleType(RoleType.ADMINISTRATOR);
    }

    @Test
    public void testGetAllRoles() {
        Microservice microservice = new Microservice("Training", "/training");
        given(microserviceService.getMicroservices()).willReturn(Collections.singletonList(microservice));

        Role role = new Role();
        role.setId(1L);
        role.setRoleType(RoleType.GUEST);
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setRoleType(RoleType.GUEST.toString());
        roleDTO.setNameOfMicroservice(microservice.getName());

        roleDTO1.setNameOfMicroservice("User and Group");

        Role[] rolesArray = new Role[1];
        rolesArray[0] = role;
        mockSpringSecurityContextForGet(rolesArray);

        Page<Role> rolePage = new PageImpl<>(Arrays.asList(r1));
        PageResultResource<RoleDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(roleDTO1));

        given(roleService.getAllRoles(pageable)).willReturn(rolePage);
        PageResultResource<RoleDTO> pageResultResource = roleFacade.getAllRoles(pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertTrue(pageResultResource.getContent().contains(roleDTO1));
        assertTrue(pageResultResource.getContent().contains(roleDTO));
    }

    private void mockSpringSecurityContextForGet(Role[] rolesArray) {
        ResponseEntity<Role[]> responseEntity = new ResponseEntity<>(rolesArray, HttpStatus.OK);
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails auth = Mockito.mock(OAuth2AuthenticationDetails.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getDetails()).willReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        given(auth.getTokenType()).willReturn("");
        given(auth.getTokenValue()).willReturn("");
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Role[].class))).willReturn(responseEntity);
    }
}
