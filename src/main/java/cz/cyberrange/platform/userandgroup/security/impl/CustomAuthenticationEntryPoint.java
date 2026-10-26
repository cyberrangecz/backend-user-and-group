package cz.cyberrange.platform.userandgroup.security.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Routes authentication failures through the MVC exception resolvers, so controller advice shapes
 * the response. Sends 401 Unauthorized when no resolver handles the failure.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver handlerExceptionResolver;

  public CustomAuthenticationEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    if (isResolved(request, response, authException)) {
      return;
    }
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }

  /**
   * Offers the failure to the MVC exception resolvers.
   *
   * @return whether a resolver produced the response; false when none handled it or one rethrew an
   *     access-denied cause
   */
  private boolean isResolved(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    try {
      return handlerExceptionResolver.resolveException(request, response, null, authException)
          != null;
    } catch (AccessDeniedException rethrownCause) {
      return false;
    }
  }
}
