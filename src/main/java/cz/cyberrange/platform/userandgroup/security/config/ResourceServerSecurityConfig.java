package cz.cyberrange.platform.userandgroup.security.config;

import cz.cyberrange.platform.userandgroup.security.AuthorityGranter;
import cz.cyberrange.platform.userandgroup.security.impl.CustomAuthenticationEntryPoint;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoAuthenticationProvider;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoValidator;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Configures authentication, CORS and endpoint access for this service as an OAuth2 resource
 * server.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerSecurityConfig {

  private final UserInfoValidator userInfoValidator;
  private final AuthorityGranter authorityGranter;

  // Each entry is one allowed origin from configuration, split on commas; defaults to allowing
  // every origin.
  @Value("#{'${cors.allowed.origins:#{*}}'.split(',')}")
  private List<String> corsAllowedOrigins;

  public ResourceServerSecurityConfig(
      AuthorityGranter authorityGranter, UserInfoValidator userInfoValidator) {
    this.authorityGranter = authorityGranter;
    this.userInfoValidator = userInfoValidator;
  }

  /**
   * Builds the HTTP security chain: stateless sessions, cross-origin requests allowed per the
   * configured CORS policy, CSRF protection disabled, {@code /webjars/**} and {@code
   * /microservices} open to every caller, and every other request accepted only with a bearer token
   * the user info provider authenticates. An authentication failure on a request is handled by the
   * given authentication entry point.
   *
   * @param http the HTTP security to configure
   * @param customAuthenticationEntryPoint the entry point answering failed authentication
   * @return the built security filter chain
   * @throws Exception when the security chain cannot be built
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
    return http.sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterBefore(
            new BearerTokenAuthenticationFilter(
                new ProviderManager(userInfoAuthenticationProvider())),
            UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers("/webjars/**", "/microservices")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint))
        .build();
  }

  /**
   * Builds the CORS filter applied to every request path, allowing only the configured origins and
   * a fixed set of headers and methods, and never permitting credentials.
   *
   * @return the configured CORS filter
   */
  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(false);
    config.setMaxAge(3600L);
    config.setExposedHeaders(List.of("authorization"));
    config.setAllowedOrigins(Collections.unmodifiableList(corsAllowedOrigins));
    config.setAllowedHeaders(List.of("content-type", "authorization", "x-auth-token"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  /**
   * Returns the authentication provider used to validate bearer tokens for this application.
   *
   * @return the bearer token authentication provider
   */
  @Bean
  public UserInfoAuthenticationProvider userInfoAuthenticationProvider() {
    return new UserInfoAuthenticationProvider(authorityGranter, userInfoValidator);
  }

  /**
   * Returns the entry point invoked when authentication fails for a request that requires it.
   *
   * @param handlerExceptionResolver the MVC exception resolvers the entry point offers each failure
   * @return the authentication entry point
   */
  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
    return new CustomAuthenticationEntryPoint(handlerExceptionResolver);
  }
}
