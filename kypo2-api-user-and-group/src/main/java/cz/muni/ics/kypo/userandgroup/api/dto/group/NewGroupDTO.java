package cz.muni.ics.kypo.userandgroup.api.dto.group;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.userandgroup.api.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class NewGroupDTO {

    @NotEmpty(message = "{newGroupDto.name.NotEmpty.message}")
    private String name;
    @NotEmpty(message = "{newGroupDto.description.NotEmpty.message}")
    private String description;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;

    private Set<UserForGroupsDTO> users = new HashSet<>();

    private List<Long> groupIdsOfImportedUsers = new ArrayList<>();

    @ApiModelProperty(value = "A name of the group.", required = true, example = "Main group")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "A description of the group.", required = true, example = "Group for main users.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "List of users who is assigned to group.")
    public Set<UserForGroupsDTO> getUsers() {
        return users;
    }

    public void setUsers(Set<UserForGroupsDTO> users) {
        this.users = users;
    }

    @ApiModelProperty(value = "Main identifiers of group.", example = "[1]")
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

    @ApiModelProperty(value = "Time until the group is valid.", example = "2019-11-20T10:28:02.727Z")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
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
