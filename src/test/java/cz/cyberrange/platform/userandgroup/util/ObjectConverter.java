package cz.cyberrange.platform.userandgroup.util;

import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserForGroupsDTO;
import cz.cyberrange.platform.userandgroup.persistence.entity.IDMGroup;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import cz.cyberrange.platform.userandgroup.persistence.enums.dto.ImplicitGroupNames;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public class ObjectConverter {

  private static final JsonMapper JSON_MAPPER = new JsonMapper();
  private static final ModelMapper MODEL_MAPPER = new ModelMapper();

  public static String convertObjectToJsonBytes(Object object) {
    return JSON_MAPPER.writeValueAsString(object);
  }

  public static String convertJsonBytesToObject(String object) {
    return JSON_MAPPER.readValue(object, String.class);
  }

  public static <T> T convertJsonBytesToObject(String object, TypeReference<T> tTypeReference) {
    return JSON_MAPPER.readValue(object, tTypeReference);
  }

  public static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) {
    return JSON_MAPPER.readValue(object, objectClass);
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
    if (Set.of(
            ImplicitGroupNames.DEFAULT_GROUP.getName(),
            ImplicitGroupNames.USER_AND_GROUP_ADMINISTRATOR.getName(),
            ImplicitGroupNames.USER_AND_GROUP_POWER_USER.getName())
        .contains(groupDTO.getName())) {
      groupDTO.setCanBeDeleted(false);
    }

    Set<RoleDTO> roles = new HashSet<>();
    for (Role role : group.getRoles()) {
      roles.add(convertToRoleDTO(role));
    }
    groupDTO.setRoles(roles);

    groupDTO.setUsers(
        group.getUsers().stream()
            .map(
                user -> {
                  return MODEL_MAPPER.map(user, UserForGroupsDTO.class);
                })
            .collect(Collectors.toSet()));
    return groupDTO;
  }
}
