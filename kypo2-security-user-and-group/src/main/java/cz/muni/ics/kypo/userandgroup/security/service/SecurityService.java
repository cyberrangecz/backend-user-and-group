package cz.muni.ics.kypo.userandgroup.security.service;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.model.IDMGroup;
import cz.muni.ics.kypo.userandgroup.model.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import cz.muni.ics.kypo.userandgroup.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.security.exception.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Dominik Pilar
 * @author Pavel Seda
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;

    @Autowired
    public SecurityService(UserRepository userRepository, IDMGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public boolean hasLoggedInUserSameLogin(String login) {
        return login.equals(getSubOfLoggedInUser());
    }

    public boolean hasLoggedInUserSameId(Long userId) {
        User loggedInUser = getLoggedInUser();
        return userId.equals(loggedInUser.getId());
    }

    public boolean isLoggedInUserInGroup(Long groupId) {
        User loggedInUser = getLoggedInUser();
        IDMGroup group = groupRepository.getOne(groupId);
        return group.getUsers().contains(loggedInUser);
    }

    public boolean isLoggedInUserInGroup(String groupName) {
        User loggedInUser = getLoggedInUser();
        Optional<IDMGroup> optionalGroup = groupRepository.findByName(groupName);
        IDMGroup group = optionalGroup.orElseThrow(() -> new SecurityException("Group with name " + groupName + " could not be found"));
        return group.getUsers().contains(loggedInUser);
    }

    private User getLoggedInUser() {
        String sub = getSubOfLoggedInUser();
        Optional<User> optionalUser = userRepository.findByLogin(sub);
        return optionalUser.orElseThrow(() -> new SecurityException("Logged in user with sub " + sub + " could not be found in database"));
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
    }
}
