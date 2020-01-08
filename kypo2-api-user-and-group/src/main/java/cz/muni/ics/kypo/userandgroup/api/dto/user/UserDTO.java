package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates information about a user.
 *
 */
@ApiModel(value = "UserDTO",
        description = "Detailed information about user.")
public class UserDTO {

    @ApiModelProperty(value = "Main identifier of the user.", example = "1", position = 1)
    private Long id;
    @ApiModelProperty(value = "Full name of the user.", example = "Pavel Seda")
    private String fullName;
    @ApiModelProperty(value = "Login of the user.", example = "pavelseda")
    private String login;
    @ApiModelProperty(value = "Email of the user.", example = "pavelseda@mail.muni.cz")
    private String mail;
    @ApiModelProperty(value = "First name of a user.", example = "Pavel")
    private String givenName;
    @ApiModelProperty(value = "Surname of a user.", example = "Seda")
    private String familyName;
    @ApiModelProperty(value = "Roles of user assigned through the groups which user is in.")
    private Set<RoleDTO> roles = new HashSet<>();
    @ApiModelProperty(value = "Issuer of a user.", example = "https://oidc.muni.cz")
    private String iss;
    @ApiModelProperty(value = "Identicon of a user.", example = "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
    private byte[] picture;

    /**
     * Instantiates a new UserDTO.
     */
    public UserDTO() {
        // no-args constructor
    }

    /**
     * Instantiates a new UserDTO with attributes: id, fullName, login, mail, iss.
     *
     * @param id       the id
     * @param fullName the full name
     * @param login    the login
     * @param mail     the mail
     */
    public UserDTO(Long id, String fullName, String login, String mail, String iss) {
        this.id = id;
        this.fullName = fullName;
        this.login = login;
        this.mail = mail;
        this.iss = iss;
    }

    /**
     * Instantiates a new UserDTO with attributes: id, fullName, login, mail, roles, iss.
     *
     * @param id       the id
     * @param fullName the full name
     * @param login    the login
     * @param mail     the mail
     * @param roles    the roles
     */
    public UserDTO(Long id, String fullName, String login, String mail, Set<RoleDTO> roles, String iss) {
        this.id = id;
        this.fullName = fullName;
        this.login = login;
        this.mail = mail;
        this.roles = roles;
        this.iss = iss;
    }

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
     * Gets the login of the user.
     *
     * @return the login of the user.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the login of the user.
     *
     * @param login the login of the user.
     */
    public void setLogin(String login) {
        this.login = login;
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
     * Gets the roles of the user.
     *
     * @return the {@link RoleDTO} of the user.
     */
    public Set<RoleDTO> getRoles() {
        return roles;
    }

    /**
     * Sets the roles of the user.
     *
     * @param roles the {@link RoleDTO} of the user.
     */
    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    /**
     * Add the role to the list of roles of the user.
     *
     * @param roleDTO the {@link RoleDTO} to be added to the list of roles.
     */
    public void addRole(RoleDTO roleDTO) {
        this.roles.add(roleDTO);
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
        if (!(object instanceof UserDTO)) return false;
        UserDTO userDTO = (UserDTO) object;
        return Objects.equals(getId(), userDTO.getId()) &&
                Objects.equals(getFullName(), userDTO.getFullName()) &&
                Objects.equals(getLogin(), userDTO.getLogin()) &&
                Objects.equals(getMail(), userDTO.getMail()) &&
                Objects.equals(getGivenName(), userDTO.getGivenName()) &&
                Objects.equals(getFamilyName(), userDTO.getFamilyName()) &&
                Objects.equals(getIss(), userDTO.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFullName(), getLogin(), getMail(), getGivenName(), getFamilyName(), getIss());
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", login='" + login + '\'' +
                ", mail='" + mail + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}
