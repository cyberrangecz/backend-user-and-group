package cz.cyberrange.platform.userandgroup.integration;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.userandgroup.api.mapping.RoleMapperImpl;
import cz.cyberrange.platform.userandgroup.api.mapping.UserMapperImpl;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserCreateDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import cz.cyberrange.platform.userandgroup.persistence.repository.IDMGroupRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.RoleRepository;
import cz.cyberrange.platform.userandgroup.persistence.repository.UserRepository;
import cz.cyberrange.platform.userandgroup.service.IDMGroupService;
import cz.cyberrange.platform.userandgroup.service.IdenticonService;
import cz.cyberrange.platform.userandgroup.service.SecurityService;
import cz.cyberrange.platform.userandgroup.service.UserService;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
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
@EntityScan(basePackages = "cz.cyberrange.platform.userandgroup.persistence.entity")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.userandgroup.persistence.repository")
@EnableTransactionManagement
@EnableRetry
@RunWith(ConcurrentTestRunner.class)
class UserConcurrentIntegrationTest {

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
    void init() throws Exception {
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        userDTO = new UserDTO();
        userDTO.setFullName("Ing. Michael Johnson");
        userDTO.setFamilyName("Johnson");
        userDTO.setGivenName("Michael");
        userDTO.setSub("mail@test.cz");
        userDTO.setIss("oidc.provider.cz");

        Microservice userAndGroupMicroservice = testDataFactory.getCrczpUaGMicroservice();
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
    void createOrUpdateUser() throws Exception {
        UserDTO createdUserDTO = userFacade.createOrUpdateOrGetOIDCUser(getOIDCUserInfo());
        assertEquals(userDTO.getFullName(), createdUserDTO.getFullName());
        assertEquals(userDTO.getSub(), createdUserDTO.getSub());
        assertEquals(userDTO.getIss(), createdUserDTO.getIss());
    }

    @After
    void testCreatedUser() throws Exception {
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("Ing. Michael Johnson", users.get(0).getFullName());
    }

    private UserCreateDTO getOIDCUserInfo() {
        UserCreateDTO oidcUserInfo = new UserCreateDTO();
        oidcUserInfo.setSub("mail@test.cz");
        oidcUserInfo.setIss("oidc.provider.cz");
        oidcUserInfo.setFullName("Ing. Michael Johnson");
        oidcUserInfo.setGivenName("Michael");
        oidcUserInfo.setFamilyName("Johnson");
        return oidcUserInfo;
    }
}
