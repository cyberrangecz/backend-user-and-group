package cz.muni.ics.kypo.userandgroup.api.dto.enums;

public enum AuthenticatedUserOIDCItems {

    ACTIVE("active", "", "true"),
    SCOPE("scope", "", "openid profile email"),
    EXPIRES_AT("expires_at", "", "2019-06-11T15:01:57+0000"),
    EXP("exp", "", "1560265317"),
    SUB("sub", "", "441048@muni.cz"),
    USER_ID("user_id", "", "152488"),
    /**
     * The example for client_id was obfuscated.
     */
    CLIENT_ID("client_id", "", "b53f2660-8fa0-4d32-94e4-23a59d7e7888"),
    TOKEN_TYPE("token_type", "", "Bearer"),
    ISS("iss", "", "https://oidc.muni.cz/oidc/"),
    NAME("name", "", "Mgr. Ing. Pavel Šeda"),
    PREFERRED_USERNAME("preferred_username", "", "441048"),
    GIVEN_NAME("given_name", "", "Pavel"),
    FAMILY_NAME("family_name", "", "Šeda"),
    EMAIL("email", "", "441048@mail.muni.cz"),
    PICTURE("picture", "", "byte-stream");

    private String name;
    private String description;
    private String example;

    AuthenticatedUserOIDCItems(String name, String description, String example) {
        this.name = name;
        this.description = description;
        this.example = example;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

}
