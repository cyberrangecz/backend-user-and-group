package cz.muni.ics.kypo.userandgroup.annotations.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The custom annotation <i>@IsAdminOrPowerUser<i/>. All methods annotated with this annotation expect
 * the user has a role <strong>ROLE_USER_AND_GROUP_ADMINISTRATOR<strong/> or <strong>ROLE_USER_AND_GROUP_POWER_USER<strong/>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.userandgroup.enums.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR, " +
        "T(cz.muni.ics.kypo.userandgroup.enums.RoleType).ROLE_USER_AND_GROUP_POWER_USER)")
public @interface IsAdminOrPowerUser {}
