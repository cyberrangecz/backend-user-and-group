package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserAndGroupStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;


public class GroupDTO {


    private Long id;
    private String name;
    private String description;
    private Set<RoleDTO> roles = new HashSet<>();
    private List<UserForGroupsDTO> users = new ArrayList<>();
    private Source source;
    private boolean canBeDeleted;

    public void convertExternalIdToSource(Long externalId) {
        if (externalId == null) {
            this.source = Source.INTERNAL;
        }else {
            this.source = Source.PERUN;
        }
    }

    public void convertStatusToCanBeDeleted(UserAndGroupStatusDTO status) {
        if (status.equals(UserAndGroupStatusDTO.DELETED)) {
            this.canBeDeleted = true;
        }
        if (status.equals(UserAndGroupStatusDTO.VALID)) {
            this.canBeDeleted = false;
        }
    }

    @ApiModelProperty(value = "Main identifier of group.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "A name of the group.", example = "Main group of organizers")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(value = "A description of the group.", example = "Organizers group for training run in June.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "List of users in group.")
    public List<UserForGroupsDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserForGroupsDTO> users) {
        this.users = users;
    }

    @ApiModelProperty(value = "Set of roles of  group.")
    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    @ApiModelProperty(value = "Source of the group, whether its internal or from perun.", example = "Internal")
    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @ApiModelProperty(value = "Sign if the group can be deleted.", example = "false")
    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDTO groupDTO = (GroupDTO) o;
        return Objects.equals(getId(), groupDTO.getId()) &&
                Objects.equals(getName(), groupDTO.getName()) &&
                Objects.equals(getDescription(), groupDTO.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription());
    }

    @Override
    public String toString() {
        return "GroupDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
