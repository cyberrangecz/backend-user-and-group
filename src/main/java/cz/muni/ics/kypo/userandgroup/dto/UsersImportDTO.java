package cz.muni.ics.kypo.userandgroup.dto;

import com.google.common.base.Objects;
import cz.muni.ics.kypo.userandgroup.dto.user.UserImportDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "UsersImportDTO", description = "Information that are necessary to import users.")
public class UsersImportDTO {

    @ApiModelProperty(value = "List of the user to be imported.")
    @Valid
    private List<UserImportDTO> users = new ArrayList<>();
    @ApiModelProperty(value = "New group to which the user will be assigned.")
    private String groupName;

    public List<UserImportDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserImportDTO> users) {
        this.users = users;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsersImportDTO)) return false;
        UsersImportDTO that = (UsersImportDTO) o;
        return Objects.equal(users, that.users) && Objects.equal(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(users, groupName);
    }

    @Override
    public String toString() {
        return "UsersImportDTO{" +
                "users=" + users +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
