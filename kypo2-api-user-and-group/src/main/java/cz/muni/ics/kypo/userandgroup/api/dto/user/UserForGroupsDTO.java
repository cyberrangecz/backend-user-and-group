package cz.muni.ics.kypo.userandgroup.api.dto.user;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
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

    @ApiModelProperty(value = "Main identifiers of the user.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Full name of the user including his titles.", example = "Peter Novak.")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @ApiModelProperty(value = "Login of the user.", example = "michaelsmith")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @ApiModelProperty(value = "Email of the user.", example = "michaelsmith@mail.muni.cz.")
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @ApiModelProperty(value = "First name of a user.", example = "Pavel")
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @ApiModelProperty(value = "Surname of a user.", example = "Seda")
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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
                Objects.equals(getMail(), that.getMail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFullName(), getGivenName(), getFamilyName(), getLogin(), getMail());
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
                '}';
    }
}
