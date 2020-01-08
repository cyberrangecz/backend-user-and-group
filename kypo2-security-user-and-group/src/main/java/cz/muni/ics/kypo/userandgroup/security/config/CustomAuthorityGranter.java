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
import cz.muni.ics.kypo.userandgroup.security.service.IdenticonService;
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
 * Class is annotated with {@link Component}, so its mark as candidates for auto-detection when using annotation-based configuration and classpath scanning.
 * This class is responsible for returning a set of Spring Security GrantedAuthority objects to be assigned to the token service's resulting <i>Authentication</i> object.
 *
 */
@Component
@Transactional
public class CustomAuthorityGranter implements IntrospectionAuthorityGranter {

    private static final int ICON_WIDTH = 75;
    private static final int ICON_HEIGHT = 75;

    private static Logger LOG = LoggerFactory.getLogger(CustomAuthorityGranter.class);

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private IdenticonService identiconService;

    @Autowired
    public CustomAuthorityGranter(UserRepository userRepository, IDMGroupRepository groupRepository, IdenticonService identiconService) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.identiconService = identiconService;
    }

    @Override
    public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
        LOG.debug("getAuthorities({})", introspectionResponse);
        String login = introspectionResponse.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
        String issuer = introspectionResponse.get(AuthenticatedUserOIDCItems.ISS.getName()).getAsString();
        Optional<User> optionalUser = userRepository.findByLoginAndIss(login, issuer);
        Set<Role> roles = new HashSet<>();
        if (!optionalUser.isPresent()) {
            roles.addAll(
                    saveNewUser(login, introspectionResponse.get(AuthenticatedUserOIDCItems.NAME.getName()) == null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.NAME.getName()).getAsString(),
                            introspectionResponse.get(AuthenticatedUserOIDCItems.EMAIL.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.EMAIL.getName()).getAsString(),
                            introspectionResponse.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName()).getAsString(),
                            introspectionResponse.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName()).getAsString(),
                            issuer
                    ));
        } else {
            User user = optionalUser.get();
            if (user.getFullName() == null || user.getMail() == null || user.getPicture() == null) {
                user.setFullName(introspectionResponse.get(AuthenticatedUserOIDCItems.NAME.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.NAME.getName()).getAsString());
                user.setMail(introspectionResponse.get(AuthenticatedUserOIDCItems.EMAIL.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.EMAIL.getName()).getAsString());
                user.setGivenName(introspectionResponse.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName()).getAsString());
                user.setFamilyName(introspectionResponse.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName())== null ? null : introspectionResponse.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName()).getAsString());
                user.setPicture(identiconService.generateIdenticons(login + issuer, ICON_WIDTH, ICON_HEIGHT));
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

    private Set<Role> saveNewUser(String login, String fullName, String email, String givenName, String familyName, String issuer) {
        LOG.info("saveNewUser({},{},{},{},{})", login, fullName, email, givenName, familyName);
        IDMGroup defaultGroup = groupRepository.findByName(ImplicitGroupNames.DEFAULT_GROUP.getName()).orElseThrow(() -> new SecurityException("Guest group could not be found"));
        User newUser = new User(login, issuer);
        newUser.setFullName(fullName);
        newUser.setMail(email);
        newUser.setGivenName(givenName);
        newUser.setFamilyName(familyName);
        newUser.setPicture(identiconService.generateIdenticons(login + issuer, ICON_WIDTH, ICON_HEIGHT));
        userRepository.save(newUser);
        defaultGroup.addUser(newUser);
        return defaultGroup.getRoles();
    }

}
