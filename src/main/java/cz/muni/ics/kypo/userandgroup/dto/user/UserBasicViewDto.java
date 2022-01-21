package cz.muni.ics.kypo.userandgroup.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(value = "UserBasicViewDto", description = "Basic information about users.")
public class UserBasicViewDto {

    @ApiModelProperty(value = "Main identifier of the user.", example = "1", position = 1)
    private Long id;
    @ApiModelProperty(value = "Sub of the user.", example = "mail@mail.muni.cz")
    private String sub;
    @ApiModelProperty(value = "Issuer of a user.", example = "https://oidc.muni.cz")
    private String iss;
    @ApiModelProperty(value = "Full name of the user.", example = "John Doe")
    private String fullName;
    @ApiModelProperty(value = "Email of the user.", example = "johndoe@mail.muni.cz")
    private String mail;
    @ApiModelProperty(value = "First name of a user.", example = "John")
    private String givenName;
    @ApiModelProperty(value = "Surname of a user.", example = "Doe")
    private String familyName;
    @ApiModelProperty(value = "Identicon of a user.", example = "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
    private byte[] picture;

    /**
     * Gets theID of the user.
     *
     * @return the ID of the user.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the user.
     *
     * @param id the ID of the user.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the full name of the user.
     *
     * @return the full name of the user.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     *
     * @param fullName the full name of the user.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the sub of the user.
     *
     * @return the sub of the user.
     */
    public String getSub() {
        return sub;
    }

    /**
     * Sets the sub of the user.
     *
     * @param sub the sub of the user.
     */
    public void setSub(String sub) {
        this.sub = sub;
    }

    /**
     * Gets the mail of the user.
     *
     * @return the mail of the user.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets the mail of the user.
     *
     * @param mail the mail of the user.
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Gets the given name of the user.
     *
     * @return the given name of the user.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets the given name of the user.
     *
     * @param givenName the given name of the user.
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Gets the family name of the user.
     *
     * @return the family name of the user.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Sets the family name of the user.
     *
     * @param familyName the family name of the user.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     * Gets the issuer - URI of the oidc provider of the user.
     *
     * @return issuer - URI of the oidc provider.
     */
    public String getIss() {
        return iss;
    }

    /**
     * Sets the issuer - URI of the oidc provider of the user.
     *
     * @param iss the family name of the user.
     */
    public void setIss(String iss) {
        this.iss = iss;
    }

    /**
     * Gets the identicon of the user encoded in base64.
     *
     * @return identicon of the user.
     */
    public byte[] getPicture() {
        return picture;
    }

    /**
     * Sets the identicon of the user encoded in base64.
     *
     * @param picture encoded identicon of the user.
     */
    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserBasicViewDto)) return false;
        UserBasicViewDto userDTO = (UserBasicViewDto) object;
        return Objects.equals(getId(), userDTO.getId()) &&
                Objects.equals(getFullName(), userDTO.getFullName()) &&
                Objects.equals(getSub(), userDTO.getSub()) &&
                Objects.equals(getMail(), userDTO.getMail()) &&
                Objects.equals(getGivenName(), userDTO.getGivenName()) &&
                Objects.equals(getFamilyName(), userDTO.getFamilyName()) &&
                Objects.equals(getIss(), userDTO.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFullName(), getSub(), getMail(), getGivenName(), getFamilyName(), getIss());
    }

    @Override
    public String toString() {
        return "UserBasicViewDto{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", sub='" + sub + '\'' +
                ", mail='" + mail + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}
