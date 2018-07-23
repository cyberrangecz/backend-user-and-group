package cz.muni.ics.kypo.userandgroup.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.facade.interfaces.UserFacade;
import cz.muni.ics.kypo.userandgroup.mapping.BeanMapping;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.service.interfaces.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.userandgroup.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.userandgroup"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.facade", "cz.muni.ics.kypo.userandgroup.service"})
public class UserFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private UserFacade userFacade;

    @MockBean
    private UserService userService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private BeanMapping beanMapping;

    private User user1, user2;
    private UserDTO userDTO1, userDTO2;
    private Predicate predicate;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        user1 = new User("user1");
        user1.setId(1L);
        user1.setFullName("User One");
        user1.setMail("user.one@mail.com");
        user1.setStatus(UserAndGroupStatus.VALID);

        user2 = new User("user2");
        user2.setId(2L);
        user2.setFullName("User Two");
        user2.setMail("user.two@mail.com");
        user2.setStatus(UserAndGroupStatus.VALID);

        userDTO1 = new UserDTO();
        userDTO1.setLogin("user1");
        userDTO1.setId(1L);
        userDTO1.setFullName("User One");
        userDTO1.setMail("user.one@mail.com");

        userDTO2 = new UserDTO();
        userDTO2.setLogin("user2");
        userDTO2.setId(2L);
        userDTO2.setFullName("User Two");
        userDTO2.setMail("user.two@mail.com");
    }

    @Test
    public void testGetUsers() {
        Page<User> rolePage = new PageImpl<>(Arrays.asList(user1, user2));
        PageResultResource<UserDTO> pageResult = new PageResultResource<>();
        pageResult.setContent(Arrays.asList(userDTO1, userDTO2));

        given(userService.getAllUsers(predicate, pageable)).willReturn(rolePage);
        given(beanMapping.mapToPageResultDTO(any(Page.class), eq(UserDTO.class))).willReturn(pageResult);
        PageResultResource<UserDTO> pageResultResource = userFacade.getUsers(predicate,pageable);

        assertEquals(2, pageResultResource.getContent().size());
        assertEquals(userDTO1, pageResultResource.getContent().get(0));
        assertEquals(userDTO2, pageResultResource.getContent().get(1));
    }
}
