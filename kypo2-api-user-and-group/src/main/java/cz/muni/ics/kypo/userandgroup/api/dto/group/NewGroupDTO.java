package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Encapsulates information about a new group to be created in the database.
 */
@ApiModel(value = "NewGroupDTO", description = "Data that need to be provided to create a new group.")
public class NewGroupDTO {

    @ApiModelProperty(value = "A name of the group.", required = true, example = "Main group")
    @NotEmpty(message = "{group.name.NotEmpty.message}")
    private String name;
    @ApiModelProperty(value = "A description of the group.", required = true, example = "Group for main users.")
    @NotEmpty(message = "{group.description.NotEmpty.message}")
    private String description;
    @ApiModelProperty(value = "Time until the group is valid.", example = "2019-11-20T10:28:02.727Z")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;
    @ApiModelProperty(value = "List of users who is assigned to group.")
    private Set<UserForGroupsDTO> users = new HashSet<>();
    @ApiModelProperty(value = "Main identifiers of group.", example = "[1]")
    private List<Long> groupIdsOfImportedUsers = new ArrayList<>();

    /**
     * Gets the name of the group.
     *
     * @return the name of the group.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the group.
     *
     * @param name the name of the group.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the group.
     *
     * @return the description of the group.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description of the group.
     *
     * @param description the description of the group.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the {@link UserForGroupsDTO} of the group.
     *
     * @return the {@link UserForGroupsDTO} of the group.
     */
    public Set<UserForGroupsDTO> getUsers() {
        return users;
    }

    /**
     * Sets the {@link UserForGroupsDTO} of the group.
     *
     * @param users the {@link UserForGroupsDTO} of the group.
     */
    public void setUsers(Set<UserForGroupsDTO> users) {
        this.users = users;
    }

    /**
     * Gets a list of IDs of groups from which import users.
     *
     * @return the list of IDs of groups from which import users.
     */
    public List<Long> getGroupIdsOfImportedUsers() {
        return groupIdsOfImportedUsers;
    }

    /**
     * Sets a list of IDs of groups from which import users.
     *
     * @param groupIdsOfImportedUsers the group ids of imported users
     */
    public void setGroupIdsOfImportedUsers(List<Long> groupIdsOfImportedUsers) {
        this.groupIdsOfImportedUsers = groupIdsOfImportedUsers;
    }

    /**
     * Gets expiration date of the group.
     *
     * @return the expiration date of the group
     */
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets expiration date of the group.
     *
     * @param expirationDate the expiration date of the group.
     */
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NewGroupDTO)) return false;
        NewGroupDTO that = (NewGroupDTO) object;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "NewGroupDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
