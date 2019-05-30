package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Seda
 */
public class UserDTO {

    private Long id;
    private String fullName;
    private String login;
    private String mail;
    private String givenName;
    private String familyName;
    private Set<RoleDTO> roles = new HashSet<>();

    public UserDTO() {
        // no-args constructor
    }

    public UserDTO(Long id, String fullName, String login, String mail) {
        this.id = id;
        this.fullName = fullName;
        this.login = login;
        this.mail = mail;
    }

    public UserDTO(Long id, String fullName, String login, String mail, Set<RoleDTO> roles) {
        this.id = id;
        this.fullName = fullName;
        this.login = login;
        this.mail = mail;
        this.roles = roles;
    }

    @ApiModelProperty(value = "Main identifier of the user.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Full name of the user.", example = "Michael Smith")
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

    @ApiModelProperty(value = "Roles of user assigned through the groups which user is in.")
    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    public void addRole(RoleDTO roleDTO) {
        this.roles.add(roleDTO);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(getId(), userDTO.getId()) &&
                Objects.equals(getLogin(), userDTO.getLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin());
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
                '}';
    }
}
