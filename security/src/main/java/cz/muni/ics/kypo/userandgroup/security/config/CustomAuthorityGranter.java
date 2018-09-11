package cz.muni.ics.kypo.userandgroup.security.config;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.Role;
import cz.muni.ics.kypo.userandgroup.model.RoleType;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.RoleRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.security.exception.SecurityException;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomAuthorityGranter implements IntrospectionAuthorityGranter {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomAuthorityGranter.class);

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;
    private RoleRepository roleRepository;

    @Autowired
    public CustomAuthorityGranter(UserRepository userRepository, IDMGroupRepository groupRepository,
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<GrantedAuthority> getAuthorities(JsonObject introspectionResponse) {
        String login = introspectionResponse.get("sub").getAsString();
        Optional<User> optionalUser = userRepository.findByLogin(login);
        Set<Role> roles = new HashSet<>();
        if (!optionalUser.isPresent()) {
            saveNewUser(login, introspectionResponse.get("name").getAsString(), introspectionResponse.get("email").getAsString());
            roles.add(roleRepository.findByRoleType(RoleType.GUEST).orElseThrow(() -> new SecurityException("Guest role could not be found")));
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
                .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                .collect(Collectors.toList());
    }

    private User saveNewUser(String login, String fullName, String email) {
        IDMGroup guestGroup = groupRepository.findByName(RoleType.GUEST.name()).orElseThrow(() -> new SecurityException("Guest group could not be found"));
        User newUser = new User(login);
        newUser.setFullName(fullName);
        newUser.setMail(email);
        newUser.addGroup(guestGroup);
        return userRepository.save(newUser);
    }
}
