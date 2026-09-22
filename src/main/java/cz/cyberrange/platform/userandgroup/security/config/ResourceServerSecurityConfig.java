package cz.cyberrange.platform.userandgroup.security.config;

import cz.cyberrange.platform.userandgroup.security.AuthorityGranter;
import cz.cyberrange.platform.userandgroup.security.impl.CustomAuthenticationEntryPoint;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoAuthenticationProvider;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoValidator;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configures authentication, CORS and endpoint access for this service as an OAuth2 resource
 * server.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerSecurityConfig extends WebSecurityConfigurerAdapter {

  private final UserInfoValidator userInfoValidator;
  private final AuthorityGranter authorityGranter;

  // Each entry is one allowed origin from configuration, split on commas; defaults to allowing
  // every origin.
  @Value("#{'${cors.allowed.origins:#{*}}'.split(',')}")
  private List<String> corsAllowedOrigins;

  @Autowired
  public ResourceServerSecurityConfig(
      AuthorityGranter authorityGranter, UserInfoValidator userInfoValidator) {
    this.authorityGranter = authorityGranter;
    this.userInfoValidator = userInfoValidator;
  }

  /**
   * Registers the bearer token authentication provider as the only authentication mechanism for
   * this application.
   *
   * @param auth the authentication manager builder to configure
   */
  @Override
  public void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(userInfoAuthenticationProvider());
  }

  /**
   * Builds the HTTP security chain: stateless sessions, cross-origin requests allowed per the
   * configured CORS policy, CSRF protection disabled, {@code /webjars/**} and {@code
   * /microservices} open to every caller, and every other request accepted only with a valid bearer
   * token. An authentication failure on a request is handled by this configuration's authentication
   * entry point.
   *
   * @param http the HTTP security to configure
   * @throws Exception when the security chain cannot be built
   */
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .cors()
        .and()
        .csrf()
        .disable()
        .addFilterBefore(
            new BearerTokenAuthenticationFilter(authenticationManagerBean()),
            UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .antMatchers("/webjars/**", "/microservices")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(customAuthenticationEntryPoint());
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
   * @return the authentication entry point
   */
  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    return new CustomAuthenticationEntryPoint();
  }
}
