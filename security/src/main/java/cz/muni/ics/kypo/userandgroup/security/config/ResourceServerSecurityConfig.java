package cz.muni.ics.kypo.userandgroup.security.config;

import cz.muni.ics.kypo.userandgroup.config.PersistenceConfig;
import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.mitre.oauth2.introspectingfilter.service.impl.StaticIntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.Set;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({PersistenceConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.security"})
@PropertySource("file:${path-to-config-file}")
public class ResourceServerSecurityConfig extends ResourceServerConfigurerAdapter {

    @Value("${kypo.idp.4oauth.introspectionURI}")
    private String introspectionURI;

    @Value("${kypo.idp.4oauth.resource.clientId}")
    private String clientIdOfResource;

    @Value("${kypo.idp.4oauth.resource.clientSecret}")
    private String clientSecretResource;

    @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
    private Set<String> scopes;

    @Autowired
    private CustomAuthorityGranter customAuthorityGranter;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenServices(tokenServices());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/swagger-ui.html","/swagger-resources/**", "/v2/api-docs/**", "/webjars/**").permitAll()
                .anyRequest().authenticated();
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        IntrospectingTokenService tokenService = new IntrospectingTokenService();
        tokenService.setIntrospectionConfigurationService(introspectionConfigurationService());
        tokenService.setIntrospectionAuthorityGranter(customAuthorityGranter);
        return tokenService;
    }

    @Bean
    public StaticIntrospectionConfigurationService introspectionConfigurationService() {
        StaticIntrospectionConfigurationService introspectionService = new StaticIntrospectionConfigurationService();
        introspectionService.setIntrospectionUrl(introspectionURI);

        RegisteredClient client = new RegisteredClient();
        client.setClientId(clientIdOfResource);
        client.setClientSecret(clientSecretResource);
        client.setScope(scopes);
        introspectionService.setClientConfiguration(client);

        return introspectionService;
    }


}
