package cz.muni.ics.kypo.userandgroup.security.config;

import cz.muni.ics.kypo.userandgroup.config.PersistenceConfig;
import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.mitre.oauth2.introspectingfilter.service.impl.StaticIntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Set;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({PersistenceConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.security"})
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
                .authorizeRequests()
                .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs/**", "/webjars/**", "/microservices").permitAll()
                .anyRequest().authenticated();
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        IntrospectingTokenService tokenService = new IntrospectingTokenService();
        tokenService.setIntrospectionConfigurationService(introspectionConfigurationService());
        tokenService.setCacheTokens(false);
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
