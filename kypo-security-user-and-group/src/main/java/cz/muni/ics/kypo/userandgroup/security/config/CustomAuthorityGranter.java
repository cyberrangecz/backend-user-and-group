package cz.muni.ics.kypo.userandgroup.security.config;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserDTO;
import cz.muni.ics.kypo.userandgroup.api.facade.UserFacade;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
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
public class CustomAuthorityGranter implements IntrospectionAuthorityGranter {

    private static Logger LOG = LoggerFactory.getLogger(CustomAuthorityGranter.class);

    private UserFacade userFacade;

    @Autowired
    public CustomAuthorityGranter(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Override
    public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
        LOG.debug("getAuthorities({})", introspectionResponse);
        String sub = introspectionResponse.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
        String issuer = introspectionResponse.get(AuthenticatedUserOIDCItems.ISS.getName()).getAsString();

        UserDTO userDTO = userFacade.createOrUpdateOrGetOIDCUser(sub, issuer, introspectionResponse);
        return userDTO.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
