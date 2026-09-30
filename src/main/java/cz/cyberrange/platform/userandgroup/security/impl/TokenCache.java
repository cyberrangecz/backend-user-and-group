package cz.cyberrange.platform.userandgroup.security.impl;

import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

/**
 * Caches the authentication resolved for a bearer token, keyed by the token's value, so a token
 * seen again before its cache entry expires skips re-validation.
 */
public class TokenCache {

  private final Map<String, TokenCacheItem> cache = new HashMap<>();

  // Default lifetime, in milliseconds, assigned to a cache entry when the token's own expiry does
  // not determine it.
  private int defaultExpireTime = 300000;

  private boolean cacheTokens = true;
  private boolean cacheNonExpiringTokens = false;

  // Caps a cache entry's lifetime to defaultExpireTime even when the token's own expiry is later.
  private boolean forceCacheExpireTime = false;

  public TokenCache() {}

  /**
   * Creates a cache configured with the given policy.
   *
   * @param defaultExpireTime default lifetime, in milliseconds, assigned to a cache entry when the
   *     token's own expiry does not determine it
   * @param cacheTokens whether validated tokens are cached at all
   * @param cacheNonExpiringTokens whether a token with no expiry is cached
   * @param forceCacheExpireTime whether a cache entry's lifetime is capped to defaultExpireTime
   *     even when the token's own expiry is later
   */
  public TokenCache(
      int defaultExpireTime,
      boolean cacheTokens,
      boolean cacheNonExpiringTokens,
      boolean forceCacheExpireTime) {
    this.defaultExpireTime = defaultExpireTime;
    this.cacheTokens = cacheTokens;
    this.cacheNonExpiringTokens = cacheNonExpiringTokens;
    this.forceCacheExpireTime = forceCacheExpireTime;
  }

  /**
   * Returns the cached entry for the given token value.
   *
   * @param key the token value used as the cache key
   * @return the cached entry, or null when caching is turned off, nothing is cached for that value,
   *     or the cached entry has expired
   */
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

  /**
   * Wraps the given access token and authentication together into a cache entry, and stores that
   * entry keyed by the token value when caching is enabled and either the token carries an expiry
   * or the cache is configured to also cache tokens with no expiry.
   *
   * @param accessToken the access token to cache
   * @param authToken the authentication resolved for that token
   * @return the wrapped entry, or null when the access token has already expired
   */
  public TokenCache.TokenCacheItem put(
      OAuth2AccessToken accessToken, AbstractAuthenticationToken authToken) {
    if (accessToken.getExpiresAt() == null || accessToken.getExpiresAt().isAfter(Instant.now())) {
      TokenCache.TokenCacheItem tco = new TokenCache.TokenCacheItem(accessToken, authToken);
      if (this.cacheTokens && (this.cacheNonExpiringTokens || accessToken.getExpiresAt() != null)) {
        this.cache.put(accessToken.getTokenValue(), tco);
      }
      return tco;
    }
    return null;
  }

  /** A cached authentication result for one access token, together with its cache expiry. */
  public class TokenCacheItem {
    private final Instant cacheExpire;
    private OAuth2AccessToken token;
    private AbstractAuthenticationToken auth;

    /**
     * Wraps a token together with the authentication resolved for it, and computes when the
     * resulting cache entry expires: the token's own expiry, unless the token has none, or the
     * cache is configured to cap it and the token's remaining lifetime is longer than the
     * configured default, in which case the entry expires the configured default duration from now.
     *
     * @param token the access token being cached
     * @param auth the authentication resolved for the token
     */
    private TokenCacheItem(OAuth2AccessToken token, AbstractAuthenticationToken auth) {
      this.token = token;
      this.auth = auth;
      if (this.token.getExpiresAt() == null
          || TokenCache.this.forceCacheExpireTime
              && this.token.getExpiresAt().getEpochSecond() - System.currentTimeMillis()
                  > (long) TokenCache.this.defaultExpireTime) {
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
