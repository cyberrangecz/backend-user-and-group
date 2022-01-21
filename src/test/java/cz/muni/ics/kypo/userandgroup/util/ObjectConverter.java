package cz.muni.ics.kypo.userandgroup.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.userandgroup.domain.IDMGroup;
import cz.muni.ics.kypo.userandgroup.domain.Role;
import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.dto.group.GroupDTO;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.dto.user.UserForGroupsDTO;
import cz.muni.ics.kypo.userandgroup.enums.dto.ImplicitGroupNames;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final ModelMapper MODEL_MAPPER = new ModelMapper();

    public static String convertObjectToJsonBytes(Object object) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    public static String convertJsonBytesToObject(String object) throws IOException {
        return OBJECT_MAPPER.readValue(object, String.class);
    }

    public static <T> T convertJsonBytesToObject(String object, TypeReference<T> tTypeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, tTypeReference);
    }

    public static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, objectClass);
    }

    public static String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }

    public static UserDTO convertToUserDTO(User user, Set<Role> roles) {
        UserDTO userDTO = MODEL_MAPPER.map(user, UserDTO.class);
        Set<RoleDTO> rolesDTO = new HashSet<>();
        for (Role role : roles) {
            rolesDTO.add(convertToRoleDTO(role));
        }
        userDTO.setRoles(rolesDTO);
        return userDTO;
    }

    public static RoleDTO convertToRoleDTO(Role role) {
        RoleDTO roleDTO = MODEL_MAPPER.map(role, RoleDTO.class);
        roleDTO.setNameOfMicroservice(role.getMicroservice().getName());
        roleDTO.setIdOfMicroservice(role.getMicroservice().getId());
        return roleDTO;
    }

    public static GroupDTO convertToGroupDTO(IDMGroup group) {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(group.getId());
        groupDTO.setDescription(group.getDescription());
        groupDTO.setName(group.getName());
        if (Set.of(ImplicitGroupNames.DEFAULT_GROUP.getName(), ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(), ImplicitGroupNames.USER_AND_GROUP_USER.getName()).contains(groupDTO.getName())) {
            groupDTO.setCanBeDeleted(false);
        }

        Set<RoleDTO> roles = new HashSet<>();
        for (Role role : group.getRoles()) {
            roles.add(convertToRoleDTO(role));
        }
        groupDTO.setRoles(roles);

        groupDTO.setUsers(group.getUsers().stream()
                .map(user -> {
                    return MODEL_MAPPER.map(user, UserForGroupsDTO.class);
                })
                .collect(Collectors.toSet()));
        return groupDTO;
    }
}
