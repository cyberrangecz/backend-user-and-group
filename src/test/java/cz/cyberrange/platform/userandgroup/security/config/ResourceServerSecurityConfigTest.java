package cz.cyberrange.platform.userandgroup.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cz.cyberrange.platform.userandgroup.security.AuthorityGranter;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoValidator;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CorsFilter;

class ResourceServerSecurityConfigTest {

  private static final String ALLOWED_ORIGIN = "https://platform.example";

  private static CorsFilter corsFilterAllowing(String origin) {
    ResourceServerSecurityConfig config =
        new ResourceServerSecurityConfig(
            mock(AuthorityGranter.class), mock(UserInfoValidator.class));
    ReflectionTestUtils.setField(config, "corsAllowedOrigins", List.of(origin));
    return config.corsFilter();
  }

  @Test
  @DisplayName("corsFilter_crossOriginRequest_exposesContentDispositionAndAuthorization")
  void corsFilter_crossOriginRequest_exposesContentDispositionAndAuthorization()
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/initial-oidc-users");
    request.addHeader("Origin", ALLOWED_ORIGIN);
    MockHttpServletResponse response = new MockHttpServletResponse();

    corsFilterAllowing(ALLOWED_ORIGIN).doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader("Access-Control-Expose-Headers"))
        .containsIgnoringCase("content-disposition")
        .containsIgnoringCase("authorization");
  }
}
