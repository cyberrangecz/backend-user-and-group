package cz.cyberrange.platform.userandgroup.security.model;


import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

import java.util.List;
import java.util.Map;

public class WellKnownOpenIDConfiguration {
    private String authorizationEndpointUri;
    private String tokenEndpointUri;
    private String registrationEndpointUri;
    private String issuer;
    private String jwksUri;
    private String userInfoUri;
    private String introspectionEndpointUri;
    private String revocationEndpointUri;
    private String checkSessionIframe;
    private String endSessionEndpoint;
    private List<String> scopesSupported;
    private List<String> responseTypesSupported;
    private List<String> grantTypesSupported;
    private List<String> acrValuesSupported;
    private List<String> subjectTypesSupported;
    private List<JWSAlgorithm> userinfoSigningAlgValuesSupported;
    private List<JWEAlgorithm> userinfoEncryptionAlgValuesSupported;
    private List<EncryptionMethod> userinfoEncryptionEncValuesSupported;
    private List<JWSAlgorithm> idTokenSigningAlgValuesSupported;
    private List<JWEAlgorithm> idTokenEncryptionAlgValuesSupported;
    private List<EncryptionMethod> idTokenEncryptionEncValuesSupported;
    private List<JWSAlgorithm> requestObjectSigningAlgValuesSupported;
    private List<JWEAlgorithm> requestObjectEncryptionAlgValuesSupported;
    private List<EncryptionMethod> requestObjectEncryptionEncValuesSupported;
    private List<String> tokenEndpointAuthMethodsSupported;
    private List<JWSAlgorithm> tokenEndpointAuthSigningAlgValuesSupported;
    private List<String> displayValuesSupported;
    private List<String> claimTypesSupported;
    private List<String> claimsSupported;
    private String serviceDocumentation;
    private List<String> claimsLocalesSupported;
    private List<String> uiLocalesSupported;
    private Boolean claimsParameterSupported;
    private Boolean requestParameterSupported;
    private Boolean requestUriParameterSupported;
    private Boolean requireRequestUriRegistration;
    private String opPolicyUri;
    private String opTosUri;
    private WellKnownOpenIDConfiguration.UserInfoTokenMethod userInfoTokenMethod;
    private List<Map<String, Object>> jwks;

    public WellKnownOpenIDConfiguration() {
    }

    public String getAuthorizationEndpointUri() {
        return this.authorizationEndpointUri;
    }

    public void setAuthorizationEndpointUri(String authorizationEndpointUri) {
        this.authorizationEndpointUri = authorizationEndpointUri;
    }

    public String getTokenEndpointUri() {
        return this.tokenEndpointUri;
    }

    public void setTokenEndpointUri(String tokenEndpointUri) {
        this.tokenEndpointUri = tokenEndpointUri;
    }

    public String getRegistrationEndpointUri() {
        return this.registrationEndpointUri;
    }

    public void setRegistrationEndpointUri(String registrationEndpointUri) {
        this.registrationEndpointUri = registrationEndpointUri;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwksUri() {
        return this.jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public String getUserInfoUri() {
        return this.userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    public String getIntrospectionEndpointUri() {
        return this.introspectionEndpointUri;
    }

    public void setIntrospectionEndpointUri(String introspectionEndpointUri) {
        this.introspectionEndpointUri = introspectionEndpointUri;
    }

    public String getCheckSessionIframe() {
        return this.checkSessionIframe;
    }

    public void setCheckSessionIframe(String checkSessionIframe) {
        this.checkSessionIframe = checkSessionIframe;
    }

    public String getEndSessionEndpoint() {
        return this.endSessionEndpoint;
    }

    public void setEndSessionEndpoint(String endSessionEndpoint) {
        this.endSessionEndpoint = endSessionEndpoint;
    }

    public List<String> getScopesSupported() {
        return this.scopesSupported;
    }

    public void setScopesSupported(List<String> scopesSupported) {
        this.scopesSupported = scopesSupported;
    }

    public List<String> getResponseTypesSupported() {
        return this.responseTypesSupported;
    }

    public void setResponseTypesSupported(List<String> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public List<String> getGrantTypesSupported() {
        return this.grantTypesSupported;
    }

    public void setGrantTypesSupported(List<String> grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    public List<String> getAcrValuesSupported() {
        return this.acrValuesSupported;
    }

    public void setAcrValuesSupported(List<String> acrValuesSupported) {
        this.acrValuesSupported = acrValuesSupported;
    }

    public List<String> getSubjectTypesSupported() {
        return this.subjectTypesSupported;
    }

    public void setSubjectTypesSupported(List<String> subjectTypesSupported) {
        this.subjectTypesSupported = subjectTypesSupported;
    }

    public List<JWSAlgorithm> getUserinfoSigningAlgValuesSupported() {
        return this.userinfoSigningAlgValuesSupported;
    }

    public void setUserinfoSigningAlgValuesSupported(List<JWSAlgorithm> userinfoSigningAlgValuesSupported) {
        this.userinfoSigningAlgValuesSupported = userinfoSigningAlgValuesSupported;
    }

    public List<JWEAlgorithm> getUserinfoEncryptionAlgValuesSupported() {
        return this.userinfoEncryptionAlgValuesSupported;
    }

    public void setUserinfoEncryptionAlgValuesSupported(List<JWEAlgorithm> userinfoEncryptionAlgValuesSupported) {
        this.userinfoEncryptionAlgValuesSupported = userinfoEncryptionAlgValuesSupported;
    }

    public List<EncryptionMethod> getUserinfoEncryptionEncValuesSupported() {
        return this.userinfoEncryptionEncValuesSupported;
    }

    public void setUserinfoEncryptionEncValuesSupported(List<EncryptionMethod> userinfoEncryptionEncValuesSupported) {
        this.userinfoEncryptionEncValuesSupported = userinfoEncryptionEncValuesSupported;
    }

    public List<JWSAlgorithm> getIdTokenSigningAlgValuesSupported() {
        return this.idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(List<JWSAlgorithm> idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }

    public List<JWEAlgorithm> getIdTokenEncryptionAlgValuesSupported() {
        return this.idTokenEncryptionAlgValuesSupported;
    }

    public void setIdTokenEncryptionAlgValuesSupported(List<JWEAlgorithm> idTokenEncryptionAlgValuesSupported) {
        this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
    }

    public List<EncryptionMethod> getIdTokenEncryptionEncValuesSupported() {
        return this.idTokenEncryptionEncValuesSupported;
    }

    public void setIdTokenEncryptionEncValuesSupported(List<EncryptionMethod> idTokenEncryptionEncValuesSupported) {
        this.idTokenEncryptionEncValuesSupported = idTokenEncryptionEncValuesSupported;
    }

    public List<JWSAlgorithm> getRequestObjectSigningAlgValuesSupported() {
        return this.requestObjectSigningAlgValuesSupported;
    }

    public void setRequestObjectSigningAlgValuesSupported(List<JWSAlgorithm> requestObjectSigningAlgValuesSupported) {
        this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
    }

    public List<JWEAlgorithm> getRequestObjectEncryptionAlgValuesSupported() {
        return this.requestObjectEncryptionAlgValuesSupported;
    }

    public void setRequestObjectEncryptionAlgValuesSupported(List<JWEAlgorithm> requestObjectEncryptionAlgValuesSupported) {
        this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
    }

    public List<EncryptionMethod> getRequestObjectEncryptionEncValuesSupported() {
        return this.requestObjectEncryptionEncValuesSupported;
    }

    public void setRequestObjectEncryptionEncValuesSupported(List<EncryptionMethod> requestObjectEncryptionEncValuesSupported) {
        this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
    }

    public List<String> getTokenEndpointAuthMethodsSupported() {
        return this.tokenEndpointAuthMethodsSupported;
    }

    public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
    }

    public List<JWSAlgorithm> getTokenEndpointAuthSigningAlgValuesSupported() {
        return this.tokenEndpointAuthSigningAlgValuesSupported;
    }

    public void setTokenEndpointAuthSigningAlgValuesSupported(List<JWSAlgorithm> tokenEndpointAuthSigningAlgValuesSupported) {
        this.tokenEndpointAuthSigningAlgValuesSupported = tokenEndpointAuthSigningAlgValuesSupported;
    }

    public List<String> getDisplayValuesSupported() {
        return this.displayValuesSupported;
    }

    public void setDisplayValuesSupported(List<String> displayValuesSupported) {
        this.displayValuesSupported = displayValuesSupported;
    }

    public List<String> getClaimTypesSupported() {
        return this.claimTypesSupported;
    }

    public void setClaimTypesSupported(List<String> claimTypesSupported) {
        this.claimTypesSupported = claimTypesSupported;
    }

    public List<String> getClaimsSupported() {
        return this.claimsSupported;
    }

    public void setClaimsSupported(List<String> claimsSupported) {
        this.claimsSupported = claimsSupported;
    }

    public String getServiceDocumentation() {
        return this.serviceDocumentation;
    }

    public void setServiceDocumentation(String serviceDocumentation) {
        this.serviceDocumentation = serviceDocumentation;
    }

    public List<String> getClaimsLocalesSupported() {
        return this.claimsLocalesSupported;
    }

    public void setClaimsLocalesSupported(List<String> claimsLocalesSupported) {
        this.claimsLocalesSupported = claimsLocalesSupported;
    }

    public List<String> getUiLocalesSupported() {
        return this.uiLocalesSupported;
    }

    public void setUiLocalesSupported(List<String> uiLocalesSupported) {
        this.uiLocalesSupported = uiLocalesSupported;
    }

    public Boolean getClaimsParameterSupported() {
        return this.claimsParameterSupported;
    }

    public void setClaimsParameterSupported(Boolean claimsParameterSupported) {
        this.claimsParameterSupported = claimsParameterSupported;
    }

    public Boolean getRequestParameterSupported() {
        return this.requestParameterSupported;
    }

    public void setRequestParameterSupported(Boolean requestParameterSupported) {
        this.requestParameterSupported = requestParameterSupported;
    }

    public Boolean getRequestUriParameterSupported() {
        return this.requestUriParameterSupported;
    }

    public void setRequestUriParameterSupported(Boolean requestUriParameterSupported) {
        this.requestUriParameterSupported = requestUriParameterSupported;
    }

    public Boolean getRequireRequestUriRegistration() {
        return this.requireRequestUriRegistration;
    }

    public void setRequireRequestUriRegistration(Boolean requireRequestUriRegistration) {
        this.requireRequestUriRegistration = requireRequestUriRegistration;
    }

    public String getOpPolicyUri() {
        return this.opPolicyUri;
    }

    public void setOpPolicyUri(String opPolicyUri) {
        this.opPolicyUri = opPolicyUri;
    }

    public String getOpTosUri() {
        return this.opTosUri;
    }

    public void setOpTosUri(String opTosUri) {
        this.opTosUri = opTosUri;
    }

    public String getRevocationEndpointUri() {
        return this.revocationEndpointUri;
    }

    public void setRevocationEndpointUri(String revocationEndpointUri) {
        this.revocationEndpointUri = revocationEndpointUri;
    }

    public UserInfoTokenMethod getUserInfoTokenMethod() {
        return userInfoTokenMethod;
    }

    public void setUserInfoTokenMethod(UserInfoTokenMethod userInfoTokenMethod) {
        this.userInfoTokenMethod = userInfoTokenMethod;
    }

    public List<Map<String, Object>> getJwks() {
        return jwks;
    }

    public void setJwks(List<Map<String, Object>> jwks) {
        this.jwks = jwks;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.acrValuesSupported == null ? 0 : this.acrValuesSupported.hashCode());
        result = 31 * result + (this.authorizationEndpointUri == null ? 0 : this.authorizationEndpointUri.hashCode());
        result = 31 * result + (this.checkSessionIframe == null ? 0 : this.checkSessionIframe.hashCode());
        result = 31 * result + (this.claimTypesSupported == null ? 0 : this.claimTypesSupported.hashCode());
        result = 31 * result + (this.claimsLocalesSupported == null ? 0 : this.claimsLocalesSupported.hashCode());
        result = 31 * result + (this.claimsParameterSupported == null ? 0 : this.claimsParameterSupported.hashCode());
        result = 31 * result + (this.claimsSupported == null ? 0 : this.claimsSupported.hashCode());
        result = 31 * result + (this.displayValuesSupported == null ? 0 : this.displayValuesSupported.hashCode());
        result = 31 * result + (this.endSessionEndpoint == null ? 0 : this.endSessionEndpoint.hashCode());
        result = 31 * result + (this.grantTypesSupported == null ? 0 : this.grantTypesSupported.hashCode());
        result = 31 * result + (this.idTokenEncryptionAlgValuesSupported == null ? 0 : this.idTokenEncryptionAlgValuesSupported.hashCode());
        result = 31 * result + (this.idTokenEncryptionEncValuesSupported == null ? 0 : this.idTokenEncryptionEncValuesSupported.hashCode());
        result = 31 * result + (this.idTokenSigningAlgValuesSupported == null ? 0 : this.idTokenSigningAlgValuesSupported.hashCode());
        result = 31 * result + (this.introspectionEndpointUri == null ? 0 : this.introspectionEndpointUri.hashCode());
        result = 31 * result + (this.issuer == null ? 0 : this.issuer.hashCode());
        result = 31 * result + (this.jwksUri == null ? 0 : this.jwksUri.hashCode());
        result = 31 * result + (this.opPolicyUri == null ? 0 : this.opPolicyUri.hashCode());
        result = 31 * result + (this.opTosUri == null ? 0 : this.opTosUri.hashCode());
        result = 31 * result + (this.registrationEndpointUri == null ? 0 : this.registrationEndpointUri.hashCode());
        result = 31 * result + (this.requestObjectEncryptionAlgValuesSupported == null ? 0 : this.requestObjectEncryptionAlgValuesSupported.hashCode());
        result = 31 * result + (this.requestObjectEncryptionEncValuesSupported == null ? 0 : this.requestObjectEncryptionEncValuesSupported.hashCode());
        result = 31 * result + (this.requestObjectSigningAlgValuesSupported == null ? 0 : this.requestObjectSigningAlgValuesSupported.hashCode());
        result = 31 * result + (this.requestParameterSupported == null ? 0 : this.requestParameterSupported.hashCode());
        result = 31 * result + (this.requestUriParameterSupported == null ? 0 : this.requestUriParameterSupported.hashCode());
        result = 31 * result + (this.requireRequestUriRegistration == null ? 0 : this.requireRequestUriRegistration.hashCode());
        result = 31 * result + (this.responseTypesSupported == null ? 0 : this.responseTypesSupported.hashCode());
        result = 31 * result + (this.revocationEndpointUri == null ? 0 : this.revocationEndpointUri.hashCode());
        result = 31 * result + (this.scopesSupported == null ? 0 : this.scopesSupported.hashCode());
        result = 31 * result + (this.serviceDocumentation == null ? 0 : this.serviceDocumentation.hashCode());
        result = 31 * result + (this.subjectTypesSupported == null ? 0 : this.subjectTypesSupported.hashCode());
        result = 31 * result + (this.tokenEndpointAuthMethodsSupported == null ? 0 : this.tokenEndpointAuthMethodsSupported.hashCode());
        result = 31 * result + (this.tokenEndpointAuthSigningAlgValuesSupported == null ? 0 : this.tokenEndpointAuthSigningAlgValuesSupported.hashCode());
        result = 31 * result + (this.tokenEndpointUri == null ? 0 : this.tokenEndpointUri.hashCode());
        result = 31 * result + (this.uiLocalesSupported == null ? 0 : this.uiLocalesSupported.hashCode());
        result = 31 * result + (this.userInfoUri == null ? 0 : this.userInfoUri.hashCode());
        result = 31 * result + (this.userinfoEncryptionAlgValuesSupported == null ? 0 : this.userinfoEncryptionAlgValuesSupported.hashCode());
        result = 31 * result + (this.userinfoEncryptionEncValuesSupported == null ? 0 : this.userinfoEncryptionEncValuesSupported.hashCode());
        result = 31 * result + (this.userinfoSigningAlgValuesSupported == null ? 0 : this.userinfoSigningAlgValuesSupported.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            WellKnownOpenIDConfiguration other = (WellKnownOpenIDConfiguration) obj;
            if (this.acrValuesSupported == null) {
                if (other.acrValuesSupported != null) {
                    return false;
                }
            } else if (!this.acrValuesSupported.equals(other.acrValuesSupported)) {
                return false;
            }

            if (this.authorizationEndpointUri == null) {
                if (other.authorizationEndpointUri != null) {
                    return false;
                }
            } else if (!this.authorizationEndpointUri.equals(other.authorizationEndpointUri)) {
                return false;
            }

            if (this.checkSessionIframe == null) {
                if (other.checkSessionIframe != null) {
                    return false;
                }
            } else if (!this.checkSessionIframe.equals(other.checkSessionIframe)) {
                return false;
            }

            if (this.claimTypesSupported == null) {
                if (other.claimTypesSupported != null) {
                    return false;
                }
            } else if (!this.claimTypesSupported.equals(other.claimTypesSupported)) {
                return false;
            }

            if (this.claimsLocalesSupported == null) {
                if (other.claimsLocalesSupported != null) {
                    return false;
                }
            } else if (!this.claimsLocalesSupported.equals(other.claimsLocalesSupported)) {
                return false;
            }

            if (this.claimsParameterSupported == null) {
                if (other.claimsParameterSupported != null) {
                    return false;
                }
            } else if (!this.claimsParameterSupported.equals(other.claimsParameterSupported)) {
                return false;
            }

            if (this.claimsSupported == null) {
                if (other.claimsSupported != null) {
                    return false;
                }
            } else if (!this.claimsSupported.equals(other.claimsSupported)) {
                return false;
            }

            if (this.displayValuesSupported == null) {
                if (other.displayValuesSupported != null) {
                    return false;
                }
            } else if (!this.displayValuesSupported.equals(other.displayValuesSupported)) {
                return false;
            }

            if (this.endSessionEndpoint == null) {
                if (other.endSessionEndpoint != null) {
                    return false;
                }
            } else if (!this.endSessionEndpoint.equals(other.endSessionEndpoint)) {
                return false;
            }

            if (this.grantTypesSupported == null) {
                if (other.grantTypesSupported != null) {
                    return false;
                }
            } else if (!this.grantTypesSupported.equals(other.grantTypesSupported)) {
                return false;
            }

            if (this.idTokenEncryptionAlgValuesSupported == null) {
                if (other.idTokenEncryptionAlgValuesSupported != null) {
                    return false;
                }
            } else if (!this.idTokenEncryptionAlgValuesSupported.equals(other.idTokenEncryptionAlgValuesSupported)) {
                return false;
            }

            if (this.idTokenEncryptionEncValuesSupported == null) {
                if (other.idTokenEncryptionEncValuesSupported != null) {
                    return false;
                }
            } else if (!this.idTokenEncryptionEncValuesSupported.equals(other.idTokenEncryptionEncValuesSupported)) {
                return false;
            }

            if (this.idTokenSigningAlgValuesSupported == null) {
                if (other.idTokenSigningAlgValuesSupported != null) {
                    return false;
                }
            } else if (!this.idTokenSigningAlgValuesSupported.equals(other.idTokenSigningAlgValuesSupported)) {
                return false;
            }

            if (this.introspectionEndpointUri == null) {
                if (other.introspectionEndpointUri != null) {
                    return false;
                }
            } else if (!this.introspectionEndpointUri.equals(other.introspectionEndpointUri)) {
                return false;
            }

            if (this.issuer == null) {
                if (other.issuer != null) {
                    return false;
                }
            } else if (!this.issuer.equals(other.issuer)) {
                return false;
            }

            if (this.jwksUri == null) {
                if (other.jwksUri != null) {
                    return false;
                }
            } else if (!this.jwksUri.equals(other.jwksUri)) {
                return false;
            }

            if (this.opPolicyUri == null) {
                if (other.opPolicyUri != null) {
                    return false;
                }
            } else if (!this.opPolicyUri.equals(other.opPolicyUri)) {
                return false;
            }

            if (this.opTosUri == null) {
                if (other.opTosUri != null) {
                    return false;
                }
            } else if (!this.opTosUri.equals(other.opTosUri)) {
                return false;
            }

            if (this.registrationEndpointUri == null) {
                if (other.registrationEndpointUri != null) {
                    return false;
                }
            } else if (!this.registrationEndpointUri.equals(other.registrationEndpointUri)) {
                return false;
            }

            if (this.requestObjectEncryptionAlgValuesSupported == null) {
                if (other.requestObjectEncryptionAlgValuesSupported != null) {
                    return false;
                }
            } else if (!this.requestObjectEncryptionAlgValuesSupported.equals(other.requestObjectEncryptionAlgValuesSupported)) {
                return false;
            }

            if (this.requestObjectEncryptionEncValuesSupported == null) {
                if (other.requestObjectEncryptionEncValuesSupported != null) {
                    return false;
                }
            } else if (!this.requestObjectEncryptionEncValuesSupported.equals(other.requestObjectEncryptionEncValuesSupported)) {
                return false;
            }

            if (this.requestObjectSigningAlgValuesSupported == null) {
                if (other.requestObjectSigningAlgValuesSupported != null) {
                    return false;
                }
            } else if (!this.requestObjectSigningAlgValuesSupported.equals(other.requestObjectSigningAlgValuesSupported)) {
                return false;
            }

            if (this.requestParameterSupported == null) {
                if (other.requestParameterSupported != null) {
                    return false;
                }
            } else if (!this.requestParameterSupported.equals(other.requestParameterSupported)) {
                return false;
            }

            if (this.requestUriParameterSupported == null) {
                if (other.requestUriParameterSupported != null) {
                    return false;
                }
            } else if (!this.requestUriParameterSupported.equals(other.requestUriParameterSupported)) {
                return false;
            }

            if (this.requireRequestUriRegistration == null) {
                if (other.requireRequestUriRegistration != null) {
                    return false;
                }
            } else if (!this.requireRequestUriRegistration.equals(other.requireRequestUriRegistration)) {
                return false;
            }

            if (this.responseTypesSupported == null) {
                if (other.responseTypesSupported != null) {
                    return false;
                }
            } else if (!this.responseTypesSupported.equals(other.responseTypesSupported)) {
                return false;
            }

            if (this.revocationEndpointUri == null) {
                if (other.revocationEndpointUri != null) {
                    return false;
                }
            } else if (!this.revocationEndpointUri.equals(other.revocationEndpointUri)) {
                return false;
            }

            if (this.scopesSupported == null) {
                if (other.scopesSupported != null) {
                    return false;
                }
            } else if (!this.scopesSupported.equals(other.scopesSupported)) {
                return false;
            }

            if (this.serviceDocumentation == null) {
                if (other.serviceDocumentation != null) {
                    return false;
                }
            } else if (!this.serviceDocumentation.equals(other.serviceDocumentation)) {
                return false;
            }

            if (this.subjectTypesSupported == null) {
                if (other.subjectTypesSupported != null) {
                    return false;
                }
            } else if (!this.subjectTypesSupported.equals(other.subjectTypesSupported)) {
                return false;
            }

            if (this.tokenEndpointAuthMethodsSupported == null) {
                if (other.tokenEndpointAuthMethodsSupported != null) {
                    return false;
                }
            } else if (!this.tokenEndpointAuthMethodsSupported.equals(other.tokenEndpointAuthMethodsSupported)) {
                return false;
            }

            if (this.tokenEndpointAuthSigningAlgValuesSupported == null) {
                if (other.tokenEndpointAuthSigningAlgValuesSupported != null) {
                    return false;
                }
            } else if (!this.tokenEndpointAuthSigningAlgValuesSupported.equals(other.tokenEndpointAuthSigningAlgValuesSupported)) {
                return false;
            }

            if (this.tokenEndpointUri == null) {
                if (other.tokenEndpointUri != null) {
                    return false;
                }
            } else if (!this.tokenEndpointUri.equals(other.tokenEndpointUri)) {
                return false;
            }

            if (this.uiLocalesSupported == null) {
                if (other.uiLocalesSupported != null) {
                    return false;
                }
            } else if (!this.uiLocalesSupported.equals(other.uiLocalesSupported)) {
                return false;
            }

            if (this.userInfoUri == null) {
                if (other.userInfoUri != null) {
                    return false;
                }
            } else if (!this.userInfoUri.equals(other.userInfoUri)) {
                return false;
            }

            if (this.userinfoEncryptionAlgValuesSupported == null) {
                if (other.userinfoEncryptionAlgValuesSupported != null) {
                    return false;
                }
            } else if (!this.userinfoEncryptionAlgValuesSupported.equals(other.userinfoEncryptionAlgValuesSupported)) {
                return false;
            }

            if (this.userinfoEncryptionEncValuesSupported == null) {
                if (other.userinfoEncryptionEncValuesSupported != null) {
                    return false;
                }
            } else if (!this.userinfoEncryptionEncValuesSupported.equals(other.userinfoEncryptionEncValuesSupported)) {
                return false;
            }

            if (this.userinfoSigningAlgValuesSupported == null) {
                return other.userinfoSigningAlgValuesSupported == null;
            } else return this.userinfoSigningAlgValuesSupported.equals(other.userinfoSigningAlgValuesSupported);
        }
    }

    public enum UserInfoTokenMethod {
        HEADER,
        FORM,
        QUERY;

        UserInfoTokenMethod() {
        }
    }
}

