package cz.muni.ics.kypo.userandgroup.api.dto.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jan Duda & Pavel Seda
 */
public class AddUsersToGroupDTO {

    private Long groupId;
    private List<Long> idsOfUsersToBeAdd = new ArrayList<>();
    private List<Long> idsOfGroupsOfImportedUsers = new ArrayList<>();

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<Long> getIdsOfUsersToBeAdd() {
        return idsOfUsersToBeAdd;
    }

    public void setIdsOfUsersToBeAdd(List<Long> idsOfUsersToBeAdd) {
        this.idsOfUsersToBeAdd = idsOfUsersToBeAdd;
    }

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
        return Objects.equals(getGroupId(), that.getGroupId()) &&
                Objects.equals(getIdsOfUsersToBeAdd(), that.getIdsOfUsersToBeAdd()) &&
                Objects.equals(getIdsOfGroupsOfImportedUsers(), that.getIdsOfGroupsOfImportedUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getIdsOfUsersToBeAdd(), getIdsOfGroupsOfImportedUsers());
    }

    @Override
    public String toString() {
        return "AddUsersToGroupDTO{" +
                "groupId=" + groupId +
                ", idsOfUsersToBeAdd=" + idsOfUsersToBeAdd +
                ", idsOfGroupsOfImportedUsers=" + idsOfGroupsOfImportedUsers +
                '}';
    }
}
