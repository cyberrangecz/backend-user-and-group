package cz.muni.ics.kypo.userandgroup.security.config;

import cz.muni.ics.kypo.userandgroup.config.FacadeConfig;
import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.JWTParsingIntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService;
import org.mitre.openid.connect.client.service.impl.StaticClientConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({FacadeConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.security"})
public class ResourceServerSecurityConfig extends ResourceServerConfigurerAdapter {

    @Value("#{'${kypo.idp.4oauth.issuers}'.split(',')}")
    private List<String> issuers;
    @Value("#{'${kypo.idp.4oauth.resource.clientIds}'.split(',')}")
    private List<String> clientIdsOfResources;
    @Value("#{'${kypo.idp.4oauth.resource.clientSecrets}'.split(',')}")
    private List<String> clientSecretResources;
    @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
    private Set<String> scopes;

    private Environment environment;
    private CustomCorsFilter corsFilter;
    private CustomAuthorityGranter customAuthorityGranter;

    @Autowired
    public ResourceServerSecurityConfig(Environment environment, CustomCorsFilter corsFilter, CustomAuthorityGranter customAuthorityGranter) {
        this.environment = environment;
        this.corsFilter = corsFilter;
        this.customAuthorityGranter = customAuthorityGranter;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenServices(tokenServices());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .addFilterBefore(corsFilter, BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs/**", "/webjars/**", "/microservices")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    @Bean
    public ServerConfigurationService serverConfigurationService() {
        DynamicServerConfigurationService serverConfigurationService = new DynamicServerConfigurationService();
        serverConfigurationService.setWhitelist(
                issuers.stream()
                        .map(String::trim)
                        .collect(Collectors.toSet()));
        return serverConfigurationService;
    }

    @Bean
    public ClientConfigurationService clientConfigurationService() {
        Map<String, RegisteredClient> clients = new HashMap<>();
        for (int i = 0; i < issuers.size(); i++) {
            RegisteredClient client = new RegisteredClient();
            client.setClientId(clientIdsOfResources.get(i).trim());
            client.setClientSecret(clientSecretResources.get(i).trim());
            client.setScope(scopes);
            clients.put(issuers.get(i).trim(), client);
        }
        StaticClientConfigurationService clientConfigurationService = new StaticClientConfigurationService();
        clientConfigurationService.setClients(clients);
        return clientConfigurationService;
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        IntrospectingTokenService tokenService = new IntrospectingTokenService();
        tokenService.setIntrospectionConfigurationService(introspectionConfigurationService());
        tokenService.setCacheTokens(true);
        tokenService.setIntrospectionAuthorityGranter(customAuthorityGranter);
        return tokenService;
    }

    @Bean
    public IntrospectionConfigurationService introspectionConfigurationService() {
        JWTParsingIntrospectionConfigurationService introspectionConfigurationService = new JWTParsingIntrospectionConfigurationService();
        introspectionConfigurationService.setServerConfigurationService(serverConfigurationService());
        introspectionConfigurationService.setClientConfigurationService(clientConfigurationService());
        return introspectionConfigurationService;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


}
