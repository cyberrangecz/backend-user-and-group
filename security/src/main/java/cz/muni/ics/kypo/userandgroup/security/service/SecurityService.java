package cz.muni.ics.kypo.userandgroup.security.service;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.dbmodel.IDMGroup;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import cz.muni.ics.kypo.userandgroup.persistence.IDMGroupRepository;
import cz.muni.ics.kypo.userandgroup.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private UserRepository userRepository;

    private IDMGroupRepository groupRepository;

    @Autowired
    public SecurityService(UserRepository userRepository, IDMGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public boolean hasLoggedInUserSameScreenName(String screenName) {
        return screenName.equals(getSubOfLoggedInUser());
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
        IDMGroup group = groupRepository.findByName(groupName);

        return group.getUsers().contains(loggedInUser);
    }

    private User getLoggedInUser() {
        return userRepository.findByScreenName(getSubOfLoggedInUser());
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("sub").getAsString();
    }
}
