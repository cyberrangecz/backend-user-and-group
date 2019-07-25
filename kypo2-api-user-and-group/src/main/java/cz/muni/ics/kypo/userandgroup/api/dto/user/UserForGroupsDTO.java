package cz.muni.ics.kypo.userandgroup.api.dto.user;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates information about a user used in {@link cz.muni.ics.kypo.userandgroup.api.dto.group.GroupDTO}.
 *
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class UserForGroupsDTO {

    private Long id;
    private String fullName;
    private String givenName;
    private String familyName;
    private String login;
    private String mail;
    private String iss;

    /**
     * Gets the ID of the user.
     *
     * @return the ID of the user.
     */
    @ApiModelProperty(value = "Main identifiers of the user.", example = "1")
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
    @ApiModelProperty(value = "Full name of the user including his titles.", example = "Peter Novak.")
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
    @ApiModelProperty(value = "Login of the user.", example = "{\"sub\":\"michaelsmith\",  \"iss\": \"https://oidc.muni.cz/oidc/\"}")
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
    @ApiModelProperty(value = "Email of the user.", example = "michaelsmith@mail.muni.cz.")
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
    @ApiModelProperty(value = "First name of a user.", example = "Pavel")
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
    @ApiModelProperty(value = "Surname of a user.", example = "Seda")
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserForGroupsDTO)) {
            return false;
        }
        UserForGroupsDTO that = (UserForGroupsDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getFullName(), that.getFullName()) &&
                Objects.equals(getGivenName(), that.getGivenName()) &&
                Objects.equals(getFamilyName(), that.getFamilyName()) &&
                Objects.equals(getLogin(), that.getLogin()) &&
                Objects.equals(getMail(), that.getMail()) &&
                Objects.equals(getIss(), that.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFullName(), getGivenName(), getFamilyName(), getLogin(), getMail(), getIss());
    }

    @Override
    public String toString() {
        return "UserForGroupsDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", login='" + login + '\'' +
                ", mail='" + mail + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}
