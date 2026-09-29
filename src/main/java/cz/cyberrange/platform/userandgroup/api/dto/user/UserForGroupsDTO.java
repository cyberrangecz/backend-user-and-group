package cz.cyberrange.platform.userandgroup.api.dto.user;

import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Carries a user's identity and contact details as listed within a group. Included in {@link
 * GroupDTO} and returned by the endpoint that lists the users of given groups.
 */
@Schema(
    name = "UserForGroupsDTO",
    description = "A user's identity and contact details as listed in a group.")
public class UserForGroupsDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(
      description = "Subject identifier the OIDC provider issued for the user.",
      example = "mail@mail.example.cz")
  private String sub;

  @Schema(
      description = "URL of the OIDC provider that issued the subject.",
      example = "https://oidc.provider.cz")
  private String iss;

  @Schema(example = "John Doe")
  private String fullName;

  @Schema(example = "John")
  private String givenName;

  @Schema(example = "Doe")
  private String familyName;

  @Schema(example = "johndoe@mail.example.cz")
  private String mail;

  @Schema(
      type = "string",
      format = "byte",
      description = "Avatar bytes, base64 encoded in JSON.",
      example =
          "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
  private byte[] picture;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public String getMail() {
    return mail;
  }

  public void setMail(String mail) {
    this.mail = mail;
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
  public boolean equals(Object object) {
    if (!(object instanceof UserForGroupsDTO)) {
      return false;
    }
    UserForGroupsDTO that = (UserForGroupsDTO) object;
    return Objects.equals(getId(), that.getId())
        && Objects.equals(getFullName(), that.getFullName())
        && Objects.equals(getGivenName(), that.getGivenName())
        && Objects.equals(getFamilyName(), that.getFamilyName())
        && Objects.equals(getSub(), that.getSub())
        && Objects.equals(getMail(), that.getMail())
        && Objects.equals(getIss(), that.getIss());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getId(), getFullName(), getGivenName(), getFamilyName(), getSub(), getMail(), getIss());
  }

  @Override
  public String toString() {
    return "UserForGroupsDTO{"
        + "id="
        + id
        + ", fullName='"
        + fullName
        + '\''
        + ", givenName='"
        + givenName
        + '\''
        + ", familyName='"
        + familyName
        + '\''
        + ", sub='"
        + sub
        + '\''
        + ", mail='"
        + mail
        + '\''
        + ", iss='"
        + iss
        + '\''
        + '}';
  }
}
