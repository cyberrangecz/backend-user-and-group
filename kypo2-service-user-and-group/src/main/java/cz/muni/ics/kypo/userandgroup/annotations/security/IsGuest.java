package cz.muni.ics.kypo.userandgroup.annotations.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The custom annotation <i>@IsGuest<i/>. All methods annotated with this annotation expect the user has a role <strong>ROLE_USER_AND_GROUP_GUEST<strong/>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.entities.enums.RoleType).ROLE_USER_AND_GROUP_GUEST)")
public @interface IsGuest {
}
