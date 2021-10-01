package cz.muni.ics.kypo.userandgroup.rest.util;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.entities.User;
import cz.muni.ics.kypo.userandgroup.entities.enums.RoleType;
import cz.muni.ics.kypo.userandgroup.security.model.OAuth2AccessTokenImpl;
import cz.muni.ics.kypo.userandgroup.security.model.UserInfo;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

public class TestAuthorityGranter{


    public static void mockSpringSecurityContextForGet(RoleType roleType){
        AuthenticationProvider authenticationProvider = Mockito.mock(AuthenticationProvider.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(new User(), null, mockGrantedAuthorities(roleType));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        given(authenticationProvider.supports(any())).willReturn(true);
        given(authenticationProvider.authenticate(any())).willReturn(authenticationToken);
    }

    public static void mockSpringSecurityContextForGetUserInfo(RoleType roleType, User user){
        UserInfo userInfo = createUserInfo(user);
        PreAuthenticatedAuthenticationToken authenticationToken = new PreAuthenticatedAuthenticationToken(userInfo, userInfo.getSub(), mockGrantedAuthorities(roleType));
        OAuth2Authentication oAuth2Authentication = Mockito.mock(OAuth2Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(oAuth2Authentication);
        given(oAuth2Authentication.getUserAuthentication()).willReturn(authenticationToken);
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

    private static UserInfo createUserInfo(User user){
        UserInfo userInfo = new UserInfo(user.getSub());
        userInfo.setName(user.getFullName());
        userInfo.setGivenName(user.getGivenName());
        userInfo.setFamilyName(user.getFamilyName());
        userInfo.setIssuer(user.getIss());
        return userInfo;
    }
}
