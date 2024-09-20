package cz.muni.ics.kypo.userandgroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.PageResultResource;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserBasicViewDto;
import cz.muni.ics.kypo.userandgroup.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserDeletionResponseDTO;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.enums.dto.UserDeletionStatusDTO;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.exceptions.errors.ApiEntityError;
import cz.muni.ics.kypo.userandgroup.facade.UserFacade;
import cz.muni.ics.kypo.userandgroup.handler.CustomRestExceptionHandler;
import cz.muni.ics.kypo.userandgroup.util.TestAuthorityGranter;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static cz.muni.ics.kypo.userandgroup.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.userandgroup.util.ObjectConverter.convertObjectToJsonBytes;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestDataFactory.class)
public class UsersRestControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;
    private UsersRestController usersRestController;
    @Mock
    private UserFacade userFacade;
    @Mock
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private AutoCloseable closeable;
    private UserDTO userDTO1, userDTO2;
    private UserBasicViewDto userBasicViewDto1, userBasicViewDto2;
    private PageResultResource<UserDTO> userPageResultResource;
    private PageResultResource<UserBasicViewDto> userBasicViewDtoPageResultResource;

    @BeforeEach
    public void setup() throws RuntimeException {
        closeable = MockitoAnnotations.openMocks(this);
        usersRestController = new UsersRestController(userFacade, objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(usersRestController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()
                        )
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandler()).build();

        userDTO1 = testDataFactory.getUser1DTO();
        userDTO1.setId(1L);

        userDTO2 = testDataFactory.getUser2DTO();
        userDTO2.setId(2L);

        userBasicViewDto1 = testDataFactory.getUserBasicViewDto1();
        userBasicViewDto1.setId(1L);

        userBasicViewDto2 = testDataFactory.getUserBasicViewDto2();
        userBasicViewDto2.setId(2L);


        userPageResultResource = new PageResultResource<>(Arrays.asList(userDTO1, userDTO2));
        userBasicViewDtoPageResultResource = new PageResultResource<>(List.of(userBasicViewDto1, userBasicViewDto2));

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    public void contextLoads() {
        assertNotNull(usersRestController);
    }

    @Test
    public void getUserInfo() throws Exception {
        User user = new User();
        user.setSub(userDTO1.getSub());
        user.setFamilyName(userDTO1.getFamilyName());
        user.setIss(userDTO1.getIss());
        TestAuthorityGranter.mockSpringSecurityContextForGetUserInfo(RoleType.ROLE_USER_AND_GROUP_TRAINEE, user);
        given(userFacade.getUserInfo()).willReturn(userDTO1);

        MockHttpServletResponse result = mockMvc.perform(
                get("/users/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();

        assertEquals(convertObjectToJsonBytes(userDTO1), result.getContentAsString());
    }

    @Test
    public void testGetUsers() throws Exception {
        String valueAs = convertObjectToJsonBytes(userPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
        given(userFacade.getUsers(any(Predicate.class), any(Pageable.class))).willReturn(userBasicViewDtoPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(
                get("/users" + "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(userPageResultResource)), result.getContentAsString());
        then(userFacade).should().getUsers(any(Predicate.class), any(Pageable.class));
    }

    @Test
    public void testGetUser() throws Exception {
        given(userFacade.getUserById(userDTO1.getId())).willReturn(userDTO1);
        mockMvc.perform(
                get("/users" + "/{id}", userDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(userDTO1)));
        then(userFacade).should().getUserById(userDTO1.getId());
    }

    @Test
    public void testGetUserWithUserNotFound() throws Exception {
        given(userFacade.getUserById(userDTO1.getId())).willThrow(new EntityNotFoundException());
        MockHttpServletResponse response = mockMvc.perform(
                get("/users" + "/{id}", userDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void testGetAllUsersNotInGivenGroup() throws Exception {
        String valueAs = convertObjectToJsonBytes(userPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueAs);
        given(userFacade.getAllUsersNotInGivenGroup(anyLong(), any(Predicate.class), any(Pageable.class))).willReturn(userPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(
                get("/users" + "/not-in-group/{groupId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(userPageResultResource)), result.getContentAsString());
        then(userFacade).should().getAllUsersNotInGivenGroup(anyLong(), any(Predicate.class), any(Pageable.class));
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(
                delete("/users" + "/{id}", userDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        then(userFacade).should().deleteUser(userDTO1.getId());
    }

    @Test
    public void testDeleteUserNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(userFacade).deleteUser(userDTO1.getId());
        MockHttpServletResponse response = mockMvc.perform(
                delete("/users" + "/{id}", userDTO1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
        then(userFacade).should().deleteUser(userDTO1.getId());
    }

    @Test
    public void testDeleteUsers() throws Exception {
        UserDeletionResponseDTO deletionResponseDTO = new UserDeletionResponseDTO();
        deletionResponseDTO.setUser(userDTO2);
        deletionResponseDTO.setStatus(UserDeletionStatusDTO.EXTERNAL_VALID);
        mockMvc.perform(
                delete("/users" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(Arrays.asList(userDTO1.getId(), userDTO2.getId()))))
                .andExpect(status().isOk());
        then(userFacade).should().deleteUsers(Arrays.asList(userDTO1.getId(), userDTO2.getId()));
    }

    @Test
    public void testDeleteUsersWithNullRequestBody() throws Exception {
        mockMvc.perform(
                delete("/users" + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(null)))
                .andExpect(status().isBadRequest());
        then(userFacade).should(never()).deleteUsers(anyList());
    }

    @Test
    public void testGetRolesOfUser() throws Exception {
        given(userFacade.getRolesOfUserWithPagination(eq(userDTO1.getId()), any(Pageable.class), any(Predicate.class)))
                .willReturn(getPageResultResourceRolesDTO());
        mockMvc.perform(
                get("/users" + "/{id}/roles", userDTO1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(convertObjectToJsonBytes(getPageResultResourceRolesDTO())));
    }

    @Test
    public void testGetRolesOfUserWithExceptionFromFacade() throws Exception {
        given(userFacade.getRolesOfUserWithPagination(eq(userDTO1.getId()), any(Pageable.class), any(Predicate.class))).willThrow(new EntityNotFoundException());
        MockHttpServletResponse response = mockMvc.perform(
                get("/users" + "/{id}/roles", userDTO1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    private RoleDTO getAdminRoleDTO() {
        RoleDTO adminRole = new RoleDTO();
        adminRole.setId(1L);
        adminRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name());
        return adminRole;
    }

    private RoleDTO getGuestRoleDTO() {
        RoleDTO guestRole = new RoleDTO();
        guestRole.setId(2L);
        guestRole.setRoleType(RoleType.ROLE_USER_AND_GROUP_TRAINEE.name());
        return guestRole;
    }

    private PageResultResource<RoleDTO> getPageResultResourceRolesDTO() {
        return new PageResultResource<>(List.of(getAdminRoleDTO(), getGuestRoleDTO()));
    }
}

