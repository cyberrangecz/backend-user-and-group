package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewGroupDTO {

    private String name;

    private String description;

    private List<UserForGroupsDTO> users = new ArrayList<>();

    private List<Long> groupIdsOfImportedUsers = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UserForGroupsDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserForGroupsDTO> users) {
        this.users = users;
    }

    public List<Long> getGroupIdsOfImportedUsers() {
        return groupIdsOfImportedUsers;
    }

    public void setGroupIdsOfImportedUsers(List<Long> groupIdsOfImportedUsers) {
        this.groupIdsOfImportedUsers = groupIdsOfImportedUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDTO groupDTO = (GroupDTO) o;
        return Objects.equals(getName(), groupDTO.getName()) &&
                Objects.equals(getDescription(), groupDTO.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription());
    }

    @Override
    public String toString() {
        return "NewGroupDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
