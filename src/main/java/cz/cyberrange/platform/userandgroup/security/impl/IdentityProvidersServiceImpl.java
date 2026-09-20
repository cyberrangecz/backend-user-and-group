package cz.cyberrange.platform.userandgroup.security.impl;

import static cz.cyberrange.platform.userandgroup.security.util.JsonUtils.getAsString;
import static cz.cyberrange.platform.userandgroup.security.util.JsonUtils.getAsStringList;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.cyberrange.platform.userandgroup.security.IdentityProvidersService;
import cz.cyberrange.platform.userandgroup.security.config.IdentityProvidersConfig;
import cz.cyberrange.platform.userandgroup.security.model.WellKnownOpenIDConfiguration;
import cz.cyberrange.platform.userandgroup.security.util.JsonUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Fetches each identity provider's published OpenID Connect configuration and keeps it cached by
 * issuer.
 */
@Component
public class IdentityProvidersServiceImpl implements IdentityProvidersService {
  private static final Logger logger = LoggerFactory.getLogger(IdentityProvidersServiceImpl.class);
  private final LoadingCache<String, WellKnownOpenIDConfiguration> providersConfiguration;
  private final Set<String> providersList;

  @Autowired
  public IdentityProvidersServiceImpl(IdentityProvidersConfig identityProvidersConfig) {
    this.providersList = identityProvidersConfig.getSetOfIssuers();
    HttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
    this.providersConfiguration =
        CacheBuilder.newBuilder()
            .build(
                new IdentityProvidersServiceImpl.OpenIDConnectServiceConfigurationFetcher(
                    httpClient, identityProvidersConfig.getUserInfoEndpointsMapping()));
  }

  /**
   * Returns the published OpenID Connect configuration for the given identity provider, fetching
   * and parsing it from the provider's discovery endpoint when it is not already cached.
   *
   * @param provider issuer URL of the identity provider
   * @return the provider's configuration, or null when it could not be loaded
   * @throws AuthenticationServiceException when no identity provider is configured for that issuer
   */
  public WellKnownOpenIDConfiguration getIdentityProviderConfiguration(String provider) {
    try {
      if (!this.providersList.isEmpty() && !this.providersList.contains(provider)) {
        throw new AuthenticationServiceException(
            "Identity provider: " + provider + " is not recognized.");
      } else {
        return this.providersConfiguration.get(provider);
      }
    } catch (ExecutionException | UncheckedExecutionException var3) {
      logger.warn("Couldn't load configuration for " + provider + ": " + var3);
      return null;
    }
  }

  /** Shape of the JSON response returned by an identity provider's JWKS endpoint. */
  private static class JwksResponse {
    private List<Map<String, Object>> keys;

    public List<Map<String, Object>> getKeys() {
      return keys;
    }

    public void setKeys(List<Map<String, Object>> keys) {
      this.keys = keys;
    }
  }

  /** Loads and parses an identity provider's published OpenID Connect discovery document. */
  private class OpenIDConnectServiceConfigurationFetcher
      extends CacheLoader<String, WellKnownOpenIDConfiguration> {
    private final HttpComponentsClientHttpRequestFactory httpFactory;
    private final Map<String, String> userInfoEndpointsMap;

    OpenIDConnectServiceConfigurationFetcher(
        HttpClient httpClient, Map<String, String> userInfoEndpointsMap) {
      this.httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
      this.userInfoEndpointsMap = userInfoEndpointsMap;
    }

    /**
     * Fetches and parses the identity provider's published discovery document into a configuration,
     * then fetches its JSON Web Key Set. The user info endpoint uses the mapping configured for the
     * provider when one is set, falling back to the value from the discovery document otherwise.
     *
     * @param provider issuer URL of the identity provider to fetch the discovery document from
     * @return the parsed configuration, with its JSON Web Key Set populated
     * @throws IllegalStateException when the discovery document is not a JSON object, or does not
     *     have an issuer field
     */
    public WellKnownOpenIDConfiguration load(String provider) {
      RestTemplate restTemplate = new RestTemplate(this.httpFactory);
      WellKnownOpenIDConfiguration conf = new WellKnownOpenIDConfiguration();
      String url = provider + "/.well-known/openid-configuration";
      String jsonString = restTemplate.getForObject(url, String.class);
      JsonElement parsed = JsonParser.parseString(jsonString);
      if (parsed.isJsonObject()) {
        JsonObject o = parsed.getAsJsonObject();
        if (!o.has("issuer")) {
          throw new IllegalStateException("Returned object did not have an 'issuer' field");
        } else {
          if (!provider.equals(o.get("issuer").getAsString())) {
            IdentityProvidersServiceImpl.logger.info(
                "Issuer used for discover was "
                    + provider
                    + " but final issuer is "
                    + o.get("issuer").getAsString());
          }

          conf.setIssuer(o.get("issuer").getAsString());
          conf.setAuthorizationEndpointUri(getAsString(o, "authorization_endpoint"));
          conf.setTokenEndpointUri(getAsString(o, "token_endpoint"));
          conf.setJwksUri(getAsString(o, "jwks_uri"));
          conf.setUserInfoUri(
              userInfoEndpointsMap.getOrDefault(provider, getAsString(o, "userinfo_endpoint")));
          conf.setRegistrationEndpointUri(getAsString(o, "registration_endpoint"));
          conf.setIntrospectionEndpointUri(getAsString(o, "introspection_endpoint"));
          conf.setCheckSessionIframe(getAsString(o, "check_session_iframe"));
          conf.setEndSessionEndpoint(getAsString(o, "end_session_endpoint"));
          conf.setAcrValuesSupported(getAsStringList(o, "acr_values_supported"));
          conf.setClaimsLocalesSupported(getAsStringList(o, "claims_locales_supported"));
          conf.setClaimsParameterSupported(JsonUtils.getAsBoolean(o, "claims_parameter_supported"));
          conf.setClaimsSupported(getAsStringList(o, "claims_supported"));
          conf.setDisplayValuesSupported(getAsStringList(o, "display_values_supported"));
          conf.setGrantTypesSupported(getAsStringList(o, "grant_types_supported"));
          conf.setIdTokenSigningAlgValuesSupported(
              JsonUtils.getAsJwsAlgorithmList(o, "id_token_signing_alg_values_supported"));
          conf.setIdTokenEncryptionAlgValuesSupported(
              JsonUtils.getAsJweAlgorithmList(o, "id_token_encryption_alg_values_supported"));
          conf.setIdTokenEncryptionEncValuesSupported(
              JsonUtils.getAsEncryptionMethodList(o, "id_token_encryption_enc_values_supported"));
          conf.setOpPolicyUri(getAsString(o, "op_policy_uri"));
          conf.setOpTosUri(getAsString(o, "op_tos_uri"));
          conf.setRequestObjectEncryptionAlgValuesSupported(
              JsonUtils.getAsJweAlgorithmList(o, "request_object_encryption_alg_values_supported"));
          conf.setRequestObjectEncryptionEncValuesSupported(
              JsonUtils.getAsEncryptionMethodList(
                  o, "request_object_encryption_enc_values_supported"));
          conf.setRequestObjectSigningAlgValuesSupported(
              JsonUtils.getAsJwsAlgorithmList(o, "request_object_signing_alg_values_supported"));
          conf.setRequestParameterSupported(
              JsonUtils.getAsBoolean(o, "request_parameter_supported"));
          conf.setRequestUriParameterSupported(
              JsonUtils.getAsBoolean(o, "request_uri_parameter_supported"));
          conf.setResponseTypesSupported(getAsStringList(o, "response_types_supported"));
          conf.setScopesSupported(getAsStringList(o, "scopes_supported"));
          conf.setSubjectTypesSupported(getAsStringList(o, "subject_types_supported"));
          conf.setServiceDocumentation(getAsString(o, "service_documentation"));
          conf.setTokenEndpointAuthMethodsSupported(
              getAsStringList(o, "token_endpoint_auth_methods"));
          conf.setTokenEndpointAuthSigningAlgValuesSupported(
              JsonUtils.getAsJwsAlgorithmList(
                  o, "token_endpoint_auth_signing_alg_values_supported"));
          conf.setUiLocalesSupported(getAsStringList(o, "ui_locales_supported"));
          conf.setUserinfoEncryptionAlgValuesSupported(
              JsonUtils.getAsJweAlgorithmList(o, "userinfo_encryption_alg_values_supported"));
          conf.setUserinfoEncryptionEncValuesSupported(
              JsonUtils.getAsEncryptionMethodList(o, "userinfo_encryption_enc_values_supported"));
          conf.setUserinfoSigningAlgValuesSupported(
              JsonUtils.getAsJwsAlgorithmList(o, "userinfo_signing_alg_values_supported"));
          conf.setJwks(getJwks(conf.getJwksUri()));
          return conf;
        }
      } else {
        throw new IllegalStateException("Couldn't parse server discovery results for " + url);
      }
    }

    /**
     * Fetches the identity provider's JSON Web Key Set from the given URI.
     *
     * @param jwksUri location of the JSON Web Key Set, or null when the provider does not have one
     * @return the keys from the set, or null when it is missing or could not be fetched
     */
    private List<Map<String, Object>> getJwks(String jwksUri) {
      if (jwksUri != null) {
        JwksResponse issuerKeys = new RestTemplate().getForObject(jwksUri, JwksResponse.class);
        if (issuerKeys != null) {
          return issuerKeys.getKeys();
        }
      }
      logger.error("Could not obtain JSON Web Key Set from URL: " + jwksUri);
      return null;
    }
  }
}
