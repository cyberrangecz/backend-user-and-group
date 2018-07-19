package cz.muni.ics.kypo.userandgroup.rest.DTO.user;

import cz.muni.ics.kypo.userandgroup.rest.DTO.role.RoleDTO;

import java.util.Set;

public class UserInfoDTO {

    private Long id;

    private String fullName;

    private String login;

    private String mail;

    private Set<RoleDTO> roles;

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

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }
}
