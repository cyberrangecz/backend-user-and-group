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
    private String login;
    private String mail;

    @ApiModelProperty(value = "Main identifiers of the user.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Full name of the user.", example = "Peter Novak.")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserForGroupsDTO that = (UserForGroupsDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getLogin(), that.getLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin());
    }

    @Override
    public String toString() {
        return "UserForGroupsDTO{" +
                "id=" + id +
                ", login='" + login + '\'' +
                '}';
    }
}
