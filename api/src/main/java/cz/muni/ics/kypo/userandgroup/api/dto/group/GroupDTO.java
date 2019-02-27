package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserAndGroupStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

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
