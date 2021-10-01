package cz.muni.ics.kypo.userandgroup.security.model;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OAuth2AccessTokenImpl implements OAuth2AccessToken {
    private final UserInfo userInfo;
    private final String tokenString;
    private final Date expireDate;

    public OAuth2AccessTokenImpl(UserInfo userInfo, String tokenString, Date tokenExpiration) {
        this.userInfo = userInfo;
        this.tokenString = tokenString;
        this.expireDate = tokenExpiration;

    }

    public Map<String, Object> getAdditionalInformation() {
        return null;
    }

    public OAuth2RefreshToken getRefreshToken() {
        return null;
    }

    public String getTokenType() {
        return "Bearer";
    }

    public boolean isExpired() {
        return this.expireDate != null && this.expireDate.before(new Date());
    }

    public Date getExpiration() {
        return this.expireDate;
    }

    public int getExpiresIn() {
        return this.expireDate != null ? (int) TimeUnit.MILLISECONDS.toSeconds(this.expireDate.getTime() - (new Date()).getTime()) : 0;
    }

    public String getValue() {
        return this.tokenString;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public Set<String> getScope() {
        return null;
    }
}
