package cz.cyberrange.platform.userandgroup.security.impl;

import static org.assertj.core.api.Assertions.assertThat;

import cz.cyberrange.platform.userandgroup.security.impl.TokenCache.TokenCacheItem;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

class TokenCacheTest {

  private static final Duration SHORT_LIFETIME = Duration.ofMillis(50);
  private static final long PAST_SHORT_LIFETIME_MILLIS = 200;

  private final AbstractAuthenticationToken authentication =
      new TestingAuthenticationToken("user", "credentials");

  private static OAuth2AccessToken tokenExpiringIn(String value, Duration lifetime) {
    Instant now = Instant.now();
    return new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER, value, now.minusSeconds(1), now.plus(lifetime));
  }

  private static OAuth2AccessToken tokenWithoutExpiry(String value) {
    return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, value, null, null);
  }

  @Test
  @DisplayName("get_unexpiredEntry_returnsCachedItem")
  void get_unexpiredEntry_returnsCachedItem() {
    TokenCache tokenCache = new TokenCache();
    OAuth2AccessToken token = tokenExpiringIn("token-a", Duration.ofHours(1));
    TokenCacheItem stored = tokenCache.put(token, authentication);

    assertThat(tokenCache.get("token-a")).isSameAs(stored);
    assertThat(stored.getToken()).isSameAs(token);
    assertThat(stored.getAuth()).isSameAs(authentication);
  }

  @Test
  @DisplayName("get_unknownKey_returnsNull")
  void get_unknownKey_returnsNull() {
    assertThat(new TokenCache().get("missing")).isNull();
  }

  @Test
  @DisplayName("get_cachingDisabled_returnsNull")
  void get_cachingDisabled_returnsNull() {
    TokenCache tokenCache = new TokenCache(Duration.ofMinutes(5), false, false, false);
    tokenCache.put(tokenExpiringIn("token-a", Duration.ofHours(1)), authentication);

    assertThat(tokenCache.get("token-a")).isNull();
  }

  @Test
  @DisplayName("put_alreadyExpiredToken_returnsNullAndStoresNothing")
  void put_alreadyExpiredToken_returnsNullAndStoresNothing() {
    TokenCache tokenCache = new TokenCache();
    Instant now = Instant.now();
    OAuth2AccessToken expired =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-a",
            now.minusSeconds(60),
            now.minusSeconds(30));

    assertThat(tokenCache.put(expired, authentication)).isNull();
    assertThat(tokenCache.get("token-a")).isNull();
  }

  @Test
  @DisplayName("put_tokenWithoutExpiryAndDefaultPolicy_returnsItemWithoutStoringIt")
  void put_tokenWithoutExpiryAndDefaultPolicy_returnsItemWithoutStoringIt() {
    TokenCache tokenCache = new TokenCache();

    TokenCacheItem item = tokenCache.put(tokenWithoutExpiry("token-a"), authentication);

    assertThat(item).isNotNull();
    assertThat(tokenCache.get("token-a")).isNull();
  }

  @Test
  @DisplayName("put_tokenWithoutExpiryAndNonExpiringCachingOn_storesItem")
  void put_tokenWithoutExpiryAndNonExpiringCachingOn_storesItem() {
    TokenCache tokenCache = new TokenCache(Duration.ofMinutes(5), true, true, false);

    TokenCacheItem item = tokenCache.put(tokenWithoutExpiry("token-a"), authentication);

    assertThat(tokenCache.get("token-a")).isSameAs(item);
  }

  @Test
  @DisplayName("get_entryPastDefaultExpireTime_returnsNullAndEvictsEntry")
  void get_entryPastDefaultExpireTime_returnsNullAndEvictsEntry() throws InterruptedException {
    TokenCache tokenCache = new TokenCache(SHORT_LIFETIME, true, true, false);
    tokenCache.put(tokenWithoutExpiry("token-a"), authentication);

    Thread.sleep(PAST_SHORT_LIFETIME_MILLIS);

    assertThat(tokenCache.get("token-a")).isNull();
  }

  @Test
  @DisplayName("get_tokenExpiresBeforeDefaultExpireTime_returnsNullOnceTokenExpired")
  void get_tokenExpiresBeforeDefaultExpireTime_returnsNullOnceTokenExpired()
      throws InterruptedException {
    TokenCache tokenCache = new TokenCache();
    tokenCache.put(tokenExpiringIn("token-a", SHORT_LIFETIME), authentication);

    Thread.sleep(PAST_SHORT_LIFETIME_MILLIS);

    assertThat(tokenCache.get("token-a")).isNull();
  }

  @Test
  @DisplayName("get_forcedExpireTimeAndLongerLivedToken_returnsNullAfterDefaultExpireTime")
  void get_forcedExpireTimeAndLongerLivedToken_returnsNullAfterDefaultExpireTime()
      throws InterruptedException {
    TokenCache tokenCache = new TokenCache(SHORT_LIFETIME, true, false, true);
    tokenCache.put(tokenExpiringIn("token-a", Duration.ofHours(1)), authentication);

    Thread.sleep(PAST_SHORT_LIFETIME_MILLIS);

    assertThat(tokenCache.get("token-a")).isNull();
  }

  @Test
  @DisplayName("get_unforcedExpireTimeAndLongerLivedToken_returnsItemAfterDefaultExpireTime")
  void get_unforcedExpireTimeAndLongerLivedToken_returnsItemAfterDefaultExpireTime()
      throws InterruptedException {
    TokenCache tokenCache = new TokenCache(SHORT_LIFETIME, true, false, false);
    TokenCacheItem item =
        tokenCache.put(tokenExpiringIn("token-a", Duration.ofHours(1)), authentication);

    Thread.sleep(PAST_SHORT_LIFETIME_MILLIS);

    assertThat(tokenCache.get("token-a")).isSameAs(item);
  }

  @Test
  @DisplayName("putAndGet_concurrentAccessToDistinctKeys_everyEntryStoredAndReadable")
  void putAndGet_concurrentAccessToDistinctKeys_everyEntryStoredAndReadable() throws Exception {
    TokenCache tokenCache = new TokenCache();
    int threadCount = 16;
    int keysPerThread = 200;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    try {
      List<Callable<Integer>> workers = new ArrayList<>();
      for (int thread = 0; thread < threadCount; thread++) {
        int threadIndex = thread;
        workers.add(
            () -> {
              int readable = 0;
              for (int key = 0; key < keysPerThread; key++) {
                String value = "token-" + threadIndex + "-" + key;
                TokenCacheItem stored =
                    tokenCache.put(tokenExpiringIn(value, Duration.ofHours(1)), authentication);
                if (tokenCache.get(value) == stored) {
                  readable++;
                }
              }
              return readable;
            });
      }

      int totalReadable = 0;
      for (Future<Integer> result : executor.invokeAll(workers)) {
        totalReadable += result.get();
      }

      assertThat(totalReadable).isEqualTo(threadCount * keysPerThread);
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  @DisplayName("putAndGet_concurrentAccessToSameKey_neverReturnsForeignEntry")
  void putAndGet_concurrentAccessToSameKey_neverReturnsForeignEntry() throws Exception {
    TokenCache tokenCache = new TokenCache();
    int threadCount = 16;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    try {
      List<Callable<Boolean>> workers = new ArrayList<>();
      for (int thread = 0; thread < threadCount; thread++) {
        workers.add(
            () -> {
              boolean consistent = true;
              for (int round = 0; round < 500; round++) {
                tokenCache.put(tokenExpiringIn("shared", Duration.ofHours(1)), authentication);
                TokenCacheItem item = tokenCache.get("shared");
                consistent &= item == null || item.getAuth() == authentication;
              }
              return consistent;
            });
      }

      for (Future<Boolean> result : executor.invokeAll(workers)) {
        assertThat(result.get()).isTrue();
      }
    } finally {
      executor.shutdownNow();
    }
  }
}
