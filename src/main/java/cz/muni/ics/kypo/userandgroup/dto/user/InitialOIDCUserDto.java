package cz.muni.ics.kypo.userandgroup.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.junit.Ignore;

@ApiModel(value = "InitialOIDCUserDto", description = "Basic information about initial OIDC user.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitialOIDCUserDto {
    @ApiModelProperty(value = "Main identifier of the user.", example = "441048@muni.cz", position = 1)
    private String name;
    @ApiModelProperty(value = "Password of a user.", example = "batman")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "InitialOIDCUsersDto{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
