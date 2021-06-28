package cz.muni.ics.kypo.userandgroup.api.dto.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object used for adding users to a group.
 */
@ApiModel(value = "AddUsersToGroupDTO", description = "Data that need to be provided to add users to a group.")
public class AddUsersToGroupDTO {

    @ApiModelProperty(value = "Main identifiers of users to be added to group.", example = "[1,2]")
    private List<Long> idsOfUsersToBeAdd = new ArrayList<>();
    @ApiModelProperty(value = "Main group identifiers whose users are to be imported into a group.", example = "[1,2]")
    private List<Long> idsOfGroupsOfImportedUsers = new ArrayList<>();

    /**
     * Gets IDs of users to be added to the group.
     *
     * @return list of IDs of users to be added to the group
     */
    public List<Long> getIdsOfUsersToBeAdd() {
        return idsOfUsersToBeAdd;
    }

    /**
     * Sets IDs of users to be added to the group.
     *
     * @param idsOfUsersToBeAdd the list of IDs of users to be added to the group
     */
    public void setIdsOfUsersToBeAdd(List<Long> idsOfUsersToBeAdd) {
        this.idsOfUsersToBeAdd = idsOfUsersToBeAdd;
    }

    /**
     * Gets IDs of groups from which import users.
     *
     * @return the list of IDs of groups from which import users
     */
    public List<Long> getIdsOfGroupsOfImportedUsers() {
        return idsOfGroupsOfImportedUsers;
    }

    /**
     * Sets list of IDs of groups from which import users.
     *
     * @param idsOfGroupsOfImportedUsers the ids of groups of imported users
     */
    public void setIdsOfGroupsOfImportedUsers(List<Long> idsOfGroupsOfImportedUsers) {
        this.idsOfGroupsOfImportedUsers = idsOfGroupsOfImportedUsers;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AddUsersToGroupDTO)) return false;
        AddUsersToGroupDTO that = (AddUsersToGroupDTO) object;
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
