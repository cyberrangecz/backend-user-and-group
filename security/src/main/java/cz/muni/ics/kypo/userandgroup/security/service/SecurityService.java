package cz.muni.ics.kypo.userandgroup.security.service;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import cz.muni.ics.kypo.userandgroup.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private UserRepository userRepository;

    @Autowired
    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasLoggedInUserSameScreenName(String screenName) {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();

        return screenName.equals(credentials.get("sub").getAsString());
    }

    public boolean hasLoggedInUserSameId(Long id) {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        User loggedInUser = userRepository.findByScreenName(credentials.get("sub").getAsString());

        return id.equals(loggedInUser.getId());
    }
}
