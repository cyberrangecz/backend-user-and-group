package cz.muni.ics.kypo.userandgroup.annotations.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dominik Pilar
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.userandgroup.model.RoleType).ROLE_USER_AND_GROUP_ADMINISTRATOR)")
public @interface IsAdmin {
}
