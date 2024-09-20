package cz.muni.ics.kypo.userandgroup.integration;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.user.UserCreateDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.mapping.RoleMapperImpl;
import cz.muni.ics.kypo.userandgroup.mapping.UserMapperImpl;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.service.IDMGroupService;
import cz.muni.ics.kypo.userandgroup.service.IdenticonService;
import cz.muni.ics.kypo.userandgroup.service.SecurityService;
import cz.muni.ics.kypo.userandgroup.service.UserService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {
        UserFacade.class,
        TestDataFactory.class,
        UserService.class,
        IDMGroupService.class,
        IdenticonService.class,
        SecurityService.class,
        UserMapperImpl.class,
        RoleMapperImpl.class
})
@DataJpaTest
@EntityScan(basePackages = "cz.muni.ics.kypo.userandgroup.domain")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.userandgroup.repository")
@EnableTransactionManagement
@EnableRetry
@RunWith(ConcurrentTestRunner.class)
public class UserConcurrentIntegrationTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IDMGroupRepository idmGroupRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MicroserviceRepository microserviceRepository;
    @MockBean
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    private UserDTO userDTO;

    @Before
    public void init() throws Exception {
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        userDTO = new UserDTO();
        userDTO.setFullName("Ing. Michael Johnson");
        userDTO.setFamilyName("Johnson");
        userDTO.setGivenName("Michael");
        userDTO.setSub("mail@muni.cz");
        userDTO.setIss("oidc.ics.muni.cz");

        Microservice userAndGroupMicroservice = testDataFactory.getKypoUaGMicroservice();
        microserviceRepository.save(userAndGroupMicroservice);

        Role role = testDataFactory.getUAGTraineeRole();
        role.setMicroservice(userAndGroupMicroservice);
        roleRepository.save(role);

        IDMGroup defaultGroup = testDataFactory.getUAGDefaultGroup();
        defaultGroup.setRoles(Set.of(role));
        idmGroupRepository.save(defaultGroup);
    }

    @Test
    @ThreadCount(50)
    public void createOrUpdateUser() throws Exception {
        UserDTO createdUserDTO = userFacade.createOrUpdateOrGetOIDCUser(getOIDCUserInfo());
        assertEquals(userDTO.getFullName(), createdUserDTO.getFullName());
        assertEquals(userDTO.getSub(), createdUserDTO.getSub());
        assertEquals(userDTO.getIss(), createdUserDTO.getIss());
    }

    @After
    public void testCreatedUser() throws Exception {
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("Ing. Michael Johnson", users.get(0).getFullName());
    }

    private UserCreateDTO getOIDCUserInfo() {
        UserCreateDTO oidcUserInfo = new UserCreateDTO();
        oidcUserInfo.setSub("mail@muni.cz");
        oidcUserInfo.setIss("oidc.ics.muni.cz");
        oidcUserInfo.setFullName("Ing. Michael Johnson");
        oidcUserInfo.setGivenName("Michael");
        oidcUserInfo.setFamilyName("Johnson");
        return oidcUserInfo;
    }
}
