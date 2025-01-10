package cz.cyberrange.platform.userandgroup.api.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;

/**
 * Encapsulates information about a user.
 */
@ApiModel(value = "UserImportDTO", description = "Information that are necessary to import a user.")
public class UserImportDTO {
    @ApiModelProperty(name = "sub", value = "User sub.", example = "johndoe@mail.example.cz")
    @NotEmpty(message = "{user.sub.NotEmpty.message}")
    private String sub;
    @ApiModelProperty(name = "iss", value = "OIDC issuer for that user record.", example = "https://oidc.provider.cz/oidc")
    @NotEmpty(message = "{user.iss.NotEmpty.message}")
    private String iss;
    @ApiModelProperty(name = "full_name", value = "User full name.", example = "John Doe")
    private String fullName;
    @ApiModelProperty(name = "given_name", value = "User given name.", example = "John")
    private String givenName;
    @ApiModelProperty(name = "family_name", value = "User family name.", example = "Doe")
    private String familyName;

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserImportDTO)) return false;
        UserImportDTO that = (UserImportDTO) o;
        return getSub().equals(that.getSub()) &&
                getIss().equals(that.getIss());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSub(), getIss());
    }

    @Override
    public String toString() {
        return "UserCreateDto{" +
                "sub='" + sub + '\'' +
                ", fullName='" + fullName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", iss='" + iss + '\'' +
                '}';
    }
}
