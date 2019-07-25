package cz.muni.ics.kypo.userandgroup.api.dto.user;

import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates information about a user.
 *
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
    private String iss;

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
    @ApiModelProperty(value = "Main identifier of the user.", example = "1")
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
    @ApiModelProperty(value = "Full name of the user.", example = "Michael Smith")
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
    @ApiModelProperty(value = "Login of the user.", example = "michaelsmith")
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
     * Gets the roles of the user.
     *
     * @return the {@link RoleDTO} of the user.
     */
    @ApiModelProperty(value = "Roles of user assigned through the groups which user is in.")
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
