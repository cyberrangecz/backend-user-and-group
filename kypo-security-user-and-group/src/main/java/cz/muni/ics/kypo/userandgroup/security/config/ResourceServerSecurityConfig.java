package cz.muni.ics.kypo.userandgroup.security.config;

import cz.muni.ics.kypo.userandgroup.config.FacadeConfig;
import cz.muni.ics.kypo.userandgroup.security.AuthorityGranter;
import cz.muni.ics.kypo.userandgroup.security.impl.CustomAuthenticationEntryPoint;
import cz.muni.ics.kypo.userandgroup.security.impl.CustomCorsFilter;
import cz.muni.ics.kypo.userandgroup.security.impl.DynamicServerConfigurationService;
import cz.muni.ics.kypo.userandgroup.security.impl.UserInfoTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({FacadeConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.security"})
public class ResourceServerSecurityConfig extends ResourceServerConfigurerAdapter {

    private final CustomCorsFilter corsFilter;
    private final AuthorityGranter authorityGranter;
    private final IdentityProvidersConfig identityProvidersConfig;

    @Autowired
    public ResourceServerSecurityConfig(CustomCorsFilter corsFilter,
                                        AuthorityGranter authorityGranter,
                                        IdentityProvidersConfig identityProvidersConfig) {
        this.corsFilter = corsFilter;
        this.authorityGranter = authorityGranter;
        this.identityProvidersConfig = identityProvidersConfig;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenServices(tokenServices());
        resources.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .addFilterBefore(corsFilter, BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/webjars/**", "/microservices")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    @Bean
    public DynamicServerConfigurationService serverConfigurationService() {
        DynamicServerConfigurationService serverConfigurationService = new DynamicServerConfigurationService(identityProvidersConfig.getUserInfoEndpointsMapping());
        serverConfigurationService.setWhitelist(identityProvidersConfig.getSetOfIssuers());
        return serverConfigurationService;
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        UserInfoTokenService tokenService = new UserInfoTokenService();
        tokenService.setServerConfigurationService(serverConfigurationService());
        tokenService.setCacheTokens(true);
        tokenService.setAuthorityGranter(authorityGranter);
        return tokenService;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
