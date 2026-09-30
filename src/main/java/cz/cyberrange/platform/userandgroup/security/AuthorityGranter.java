package cz.cyberrange.platform.userandgroup.security;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;

/** Resolves the Spring Security authorities to grant for an authenticated identity. */
public interface AuthorityGranter {

  /**
   * Returns the authorities to grant for the given user identity.
   *
   * @param userInfo identity information for the authenticated user
   * @return the granted authorities
   */
  List<GrantedAuthority> getAuthorities(Object userInfo);
}
