package cz.muni.ics.kypo.userandgroup.security.service;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.userandgroup.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public boolean hasLoggedInUserSameScreenName(String screenName, OAuth2Authentication authentication) {
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();

        return screenName.equals(credentials.get("sub").getAsString());
    }
}
