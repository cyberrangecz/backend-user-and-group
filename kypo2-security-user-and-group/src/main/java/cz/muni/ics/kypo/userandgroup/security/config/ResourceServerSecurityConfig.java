package cz.muni.ics.kypo.userandgroup.security.config;

import cz.muni.ics.kypo.userandgroup.config.PersistenceConfig;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({PersistenceConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.userandgroup.security"})
public class ResourceServerSecurityConfig extends ResourceServerConfigurerAdapter {

    @Value("${kypo.muni.idp.4oauth.issuer}")
    private String issuerMUNI;
    @Value("${kypo.muni.idp.4oauth.introspectionURI}")
    private String introspectionURIMUNI;
    @Value("${kypo.muni.idp.4oauth.resource.clientId}")
    private String clientIdOfResourceMUNI;
    @Value("${kypo.muni.idp.4oauth.resource.clientSecret}")
    private String clientSecretResourceMUNI;
    @Value("#{'${kypo.muni.idp.4oauth.scopes}'.split(',')}")
    private Set<String> scopesMUNI;
    //TODO uncomment after adding new custom mitre provider
//    @Value("${kypo.mitre.idp.4oauth.issuer}")
//    private String issuerKYPO;
//    @Value("${kypo.mitre.idp.4oauth.introspectionURI}")
//    private String introspectionURIKYPO;
//    @Value("${kypo.mitre.idp.4oauth.resource.clientId}")
//    private String clientIdOfResourceKYPO;
//    @Value("${kypo.mitre.idp.4oauth.resource.clientSecret}")
//    private String clientSecretResourceKYPO;
//    @Value("#{'${kypo.mitre.idp.4oauth.scopes}'.split(',')}")
//    private Set<String> scopesKYPO;

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
                .anyRequest().authenticated()
                .and()
                .requiresChannel()
                .antMatchers()
                .requiresSecure()
                .and()
                .x509()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true);
    }

    @Bean
    public ServerConfigurationService serverConfigurationService() {
        DynamicServerConfigurationService serverConfigurationService =
                new DynamicServerConfigurationService();
        //TODO add issuerKYPO to the set of whitelist
        serverConfigurationService.setWhitelist(new HashSet<>(Set.of(issuerMUNI)));
        return serverConfigurationService;
    }

    @Bean
    public ClientConfigurationService clientConfigurationService() {
        Map<String, RegisteredClient> clients = new HashMap<>();
        //TODO uncomment after adding new custom mitre provider
//        RegisteredClient clientKYPO = new RegisteredClient();
//        clientKYPO.setClientId(clientIdOfResourceKYPO);
//        clientKYPO.setClientSecret(clientSecretResourceKYPO);
//        clientKYPO.setScope(scopesKYPO);
//        clients.put(issuerKYPO, clientKYPO);

        RegisteredClient clientMUNI = new RegisteredClient();
        clientMUNI.setClientId(clientIdOfResourceMUNI);
        clientMUNI.setClientSecret(clientSecretResourceMUNI);
        clientMUNI.setScope(scopesMUNI);
        clients.put(issuerMUNI, clientMUNI);

        StaticClientConfigurationService clientConfigurationService =
                new StaticClientConfigurationService();
        clientConfigurationService.setClients(clients);

        return clientConfigurationService;
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
    public IntrospectionConfigurationService introspectionConfigurationService() {
        JWTParsingIntrospectionConfigurationService introspectionConfigurationService =
                new JWTParsingIntrospectionConfigurationService();
        introspectionConfigurationService
                .setServerConfigurationService(serverConfigurationService());
        introspectionConfigurationService
                .setClientConfigurationService(clientConfigurationService());
        return introspectionConfigurationService;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


}
