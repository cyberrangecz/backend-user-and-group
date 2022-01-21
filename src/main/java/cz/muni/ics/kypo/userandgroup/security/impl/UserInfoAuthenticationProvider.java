package cz.muni.ics.kypo.userandgroup.security.impl;

import com.nimbusds.jwt.JWTParser;
import cz.muni.ics.kypo.userandgroup.security.AuthorityGranter;
import cz.muni.ics.kypo.userandgroup.security.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class UserInfoAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoAuthenticationProvider.class);

    private final TokenCache tokenCache;
    private final AuthorityGranter authorityGranter;
    private final UserInfoValidator userInfoValidator;


    public UserInfoAuthenticationProvider(AuthorityGranter authorityGranter,
                                          UserInfoValidator userInfoValidator,
                                          TokenCache tokenCache) {
        this.authorityGranter = authorityGranter;
        this.userInfoValidator = userInfoValidator;
        this.tokenCache = tokenCache;
    }

    public UserInfoAuthenticationProvider(AuthorityGranter authorityGranter,
                                          UserInfoValidator userInfoValidator) {
        this(authorityGranter, userInfoValidator, new TokenCache());
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String bearerToken = ((BearerTokenAuthenticationToken) authentication).getToken();
        TokenCache.TokenCacheItem cacheItem = this.tokenCache.get(bearerToken);
        if (cacheItem != null) {
            return cacheItem.getAuth();
        } else {
            cacheItem = this.validateToken(bearerToken, authentication);
            return cacheItem != null ? cacheItem.getAuth() : null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private TokenCache.TokenCacheItem validateToken(String bearerToken, Authentication authentication) {
        Jwt jwt = this.getJwt(bearerToken);
        UserInfo userInfo = this.userInfoValidator.validate(bearerToken, jwt.getIssuer().toString());
        AbstractAuthenticationToken authToken = this.convertToAuthenticationToken(jwt, userInfo, authentication);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, bearerToken, jwt.getIssuedAt(), jwt.getExpiresAt());
        return this.tokenCache.put(accessToken, authToken);
    }

    private AbstractAuthenticationToken convertToAuthenticationToken(Jwt jwt, UserInfo userInfo, Authentication authentication) {
        Collection<GrantedAuthority> authorities = this.authorityGranter.getAuthorities(userInfo);
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, authorities, userInfo.getSub());
        jwtAuthenticationToken.setDetails(authentication.getDetails());
        return jwtAuthenticationToken;
    }

    public Jwt getJwt(String token) throws JwtException {
        try {
            com.nimbusds.jwt.JWT tmpJwt = this.parse(token);
            return this.createJwt(token, tmpJwt);
        } catch (JwtException var4) {
            throw new AuthenticationServiceException(var4.getMessage(), var4);
        }
    }

    private com.nimbusds.jwt.JWT parse(String token) {
        try {
            return JWTParser.parse(token);
        } catch (Exception var3) {
            throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", var3.getMessage()), var3);
        }
    }

    private Jwt createJwt(String token, com.nimbusds.jwt.JWT parsedJwt) {
        try {
            Map<String, Object> headers = new LinkedHashMap(parsedJwt.getHeader().toJSONObject());
            Map<String, Object> claims = this.modifyTimeClaims(parsedJwt.getJWTClaimsSet().getClaims());
            return Jwt.withTokenValue(token).headers((h) -> {
                h.putAll(headers);
            }).claims((c) -> {
                c.putAll(claims);
            }).build();
        } catch (Exception var7) {
            if (var7.getCause() instanceof ParseException) {
                throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", "Malformed payload"));
            } else {
                throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", var7.getMessage()), var7);
            }
        }
    }

    public Map<String, Object> modifyTimeClaims(Map<String, Object> parsedClaims) {
        Map<String, Object> modifiedClaims = new HashMap<>(parsedClaims);
        Instant expiresAt = modifiedClaims.get("exp") != null ? ((Date) modifiedClaims.get("exp")).toInstant() : null;
        Instant issuedAt = modifiedClaims.get("iat") != null ? ((Date) modifiedClaims.get("iat")).toInstant() : null;
        modifiedClaims.put("exp", expiresAt);
        modifiedClaims.put("iat", issuedAt);
        if (issuedAt == null && expiresAt != null) {
            modifiedClaims.put("iat", expiresAt.minusSeconds(1L));
        }
        return modifiedClaims;
    }
}
