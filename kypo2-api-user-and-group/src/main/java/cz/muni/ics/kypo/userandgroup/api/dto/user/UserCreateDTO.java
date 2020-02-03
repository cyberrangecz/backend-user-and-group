package cz.muni.ics.kypo.userandgroup.api.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about a user.
 */
@ApiModel(value = "UserCreateDto", description = "Information that are necessary to create a user.")
public class UserCreateDTO {
    @ApiModelProperty(name = "login", value = "User login.", example = "seda@ics.muni.cz")
    private String login;
    @ApiModelProperty(name = "full_name", value = "User full name.", example = "Pavel Seda")
    private String fullName;
    @ApiModelProperty(name = "given_name", value = "User given name.", example = "Pavel")
    private String givenName;
    @ApiModelProperty(name = "family_name", value = "User family name.", example = "Seda")
    private String familyName;
    @ApiModelProperty(name = "external_id", value = "User external id.", example = "1")
    private Long externalId;
    @ApiModelProperty(name = "mail", value = "User external id.", example = "seda@ics.muni.cz")
    private String mail;
    @ApiModelProperty(name = "iss", value = "OIDC issuer for that user record.", example = "https://oidc.muni.cz/oidc")
    private String iss;
    @ApiModelProperty(name = "picture", value = "User profile picture.")
    private byte[] picture;

    public UserCreateDTO() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCreateDTO)) return false;
        UserCreateDTO that = (UserCreateDTO) o;
        return getLogin().equals(that.getLogin()) &&
                getIss().equals(that.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLogin(), getIss());
    }

    @Override
    public String toString() {
        return "UserCreateDto{" +
                "login='" + login + '\'' +
                ", fullName='" + fullName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", externalId=" + externalId +
                ", mail='" + mail + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}
