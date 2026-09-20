package cz.cyberrange.platform.userandgroup.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Carries the information obtained from an OIDC provider about an authenticating user, used to
 * create that user on first sign-in or refresh their stored details on later ones.
 */
@Schema(
    name = "UserCreateDto",
    description = "Details an OIDC provider reports about a user signing in.")
public class UserCreateDTO {
  @Schema(
      name = "sub",
      description = "Subject identifier the OIDC provider issued for the user.",
      example = "johndoe@mail.example.cz")
  private String sub;

  @Schema(name = "full_name", example = "John Doe")
  private String fullName;

  @Schema(name = "given_name", example = "John")
  private String givenName;

  @Schema(name = "family_name", example = "Doe")
  private String familyName;

  @Schema(
      name = "external_id",
      description = "Id the user carries in the source it was imported from.",
      example = "1")
  private Long externalId;

  @Schema(name = "mail", example = "johndoe@mail.example.cz")
  private String mail;

  @Schema(
      name = "iss",
      description = "URL of the OIDC provider that issued the subject.",
      example = "https://oidc.provider.cz/oidc")
  private String iss;

  @Schema(
      name = "picture",
      description = "Ignored; a generated identicon is stored for a new user instead.")
  private byte[] picture;

  public UserCreateDTO() {}

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
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
    return getSub().equals(that.getSub()) && getIss().equals(that.getIss());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSub(), getIss());
  }

  @Override
  public String toString() {
    return "UserCreateDto{"
        + "sub='"
        + sub
        + '\''
        + ", fullName='"
        + fullName
        + '\''
        + ", givenName='"
        + givenName
        + '\''
        + ", familyName='"
        + familyName
        + '\''
        + ", externalId="
        + externalId
        + ", mail='"
        + mail
        + '\''
        + ", iss='"
        + iss
        + '\''
        + '}';
  }
}
