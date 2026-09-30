package cz.cyberrange.platform.userandgroup.security.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Authentication entry point invoked when a request that requires authentication fails to
 * authenticate.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Autowired private HandlerExceptionResolver handlerExceptionResolver;

  /**
   * Routes an authentication failure through this application's centralized exception handling, the
   * same handling used for exceptions raised in controllers.
   *
   * @param request the request that failed authentication
   * @param response the response to write the resulting error to
   * @param authException the authentication failure that occurred
   */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    handlerExceptionResolver.resolveException(request, response, null, authException);
  }
}
