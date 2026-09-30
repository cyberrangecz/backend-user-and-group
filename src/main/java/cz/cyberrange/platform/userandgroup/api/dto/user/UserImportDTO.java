package cz.cyberrange.platform.userandgroup.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;

/** Carries one user to import, as an entry of the users import endpoint's request body. */
@Schema(name = "UserImportDTO", description = "One user to create through the import endpoint.")
public class UserImportDTO {
  @Schema(
      name = "sub",
      description = "Subject identifier the OIDC provider issued for the user.",
      example = "johndoe@mail.example.cz",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "{user.sub.NotEmpty.message}")
  private String sub;

  @Schema(
      name = "iss",
      description = "URL of the OIDC provider that issued the subject.",
      example = "https://oidc.provider.cz/oidc",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "{user.iss.NotEmpty.message}")
  private String iss;

  @Schema(name = "full_name", example = "John Doe")
  private String fullName;

  @Schema(name = "given_name", example = "John")
  private String givenName;

  @Schema(name = "family_name", example = "Doe")
  private String familyName;

  public String getIss() {
    return iss;
  }

  public void setIss(String iss) {
    this.iss = iss;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserImportDTO)) return false;
    UserImportDTO that = (UserImportDTO) o;
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
        + ", iss='"
        + iss
        + '\''
        + '}';
  }
}
