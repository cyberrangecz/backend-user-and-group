package cz.muni.ics.kypo.userandgroup.security.impl;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TokenCache {

    private final Map<String, TokenCacheItem> cache = new HashMap<>();
    private int defaultExpireTime = 300000;
    private boolean cacheTokens = true;
    private boolean cacheNonExpiringTokens = false;
    private boolean forceCacheExpireTime = false;

    public TokenCache() {
    }

    public TokenCache(int defaultExpireTime, boolean cacheTokens, boolean cacheNonExpiringTokens, boolean forceCacheExpireTime) {
        this.defaultExpireTime = defaultExpireTime;
        this.cacheTokens = cacheTokens;
        this.cacheNonExpiringTokens = cacheNonExpiringTokens;
        this.forceCacheExpireTime = forceCacheExpireTime;
    }

    public TokenCache.TokenCacheItem get(String key) {
        if (this.cacheTokens && this.cache.containsKey(key)) {
            TokenCache.TokenCacheItem tco = this.cache.get(key);
            if (tco != null && tco.cacheExpire != null && tco.cacheExpire.isAfter(Instant.now())) {
                return tco;
            }
            this.cache.remove(key);
        }
        return null;
    }

    public TokenCache.TokenCacheItem put(OAuth2AccessToken accessToken, AbstractAuthenticationToken authToken) {
        if (accessToken.getExpiresAt() == null || accessToken.getExpiresAt().isAfter(Instant.now())) {
            TokenCache.TokenCacheItem tco = new TokenCache.TokenCacheItem(accessToken, authToken);
            if (this.cacheTokens && (this.cacheNonExpiringTokens || accessToken.getExpiresAt() != null)) {
                this.cache.put(accessToken.getTokenValue(), tco);
            }
            return tco;
        }
        return null;
    }

    public class TokenCacheItem {
        private final Instant cacheExpire;
        private OAuth2AccessToken token;
        private AbstractAuthenticationToken auth;

        private TokenCacheItem(OAuth2AccessToken token, AbstractAuthenticationToken auth) {
            this.token = token;
            this.auth = auth;
            if (this.token.getExpiresAt() == null || TokenCache.this.forceCacheExpireTime &&
                    this.token.getExpiresAt().getEpochSecond() - System.currentTimeMillis() > (long) TokenCache.this.defaultExpireTime) {
                Calendar cal = Calendar.getInstance();
                cal.add(14, TokenCache.this.defaultExpireTime);
                this.cacheExpire = cal.getTime().toInstant();
            } else {
                this.cacheExpire = this.token.getExpiresAt();
            }

        }

        public OAuth2AccessToken getToken() {
            return token;
        }

        public void setToken(OAuth2AccessToken token) {
            this.token = token;
        }

        public AbstractAuthenticationToken getAuth() {
            return auth;
        }

        public void setAuth(AbstractAuthenticationToken auth) {
            this.auth = auth;
        }
    }
}
