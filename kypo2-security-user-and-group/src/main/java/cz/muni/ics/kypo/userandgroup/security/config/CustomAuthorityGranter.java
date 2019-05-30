package cz.muni.ics.kypo.userandgroup.security.config;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.security.exception.SecurityException;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Component
@Transactional
public class CustomAuthorityGranter implements IntrospectionAuthorityGranter {

    private static Logger LOG = LoggerFactory.getLogger(CustomAuthorityGranter.class);

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;

    @Autowired
    public CustomAuthorityGranter(UserRepository userRepository, IDMGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
        LOG.info("getAuthorities({})", introspectionResponse);
        String login = introspectionResponse.get("sub").getAsString();
        Optional<User> optionalUser = userRepository.findByLogin(login);
        Set<Role> roles;
        if (!optionalUser.isPresent()) {
            roles = new HashSet<>(saveNewUser(login, introspectionResponse.get("name").getAsString(), introspectionResponse.get("email").getAsString()));
        } else {
            User user = optionalUser.get();
            if (user.getFullName() == null || user.getMail() == null) {
                user.setFullName(introspectionResponse.get("name").getAsString());
                user.setMail(introspectionResponse.get("email").getAsString());
                userRepository.save(user);
            }
            roles = userRepository.getRolesOfUser(user.getId());
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType()))
                .collect(Collectors.toList());
    }

    private Set<Role> saveNewUser(String login, String fullName, String email) {
        LOG.info("saveNewUser({},{},{})", login, fullName, email);
        IDMGroup defaultGroup = groupRepository.findByName("DEFAULT_GROUP").orElseThrow(() -> new SecurityException("Guest group could not be found"));
        User newUser = new User(login);
        newUser.setFullName(fullName);
        newUser.setMail(email);
        newUser.addGroup(defaultGroup);
        userRepository.save(newUser);
        return defaultGroup.getRoles();
    }
}
