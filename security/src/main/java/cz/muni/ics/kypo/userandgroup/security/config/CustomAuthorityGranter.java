package cz.muni.ics.kypo.userandgroup.security.config;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.security.exception.SecurityException;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomAuthorityGranter implements IntrospectionAuthorityGranter {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomAuthorityGranter.class);

    private UserRepository userRepository;

    @Autowired
    public CustomAuthorityGranter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
        String screenName = introspectionResponse.get("sub").getAsString();
        Optional<User> optionalUser = userRepository.findByScreenName(screenName);
        User user = optionalUser.orElseThrow(() -> new SecurityException("Logged in user with sub " + screenName + " could not be found in database"));
        Set<Role> roles = userRepository.getRolesOfUser(user.getId());

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType()))
                .collect(Collectors.toList());
    }
}
