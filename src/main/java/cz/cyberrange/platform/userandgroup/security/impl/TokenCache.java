package cz.cyberrange.platform.userandgroup.security.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

/**
 * Thread-safe cache of the authentication resolved for a bearer token, keyed by the token's value,
 * so a token seen again before its cache entry expires skips re-validation.
 */
public class TokenCache {

  private final Map<String, TokenCacheItem> cache = new ConcurrentHashMap<>();

  // Lifetime assigned to a cache entry when the token's own expiry does not determine it.
  private final Duration defaultExpireTime;

  private final boolean cacheTokens;
  private final boolean cacheNonExpiringTokens;

  // Caps a cache entry's lifetime to defaultExpireTime even when the token's own expiry is later.
  private final boolean forceCacheExpireTime;

  public TokenCache() {
    this(Duration.ofMinutes(5), true, false, false);
  }

  /**
   * Creates a cache configured with the given policy.
   *
   * @param defaultExpireTime lifetime assigned to a cache entry when the token's own expiry does
   *     not determine it
   * @param cacheTokens whether validated tokens are cached at all
   * @param cacheNonExpiringTokens whether a token with no expiry is cached
   * @param forceCacheExpireTime whether a cache entry's lifetime is capped to defaultExpireTime
   *     even when the token's own expiry is later
   */
  public TokenCache(
      Duration defaultExpireTime,
      boolean cacheTokens,
      boolean cacheNonExpiringTokens,
      boolean forceCacheExpireTime) {
    this.defaultExpireTime = defaultExpireTime;
    this.cacheTokens = cacheTokens;
    this.cacheNonExpiringTokens = cacheNonExpiringTokens;
    this.forceCacheExpireTime = forceCacheExpireTime;
  }

  /**
   * Returns the cached entry for the given token value, evicting it when it has expired.
   *
   * @param key the token value used as the cache key
   * @return the cached entry, or null when caching is turned off, nothing is cached for that value,
   *     or the cached entry has expired
   */
  public TokenCache.TokenCacheItem get(String key) {
    if (!this.cacheTokens) {
      return null;
    }
    TokenCacheItem item = this.cache.get(key);
    if (item == null) {
      return null;
    }
    if (item.cacheExpire.isAfter(Instant.now())) {
      return item;
    }
    this.cache.remove(key, item);
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
    private volatile OAuth2AccessToken token;
    private volatile AbstractAuthenticationToken auth;

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
      Instant cappedExpiry = Instant.now().plus(TokenCache.this.defaultExpireTime);
      Instant tokenExpiry = token.getExpiresAt();
      this.cacheExpire =
          tokenExpiry == null
                  || TokenCache.this.forceCacheExpireTime && tokenExpiry.isAfter(cappedExpiry)
              ? cappedExpiry
              : tokenExpiry;
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
