package cz.muni.ics.kypo.userandgroup.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public interface AuthorityGranter {
    List<GrantedAuthority> getAuthorities(Object userInfo);
}
