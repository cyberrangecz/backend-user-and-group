package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;

import java.util.List;

public class NewGroupDTO {

    private String name;

    private String description;

    private List<UserForGroupsDTO> members;

    private List<Long> groupIdsOfImportedMembers;

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

    public List<UserForGroupsDTO> getMembers() {
        return members;
    }

    public void setMembers(List<UserForGroupsDTO> members) {
        this.members = members;
    }

    public List<Long> getGroupIdsOfImportedMembers() {
        return groupIdsOfImportedMembers;
    }

    public void setGroupIdsOfImportedMembers(List<Long> groupIdsOfImportedMembers) {
        groupIdsOfImportedMembers = groupIdsOfImportedMembers;
    }
}
