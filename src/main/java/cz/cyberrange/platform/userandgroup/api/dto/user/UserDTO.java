package cz.cyberrange.platform.userandgroup.api.dto.user;

import cz.cyberrange.platform.userandgroup.api.dto.role.RoleDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Carries the detailed information about a user, including the roles granted through the user's
 * groups. Returned by the endpoints that expose a single user or a page of users.
 */
@Schema(
    name = "UserDTO",
    description = "A user, with the roles granted through the groups it belongs to.")
public class UserDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(
      description = "Subject identifier the OIDC provider issued for the user.",
      example = "mail@mail.example.cz")
  private String sub;

  @Schema(
      description = "URL of the OIDC provider that issued the subject.",
      example = "https://oidc.example.cz")
  private String iss;

  @Schema(example = "John Doe")
  private String fullName;

  @Schema(example = "johndoe@mail.example.cz")
  private String mail;

  @Schema(example = "John")
  private String givenName;

  @Schema(example = "Doe")
  private String familyName;

  private Set<RoleDTO> roles = new HashSet<>();

  @Schema(
      description = "Avatar bytes, base64 encoded in JSON.",
      example =
          "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
  private byte[] picture;

  public UserDTO() {
    // no-args constructor
  }

  public UserDTO(Long id, String fullName, String sub, String mail, String iss) {
    this.id = id;
    this.fullName = fullName;
    this.sub = sub;
    this.mail = mail;
    this.iss = iss;
  }

  public UserDTO(
      Long id, String fullName, String sub, String mail, Set<RoleDTO> roles, String iss) {
    this.id = id;
    this.fullName = fullName;
    this.sub = sub;
    this.mail = mail;
    this.roles = roles;
    this.iss = iss;
  }

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

  public Set<RoleDTO> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleDTO> roles) {
    this.roles = roles;
  }

  public void addRole(RoleDTO roleDTO) {
    this.roles.add(roleDTO);
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
    if (!(object instanceof UserDTO)) return false;
    UserDTO userDTO = (UserDTO) object;
    return Objects.equals(getId(), userDTO.getId())
        && Objects.equals(getFullName(), userDTO.getFullName())
        && Objects.equals(getSub(), userDTO.getSub())
        && Objects.equals(getMail(), userDTO.getMail())
        && Objects.equals(getGivenName(), userDTO.getGivenName())
        && Objects.equals(getFamilyName(), userDTO.getFamilyName())
        && Objects.equals(getIss(), userDTO.getIss());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getId(), getFullName(), getSub(), getMail(), getGivenName(), getFamilyName(), getIss());
  }

  @Override
  public String toString() {
    return "UserDTO{"
        + "id="
        + id
        + ", fullName='"
        + fullName
        + '\''
        + ", sub='"
        + sub
        + '\''
        + ", mail='"
        + mail
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
