package cz.cyberrange.platform.userandgroup.rest.facade.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Restricts a method to callers holding the ROLE_USER_AND_GROUP_ADMINISTRATOR or
 * ROLE_USER_AND_GROUP_POWER_USER authority.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "hasAnyAuthority(T(cz.cyberrange.platform.userandgroup.persistence.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR, "
        + "T(cz.cyberrange.platform.userandgroup.persistence.enums.RoleType).ROLE_USER_AND_GROUP_POWER_USER)")
public @interface IsAdminOrPowerUser {}
