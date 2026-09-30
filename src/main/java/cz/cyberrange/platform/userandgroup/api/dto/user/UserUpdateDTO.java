package cz.cyberrange.platform.userandgroup.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

/** Carries the fields accepted when updating a user's stored details. */
public class UserUpdateDTO {
  @Schema(
      name = "sub",
      description = "Subject identifier the OIDC provider issued for the user.",
      example = "johndoe@mail.example.cz")
  private String sub;

  @Schema(
      name = "iss",
      description = "URL of the OIDC provider that issued the subject.",
      example = "https://oidc.provider.cz/oidc")
  private String iss;

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

  @Schema(name = "picture")
  private byte[] picture;

  public UserUpdateDTO() {}

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
  public String toString() {
    return "UserUpdateDTO{"
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
