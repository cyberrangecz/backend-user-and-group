package cz.muni.ics.kypo.userandgroup.service.impl;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.userandgroup.entities.IDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.User;
import cz.muni.ics.kypo.userandgroup.repository.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * SecurityService class provides methods for obtaining information about logged in user.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {

    private UserRepository userRepository;
    private IDMGroupRepository groupRepository;

    /**
     * Instantiates a new Security service.
     *
     * @param userRepository  the user repository
     * @param groupRepository the group repository
     */
    @Autowired
    public SecurityService(UserRepository userRepository, IDMGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    private User getLoggedInUser() {
        String sub = getSubOfLoggedInUser();
        String iss = getIssOfLoggedInUser();
        Optional<User> optionalUser = userRepository.findByLoginAndIss(sub, iss);
        return optionalUser.orElseThrow(() -> new SecurityException("Logged in user with sub " + sub + " could not be found in database."));
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
    }

    private String getIssOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.ISS.getName()).getAsString();
    }

    /**
     * Check if logged in {@link User} has same login as given login.
     *
     * @param login the login of the user
     * @return true if login of logged in user is same as given login, false otherwise.w
     */
    public boolean hasLoggedInUserSameLogin(String login) {
        return login.equals(getSubOfLoggedInUser());
    }

    /**
     * Check if logged in {@link User} has same ID as given ID.
     *
     * @param userId ID of the user
     * @return true if ID of logged in user is same as given ID, false otherwise.
     */
    public boolean hasLoggedInUserSameId(Long userId) {
        User loggedInUser = getLoggedInUser();
        return userId.equals(loggedInUser.getId());
    }

    /**
     * Check if logged in {@link User} is in {@link IDMGroup} with given group ID.
     *
     * @param groupId ID of the IDMGroup
     * @return true if logged in user is in group with given group ID, false otherwise.
     */
    public boolean isLoggedInUserInGroup(Long groupId) {
        User loggedInUser = getLoggedInUser();
        IDMGroup group = groupRepository.getOne(groupId);
        return group.getUsers().contains(loggedInUser);
    }

    /**
     * Check if logged in {@link User} is in {@link IDMGroup} with given group name.
     *
     * @param groupName name of the IDMGroup
     * @return true if logged in user is in group with given group name, false otherwise.
     */
    public boolean isLoggedInUserInGroup(String groupName) {
        User loggedInUser = getLoggedInUser();
        Optional<IDMGroup> optionalGroup = groupRepository.findByName(groupName);
        IDMGroup group = optionalGroup.orElseThrow(() -> new SecurityException("Group with name " + groupName + " could not be found."));
        return group.getUsers().contains(loggedInUser);
    }

}
