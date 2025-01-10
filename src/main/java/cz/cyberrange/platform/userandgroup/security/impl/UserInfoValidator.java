package cz.cyberrange.platform.userandgroup.security.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.cyberrange.platform.userandgroup.security.IdentityProvidersService;
import cz.cyberrange.platform.userandgroup.security.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserInfoValidator {

    private final IdentityProvidersService identityProvidersService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserInfoValidator(IdentityProvidersService identityProvidersService) {
        this.identityProvidersService = identityProvidersService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    }

    public UserInfo validate(String accessToken, String issuerUrl) {
        String userInfoUrl = this.identityProvidersService.getIdentityProviderConfiguration(issuerUrl).getUserInfoUri();

        HttpEntity<String> request = new HttpEntity<>(headersWithBearerAuthorization(accessToken));
        try {
            String userInfoSrc = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class).getBody();
            UserInfo userInfo = objectMapper.readValue(userInfoSrc, UserInfo.class);
            userInfo.setIssuer(issuerUrl);
            return userInfo;
        } catch (JsonProcessingException e) {
            throw new InternalAuthenticationServiceException("Unable to parse user info response.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthenticationServiceException("Invalid access token: " + accessToken);
            }
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

    private HttpHeaders headersWithBearerAuthorization(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", bearerToken));
        return headers;
    }


}
