package cz.cyberrange.platform.userandgroup.security.enums;

public enum OIDCItems {

    SUB("sub", "Subject - Identifier for the End-User at the Issuer.", "999999@example.cz"),
    ISS("iss", "Specifies the issuing authority.", "https://oidc.provider.cz/oidc/"),
    PREFERRED_USERNAME("preferred_username", "Shorthand name by which the End-User wishes to be referred to at the RP, such as janedoe or j.doe.", "999999"),
    GIVEN_NAME("given_name", "Given name(s) or first name(s) of the End-User. Note that in some cultures, people can have multiple given names; all can be present, with the names being separated by space characters.", "John"),
    FAMILY_NAME("family_name", "Surname(s) or last name(s) of the End-User. Note that in some cultures, people can have multiple family names or no family name; all can be present, with the names being separated by space characters.", "Doe"),
    NAME("name", "End-User's full name in displayable form including all name parts, possibly including titles and suffixes, ordered according to the End-User's locale and preferences.", "Mgr. Ing. John Doe"),
    EMAIL("email", "End-User's preferred e-mail address.", "999999@mail.example.cz"),
    SCOPE("scope", "A space-separated list of scopes.", "openid profile email"),
    SCP("scp", "A space-separated list of scopes.", "openid profile email"),
    /**
     * The example for client_id was obfuscated.
     */
    CLIENT_ID("client_id", "The Application (client) ID that is assigned to the registrated app.", "b53f2660-8fa0-4d32-94e4-23a59d7e7888"),
    KID("kid", "The kid (key ID) claim is an optional header claim, used to specify the key for validating the signature.", "sxcxzczxaqwEQW5saqQwq645"),
    EXPIRES_AT("expires_at", "Expiration time on or after which the ID Token MUST NOT be accepted for processing. ", "2019-06-11T15:01:57+0000"),
    EXP("exp", "Expiration time on or after which the ID Token MUST NOT be accepted for processing. ", "1560265317"),
    TOKEN_TYPE("token_type", "OAuth 2.0 Token Type value. The value MUST be Bearer or another token_type value that the Client has negotiated with the Authorization Server.", "Bearer");

    private final String name;
    private final String description;
    private final String example;

    OIDCItems(String name, String description, String example) {
        this.name = name;
        this.description = description;
        this.example = example;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }
}
