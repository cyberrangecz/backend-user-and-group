package cz.cyberrange.platform.userandgroup.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cz.cyberrange.platform.userandgroup.security.IdentityProvidersService;
import cz.cyberrange.platform.userandgroup.security.model.UserInfo;
import cz.cyberrange.platform.userandgroup.security.model.WellKnownOpenIDConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.JacksonException;

class UserInfoValidatorTest {

  private static final String ISSUER = "https://issuer.example/realm";
  private static final String USER_INFO_URL = "https://issuer.example/realm/userinfo";
  private static final String SECRET_TOKEN = "secret-token-value-123";

  private UserInfoValidator validator;
  private MockRestServiceServer server;

  @BeforeEach
  void setUp() {
    WellKnownOpenIDConfiguration configuration = new WellKnownOpenIDConfiguration();
    configuration.setUserInfoUri(USER_INFO_URL);
    IdentityProvidersService identityProvidersService = mock(IdentityProvidersService.class);
    when(identityProvidersService.getIdentityProviderConfiguration(ISSUER))
        .thenReturn(configuration);

    validator = new UserInfoValidator(identityProvidersService);
    RestTemplate restTemplate =
        (RestTemplate) ReflectionTestUtils.getField(validator, "restTemplate");
    server = MockRestServiceServer.bindTo(restTemplate).build();
  }

  private static void assertNoTokenInChain(Throwable thrown) {
    for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
      assertThat(String.valueOf(cause.getMessage())).doesNotContain(SECRET_TOKEN);
    }
  }

  @Test
  @DisplayName("validate_providerAnswersWithIdentity_returnsUserInfoWithIssuerSet")
  void validate_providerAnswersWithIdentity_returnsUserInfoWithIssuerSet() {
    server
        .expect(requestTo(USER_INFO_URL))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer " + SECRET_TOKEN))
        .andRespond(
            withSuccess(
                "{\"sub\":\"subject-1\",\"preferred_username\":\"alice\",\"issuer\":\"ignored\"}",
                MediaType.APPLICATION_JSON));

    UserInfo userInfo = validator.validate(SECRET_TOKEN, ISSUER);

    assertThat(userInfo.getSub()).isEqualTo("subject-1");
    assertThat(userInfo.getPreferredUsername()).isEqualTo("alice");
    assertThat(userInfo.getIssuer()).isEqualTo(ISSUER);
    server.verify();
  }

  @Test
  @DisplayName("validate_providerRejectsToken_throwsAuthenticationServiceWithoutTokenText")
  void validate_providerRejectsToken_throwsAuthenticationServiceWithoutTokenText() {
    server.expect(requestTo(USER_INFO_URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(() -> validator.validate(SECRET_TOKEN, ISSUER))
        .isInstanceOf(AuthenticationServiceException.class)
        .hasMessageContaining(ISSUER)
        .hasMessageContaining("401")
        .satisfies(UserInfoValidatorTest::assertNoTokenInChain);
  }

  @Test
  @DisplayName("validate_providerAnswersClientError_throwsAuthenticationServiceWithStatus")
  void validate_providerAnswersClientError_throwsAuthenticationServiceWithStatus() {
    server.expect(requestTo(USER_INFO_URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

    assertThatThrownBy(() -> validator.validate(SECRET_TOKEN, ISSUER))
        .isInstanceOf(AuthenticationServiceException.class)
        .hasMessageContaining(ISSUER)
        .hasMessageContaining("403")
        .satisfies(UserInfoValidatorTest::assertNoTokenInChain);
  }

  @Test
  @DisplayName("validate_providerAnswersUnparsableBody_throwsInternalServiceExceptionKeepingCause")
  void validate_providerAnswersUnparsableBody_throwsInternalServiceExceptionKeepingCause() {
    server
        .expect(requestTo(USER_INFO_URL))
        .andRespond(withSuccess("not json at all", MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> validator.validate(SECRET_TOKEN, ISSUER))
        .isInstanceOf(InternalAuthenticationServiceException.class)
        .hasMessageContaining(ISSUER)
        .hasCauseInstanceOf(JacksonException.class)
        .satisfies(UserInfoValidatorTest::assertNoTokenInChain);
  }
}
