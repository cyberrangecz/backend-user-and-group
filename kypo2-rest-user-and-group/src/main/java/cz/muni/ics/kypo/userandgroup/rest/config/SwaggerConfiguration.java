package cz.muni.ics.kypo.userandgroup.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.*;

/**
 * Configuration of Swagger documentation.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Value("#{'${kypo.idp.4oauth.authorizationURIs}'.split(',')}")
    private List<String> authorizationURIs;
    @Value("#{'${kypo.idp.4oauth.client.clientIds}'.split(',')}")
    private List<String> clientIds;
    @Value("#{'${kypo.idp.4oauth.scopes}'.split(',')}")
    private Set<String> scopes;
    @Value("${swagger.enabled}")
    private boolean swaggerEnabled;


    private static String NAME_OF_TOKEN = "bearer";

    private static String NAME_OF_SECURITY_SCHEME = "KYPO";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerEnabled)
                .groupName("user-and-group-api")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cz.muni.ics.kypo.userandgroup.rest.controllers"))
                .paths(PathSelectors.any())
                .build()
                .directModelSubstitute(Pageable.class, SwaggerPageable.class)
                .securitySchemes(List.of(securityScheme()))
                .securityContexts(List.of(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("REST API documentation")
                .description("Developed by CSIRT team")
                .termsOfServiceUrl("Licensed by CSIRT team")
                .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId(clientIds.get(0).trim())
                .scopeSeparator(" ")
                .additionalQueryStringParams(Map.of("realm", "User Realm"))
                .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new ImplicitGrantBuilder()
                .loginEndpoint(new LoginEndpoint(authorizationURIs.get(0).trim()))
                .tokenName(NAME_OF_TOKEN)
                .build();

        return new OAuthBuilder().name(NAME_OF_SECURITY_SCHEME)
                .grantTypes(List.of(grantType))
                .scopes(List.of(scopes()))
                .build();
    }

    private AuthorizationScope[] scopes() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[scopes.size()];
        int i = 0;
        for (String scope : scopes) {
            authorizationScopes[i] = new AuthorizationScope(scope, "");
            i++;
        }
        return authorizationScopes;
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(
                        List.of(new SecurityReference(NAME_OF_SECURITY_SCHEME, scopes())))
                .forPaths(PathSelectors.any())
                .build();
    }

    private class SwaggerPageable {
        private Integer size;
        private Integer page;
        private String sort;
    }

}
