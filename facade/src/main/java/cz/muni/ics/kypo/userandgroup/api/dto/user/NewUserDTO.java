package cz.muni.ics.kypo.userandgroup.api.dto.user;

import java.util.Objects;

public class NewUserDTO {

    private String fullName;
    private String login;
    private String mail;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void convertScreenNameToLogin(String screenName) {
        this.login = screenName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewUserDTO that = (NewUserDTO) o;
        return Objects.equals(getFullName(), that.getFullName()) &&
                Objects.equals(getLogin(), that.getLogin()) &&
                Objects.equals(getMail(), that.getMail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFullName(), getLogin(), getMail());
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "login='" + login + '\'' +
                '}';
    }
}
