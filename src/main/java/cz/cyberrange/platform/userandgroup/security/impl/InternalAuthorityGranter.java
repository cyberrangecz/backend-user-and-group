package cz.cyberrange.platform.userandgroup.security.impl;

import cz.cyberrange.platform.userandgroup.api.dto.user.UserCreateDTO;
import cz.cyberrange.platform.userandgroup.api.dto.user.UserDTO;
import cz.cyberrange.platform.userandgroup.rest.facade.UserFacade;
import cz.cyberrange.platform.userandgroup.security.AuthorityGranter;
import cz.cyberrange.platform.userandgroup.security.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class is annotated with {@link Component}, so its mark as candidates for auto-detection when using annotation-based configuration and classpath scanning.
 * This class is responsible for returning a set of Spring Security GrantedAuthority objects to be assigned to the token service's resulting <i>Authentication</i> object.
 */
@Component
public class InternalAuthorityGranter implements AuthorityGranter {

    private static final Logger LOG = LoggerFactory.getLogger(InternalAuthorityGranter.class);

    private final UserFacade userFacade;

    @Autowired
    public InternalAuthorityGranter(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

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
