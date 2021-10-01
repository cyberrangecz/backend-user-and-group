package cz.muni.ics.kypo.userandgroup.security.impl;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Strings;
import cz.muni.ics.kypo.userandgroup.security.AuthorityGranter;
import cz.muni.ics.kypo.userandgroup.security.ClientConfigurationService;
import cz.muni.ics.kypo.userandgroup.security.model.OAuth2AccessTokenImpl;
import cz.muni.ics.kypo.userandgroup.security.model.UserInfo;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static cz.muni.ics.kypo.userandgroup.security.enums.OIDCItems.*;

public class UserInfoTokenService implements ResourceServerTokenServices {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoTokenService.class);
    private final HttpComponentsClientHttpRequestFactory factory;
    private DynamicServerConfigurationService serverConfigurationService;
    private ClientConfigurationService clientConfigurationService;
    private AuthorityGranter authorityGranter;
    private int defaultExpireTime;
    private boolean forceCacheExpireTime;
    private boolean cacheNonExpiringTokens;
    private boolean cacheTokens;
    private final Map<String, UserInfoTokenService.TokenCacheObject> authCache;

    public UserInfoTokenService() {
        this(HttpClientBuilder.create().useSystemProperties().build());
    }

    public UserInfoTokenService(HttpClient httpClient) {
        this.authorityGranter = null;
        this.defaultExpireTime = 300000;
        this.forceCacheExpireTime = false;
        this.cacheNonExpiringTokens = false;
        this.cacheTokens = true;
        this.authCache = new HashMap<>();
        this.factory = new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    public AuthorityGranter getAuthorityGranter() {
        return authorityGranter;
    }

    public void setAuthorityGranter(AuthorityGranter authorityGranter) {
        this.authorityGranter = authorityGranter;
    }

    public DynamicServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(DynamicServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public ClientConfigurationService getClientConfigurationService() {
        return clientConfigurationService;
    }

    public void setClientConfigurationService(ClientConfigurationService clientConfigurationService) {
        this.clientConfigurationService = clientConfigurationService;
    }

    public int getDefaultExpireTime() {
        return this.defaultExpireTime;
    }

    public void setDefaultExpireTime(int defaultExpireTime) {
        this.defaultExpireTime = defaultExpireTime;
    }

    public boolean isForceCacheExpireTime() {
        return this.forceCacheExpireTime;
    }

    public void setForceCacheExpireTime(boolean forceCacheExpireTime) {
        this.forceCacheExpireTime = forceCacheExpireTime;
    }

    public boolean isCacheNonExpiringTokens() {
        return this.cacheNonExpiringTokens;
    }

    public void setCacheNonExpiringTokens(boolean cacheNonExpiringTokens) {
        this.cacheNonExpiringTokens = cacheNonExpiringTokens;
    }

    public boolean isCacheTokens() {
        return this.cacheTokens;
    }

    public void setCacheTokens(boolean cacheTokens) {
        this.cacheTokens = cacheTokens;
    }

    private UserInfoTokenService.TokenCacheObject checkCache(String key) {
        if (this.cacheTokens && this.authCache.containsKey(key)) {
            UserInfoTokenService.TokenCacheObject tco = this.authCache.get(key);
            if (tco != null && tco.cacheExpire != null && tco.cacheExpire.after(new Date())) {
                return tco;
            }
            this.authCache.remove(key);
        }
        return null;
    }

    private UserInfoTokenService.TokenCacheObject parseToken(String accessToken) {
        try {
            DecodedJWT decodedToken = this.decodeJWTAccessToken(accessToken);
            String userInfoUri = this.serverConfigurationService.getServerConfiguration(decodedToken.getIssuer()).getUserInfoUri();

            UserInfo userInfo = this.getUserInfo(accessToken, userInfoUri);
            userInfo.setIssuer(decodedToken.getIssuer());

            OAuth2Authentication auth = new OAuth2Authentication(this.createStoredRequest(decodedToken), this.createUserAuthentication(decodedToken, userInfo));
            OAuth2AccessToken token = this.createAccessToken(decodedToken, userInfo, accessToken);
            if (token.getExpiration() == null || token.getExpiration().after(new Date())) {
                UserInfoTokenService.TokenCacheObject tco = new UserInfoTokenService.TokenCacheObject(token, auth);
                if (this.cacheTokens && (this.cacheNonExpiringTokens || token.getExpiration() != null)) {
                    this.authCache.put(accessToken, tco);
                }
                return tco;
            }
            return null;
        } catch (IllegalArgumentException var15) {
            logger.error("Unable to load introspection URL or client configuration", var15);
            return null;
        }
    }

    private DecodedJWT decodeJWTAccessToken(String accessToken) {
        try {
            return JWT.decode(accessToken);
        } catch (JWTDecodeException ex) {
            throw new InternalAuthenticationServiceException("Unable to parse access token to JWT format.");
        }
    }

    private OAuth2Request createStoredRequest(DecodedJWT token) {
        String clientId = null;
        if (getClientConfigurationService() != null && getClientConfigurationService().getClientConfiguration(token.getIssuer()) != null) {
            clientId = getClientConfigurationService().getClientConfiguration(token.getIssuer()).getClientId();
        }
        String rawScopes = null;

        if (token.getClaim(SCOPE.getName()) != null) {
            rawScopes = token.getClaim(SCOPE.getName()).asString();
        }
        if (token.getClaim(SCP.getName()) != null) {
            rawScopes = token.getClaim(SCP.getName()).asString();
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLIENT_ID.getName(), clientId);
        parameters.put(SCOPE.getName(), rawScopes);
        return new OAuth2Request(parameters, clientId, null, true, OAuth2Utils.parseParameterList(rawScopes), null, null, null, null);
    }

    private UserInfo getUserInfo(String accessToken, String userInfoUrl) {
        RestTemplate restTemplate = new RestTemplate(this.factory) {
            protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
                ClientHttpRequest httpRequest = super.createRequest(url, method);
                httpRequest.getHeaders().add("Authorization", String.format("Bearer %s", accessToken));
                return httpRequest;
            }
        };
        String userInfoSrc = restTemplate.getForObject(userInfoUrl, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        try {
            return objectMapper.readValue(userInfoSrc, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new InternalAuthenticationServiceException("Unable to parse user info response.");
        }
    }

    private Authentication createUserAuthentication(DecodedJWT token, UserInfo userInfo) {
        return new PreAuthenticatedAuthenticationToken(userInfo, token, this.authorityGranter.getAuthorities(userInfo));
    }

    private OAuth2AccessToken createAccessToken(DecodedJWT decodedToken, UserInfo userInfo, String tokenString) {
        return new OAuth2AccessTokenImpl(userInfo, tokenString, decodedToken.getExpiresAt());
    }

    private DecodedJWT validateJWT(DecodedJWT jwt) {
        if (!Strings.isNullOrEmpty(jwt.getIssuer())) {
            // Check signature
            List<Map<String, Object>> jwks = this.serverConfigurationService.getServerConfiguration(jwt.getIssuer()).getJwks();
            try {
                Map<String, Object> webKey = jwks.stream()
                        .filter(key -> key.get(KID.getName()).equals(jwt.getKeyId()))
                        .findFirst()
                        .orElseThrow(() -> new InternalAuthenticationServiceException("Cannot find public key: " + jwt.getKeyId() + " in the key set of the issuer."));
                Jwk jwk = Jwk.fromValues(webKey);
                Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                algorithm.verify(jwt);
            } catch (JwkException ex) {
                throw new InternalAuthenticationServiceException("Invalid token. Couldn't get JSON Web public key!");

            }
            // Check expiration
            if (jwt.getExpiresAt().before(Calendar.getInstance().getTime())) {
                throw new InternalAuthenticationServiceException("Expired token!");
            }
            return jwt;
        } else {
            throw new IllegalArgumentException("No issuer claim found in JWT");
        }
    }

    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {
        UserInfoTokenService.TokenCacheObject cacheAuth = this.checkCache(accessToken);
        if (cacheAuth != null) {
            return cacheAuth.auth;
        } else {
            cacheAuth = this.parseToken(accessToken);
            return cacheAuth != null ? cacheAuth.auth : null;
        }
    }

    public OAuth2AccessToken readAccessToken(String accessToken) {
        UserInfoTokenService.TokenCacheObject cacheAuth = this.checkCache(accessToken);
        if (cacheAuth != null) {
            return cacheAuth.token;
        } else {
            cacheAuth = this.parseToken(accessToken);
            return cacheAuth != null ? cacheAuth.token : null;
        }
    }

    private class TokenCacheObject {
        OAuth2AccessToken token;
        OAuth2Authentication auth;
        Date cacheExpire;

        private TokenCacheObject(OAuth2AccessToken token, OAuth2Authentication auth) {
            this.token = token;
            this.auth = auth;
            if (this.token.getExpiration() == null || UserInfoTokenService.this.forceCacheExpireTime && this.token.getExpiration().getTime() - System.currentTimeMillis() > (long) UserInfoTokenService.this.defaultExpireTime) {
                Calendar cal = Calendar.getInstance();
                cal.add(14, UserInfoTokenService.this.defaultExpireTime);
                this.cacheExpire = cal.getTime();
            } else {
                this.cacheExpire = this.token.getExpiration();
            }

        }
    }
}
