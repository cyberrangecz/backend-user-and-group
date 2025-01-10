package cz.cyberrange.platform.userandgroup.rest.facade.annotations.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The custom annotation <i>@IsTrainee<i/>. All methods annotated with this annotation expect the user has a role <strong>ROLE_USER_AND_GROUP_Trainee<strong/>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(cz.cyberrange.platform.userandgroup.persistence.enums.RoleType).ROLE_USER_AND_GROUP_TRAINEE)")
public @interface IsTrainee {}
