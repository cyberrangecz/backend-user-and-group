package cz.cyberrange.platform.userandgroup.api.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/** Holds the login name and password of one entry in the initial set of OIDC users. */
@Schema(
    name = "InitialOIDCUserDto",
    description = "Login name and password of one user in the initial OIDC user file.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitialOIDCUserDto {
  @Schema(example = "441048@example.cz")
  private String name;

  @Schema(example = "batman")
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
    return "InitialOIDCUsersDto{" + "name='" + name + '\'' + ", password='" + password + '\'' + '}';
  }
}
