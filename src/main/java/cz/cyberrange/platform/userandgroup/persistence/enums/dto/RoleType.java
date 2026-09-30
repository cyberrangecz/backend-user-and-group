package cz.cyberrange.platform.userandgroup.persistence.enums.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Distinguishes the access levels a default role can grant: administrator, user or guest. */
@Schema(name = "RoleTypeDTO", description = "Level of access a role grants.")
public enum RoleType {
  ADMINISTRATOR,
  USER,
  GUEST
}
