package cz.cyberrange.platform.userandgroup.security.impl;

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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Confirms a bearer token by presenting it to the issuing identity provider's user info endpoint
 * and parsing the identity it returns.
 */
@Component
public class UserInfoValidator {

  private final IdentityProvidersService identityProvidersService;
  private final RestTemplate restTemplate;
  private final JsonMapper jsonMapper;

  @Autowired
  public UserInfoValidator(IdentityProvidersService identityProvidersService) {
    this.identityProvidersService = identityProvidersService;
    this.restTemplate = new RestTemplate();
    this.jsonMapper =
        JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
  }

  /**
   * Confirms the given bearer token by fetching the identity it belongs to from the issuing
   * provider's user info endpoint. The returned identity's issuer is always set to the given issuer
   * URL, regardless of anything the endpoint returns.
   *
   * @param accessToken the bearer token to validate
   * @param issuerUrl issuer of the token, identifying which provider to query
   * @return the identity resolved for the token
   * @throws InternalAuthenticationServiceException when the user info response cannot be parsed
   * @throws AuthenticationServiceException when the token is rejected by the identity provider, or
   *     the provider's user info endpoint request otherwise fails
   */
  public UserInfo validate(String accessToken, String issuerUrl) {
    String userInfoUrl =
        this.identityProvidersService.getIdentityProviderConfiguration(issuerUrl).getUserInfoUri();

    HttpEntity<String> request = new HttpEntity<>(headersWithBearerAuthorization(accessToken));
    try {
      String userInfoSrc =
          restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class).getBody();
      UserInfo userInfo = jsonMapper.readValue(userInfoSrc, UserInfo.class);
      userInfo.setIssuer(issuerUrl);
      return userInfo;
    } catch (JacksonException e) {
      throw new InternalAuthenticationServiceException(
          "Unable to parse user info response from issuer " + issuerUrl, e);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        throw new AuthenticationServiceException(
            "Access token rejected by issuer " + issuerUrl + " with status " + e.getStatusCode());
      }
      throw new AuthenticationServiceException(
          "User info request to issuer " + issuerUrl + " failed with status " + e.getStatusCode());
    }
  }

  private HttpHeaders headersWithBearerAuthorization(String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", String.format("Bearer %s", bearerToken));
    return headers;
  }
}
