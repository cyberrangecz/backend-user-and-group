package cz.muni.ics.kypo.userandgroup.security.config;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.security.enums.ImplicitGroupNames;
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
        String login = introspectionResponse.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
        Optional<User> optionalUser = userRepository.findByLogin(login);
        Set<Role> roles = new HashSet<>();
        if (!optionalUser.isPresent()) {
            roles.addAll(
                    saveNewUser(login, introspectionResponse.get(AuthenticatedUserOIDCItems.NAME.getName()).getAsString(),
                            introspectionResponse.get(AuthenticatedUserOIDCItems.EMAIL.getName()).getAsString(),
                            introspectionResponse.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName()).getAsString(),
                            introspectionResponse.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName()).getAsString()
                    ));
        } else {
            User user = optionalUser.get();
            if (user.getFullName() == null || user.getMail() == null) {
                user.setFullName(introspectionResponse.get(AuthenticatedUserOIDCItems.NAME.getName()).getAsString());
                user.setMail(introspectionResponse.get(AuthenticatedUserOIDCItems.EMAIL.getName()).getAsString());
                user.setGivenName(introspectionResponse.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName()).getAsString());
                user.setFamilyName(introspectionResponse.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName()).getAsString());
                userRepository.save(user);

            }
            for (IDMGroup groupOfUser : user.getGroups()) {
                roles.addAll(groupOfUser.getRoles());
            }
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType()))
                .collect(Collectors.toList());
    }

    private Set<Role> saveNewUser(String login, String fullName, String email, String givenName, String familyName) {
        LOG.info("saveNewUser({},{},{},{},{})", login, fullName, email, givenName, familyName);
        IDMGroup defaultGroup = groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).orElseThrow(() -> new SecurityException("Guest group could not be found"));
        User newUser = new User(login);
        newUser.setFullName(fullName);
        newUser.setMail(email);
        newUser.setGivenName(givenName);
        newUser.setFamilyName(familyName);
        userRepository.save(newUser);
        defaultGroup.addUser(newUser);
        return defaultGroup.getRoles();
    }
}
