package cz.cyberrange.platform.userandgroup.security.impl;

import cz.cyberrange.platform.userandgroup.api.dto.user.UserCreateDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import cz.cyberrange.platform.userandgroup.security.AuthorityGranter;
import cz.cyberrange.platform.userandgroup.security.model.UserInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Resolves the Spring Security authorities for an OIDC-authenticated user, matching or creating
 * that user first.
 */
@Component
public class InternalAuthorityGranter implements AuthorityGranter {

  private static final Logger LOG = LoggerFactory.getLogger(InternalAuthorityGranter.class);

  private final UserFacade userFacade;

  @Autowired
  public InternalAuthorityGranter(UserFacade userFacade) {
    this.userFacade = userFacade;
  }

  /**
   * Resolves the Spring Security authorities for an OIDC-authenticated user, matching or creating
   * that user first. A user already known by subject and issuer is updated to match the identity
   * information; a user seen for the first time is created and placed in the default group.
   *
   * @param userInfoObject the authenticated user's OIDC identity information; must be a {@link
   *     UserInfo}
   * @return the authorities for that user's roles
   * @throws ClassCastException when userInfoObject is not a {@link UserInfo} instance
   * @throws UnprocessableEntityException when the identity information lacks a given, family or
   *     full name
   */
  @Override
  public List<GrantedAuthority> getAuthorities(Object userInfoObject) {
    UserInfo userInfo = (UserInfo) userInfoObject;
    UserCreateDTO oidcUserDTO = new UserCreateDTO();
    oidcUserDTO.setSub(userInfo.getSub());
    oidcUserDTO.setIss(userInfo.getIssuer());
    oidcUserDTO.setGivenName(userInfo.getGivenName());
    oidcUserDTO.setFamilyName(userInfo.getFamilyName());
    oidcUserDTO.setFullName(userInfo.getName());
    oidcUserDTO.setMail(userInfo.getEmail());
    UserDTO userDTO = userFacade.createOrUpdateOrGetOIDCUser(oidcUserDTO);
    return userDTO.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getRoleType()))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
