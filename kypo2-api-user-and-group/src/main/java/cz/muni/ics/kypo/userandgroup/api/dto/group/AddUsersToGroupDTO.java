package cz.muni.ics.kypo.userandgroup.api.dto.group;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jan Duda
 * @author Pavel Seda
 */
public class AddUsersToGroupDTO {

    private List<Long> idsOfUsersToBeAdd = new ArrayList<>();
    private List<Long> idsOfGroupsOfImportedUsers = new ArrayList<>();

    @ApiModelProperty(value = "Main identifiers of users to be added to group.", example = "[1,2]")
    public List<Long> getIdsOfUsersToBeAdd() {
        return idsOfUsersToBeAdd;
    }

    public void setIdsOfUsersToBeAdd(List<Long> idsOfUsersToBeAdd) {
        this.idsOfUsersToBeAdd = idsOfUsersToBeAdd;
    }

    @ApiModelProperty(value = "Main group identifiers whose users are to be imported into a group.", example = "[1,2]")
    public List<Long> getIdsOfGroupsOfImportedUsers() {
        return idsOfGroupsOfImportedUsers;
    }

    public void setIdsOfGroupsOfImportedUsers(List<Long> idsOfGroupsOfImportedUsers) {
        this.idsOfGroupsOfImportedUsers = idsOfGroupsOfImportedUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddUsersToGroupDTO that = (AddUsersToGroupDTO) o;
        return Objects.equals(getIdsOfUsersToBeAdd(), that.getIdsOfUsersToBeAdd()) &&
                Objects.equals(getIdsOfGroupsOfImportedUsers(), that.getIdsOfGroupsOfImportedUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdsOfUsersToBeAdd(), getIdsOfGroupsOfImportedUsers());
    }

    @Override
    public String toString() {
        return "AddUsersToGroupDTO{" +
                ", idsOfUsersToBeAdd=" + idsOfUsersToBeAdd +
                ", idsOfGroupsOfImportedUsers=" + idsOfGroupsOfImportedUsers +
                '}';
    }
}
