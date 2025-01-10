package cz.cyberrange.platform.userandgroup.security.config;

import cz.cyberrange.platform.userandgroup.security.AuthorityGranter;
import cz.cyberrange.platform.userandgroup.security.impl.CustomAuthenticationEntryPoint;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoAuthenticationProvider;
import cz.cyberrange.platform.userandgroup.security.impl.UserInfoValidator;
import edu.emory.mathcs.backport.java.util.Collections;
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

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("#{'${cors.allowed.origins:#{*}}'.split(',')}")
    private List<String> corsAllowedOrigins;
    private final UserInfoValidator userInfoValidator;
    private final AuthorityGranter authorityGranter;

    @Autowired
    public ResourceServerSecurityConfig(AuthorityGranter authorityGranter,
                                        UserInfoValidator userInfoValidator) {
        this.authorityGranter = authorityGranter;
        this.userInfoValidator = userInfoValidator;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(userInfoAuthenticationProvider());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors()
                .and()
                .csrf().disable()
                .addFilterBefore(new BearerTokenAuthenticationFilter(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/webjars/**", "/microservices").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint());
    }

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

    @Bean
    public UserInfoAuthenticationProvider userInfoAuthenticationProvider() {
        return new UserInfoAuthenticationProvider(authorityGranter, userInfoValidator);
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }
}
