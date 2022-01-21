package cz.muni.ics.kypo.userandgroup.util;

import cz.muni.ics.kypo.userandgroup.domain.User;
import cz.muni.ics.kypo.userandgroup.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.security.model.UserInfo;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

public class TestAuthorityGranter {


    public static void mockSpringSecurityContextForGet(RoleType roleType) {
        AuthenticationProvider authenticationProvider = Mockito.mock(AuthenticationProvider.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(new User(), null, mockGrantedAuthorities(roleType));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        given(authenticationProvider.supports(any())).willReturn(true);
        given(authenticationProvider.authenticate(any())).willReturn(authenticationToken);
    }

    public static void mockSpringSecurityContextForGetUserInfo(RoleType roleType, User user) {
        Jwt jwt = new Jwt("bearer-token-value", null, null, Map.of("alg", "HS256"), Map.of("iss", user.getIss(), "sub", user.getSub()));
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, List.of( new SimpleGrantedAuthority(roleType.name())), user.getSub());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private static List<GrantedAuthority> mockGrantedAuthorities(RoleType roleType) {
        if (roleType == RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR) {
            return List.of(
                    new SimpleGrantedAuthority(RoleType.ROLE_USER_AND_GROUP_ADMINISTRATOR.name()),
                    new SimpleGrantedAuthority(RoleType.ROLE_USER_AND_GROUP_GUEST.name()),
                    new SimpleGrantedAuthority(RoleType.ROLE_USER_AND_GROUP_USER.name()));
        } else if (roleType == RoleType.ROLE_USER_AND_GROUP_USER) {
            return List.of(
                    new SimpleGrantedAuthority(RoleType.ROLE_USER_AND_GROUP_USER.name()),
                    new SimpleGrantedAuthority(RoleType.ROLE_USER_AND_GROUP_GUEST.name()));
        } else {
            return List.of(new SimpleGrantedAuthority(RoleType.ROLE_USER_AND_GROUP_GUEST.name()));
        }
    }

    private static UserInfo createUserInfo(User user) {
        UserInfo userInfo = new UserInfo(user.getSub());
        userInfo.setName(user.getFullName());
        userInfo.setGivenName(user.getGivenName());
        userInfo.setFamilyName(user.getFamilyName());
        userInfo.setIssuer(user.getIss());
        return userInfo;
    }
}
