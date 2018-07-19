package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.model.UserAndGroupStatus;
import cz.muni.ics.kypo.userandgroup.api.dto.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;

import java.util.List;
import java.util.Set;


public class GroupDTO {

    private Long id;

    private String name;

    private String description;

    private Set<RoleDTO> roles;

    private List<UserForGroupsDTO> members;

    private Source source;

    private boolean canBeDeleted;

    public void convertExternalIdToSource(Long externalId) {
        if (externalId == null) {
            this.source = Source.INTERNAL;
        }else {
            this.source = Source.PERUN;
        }
    }
    public void convertStatusToCanBeDeleted(UserAndGroupStatus status) {
        if (status.equals(UserAndGroupStatus.DELETED)) {
            this.canBeDeleted = true;
        }
        if (status.equals(UserAndGroupStatus.VALID)) {
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

    public List<UserForGroupsDTO> getMembers() {
        return members;
    }

    public void setMembers(List<UserForGroupsDTO> members) {
        this.members = members;
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
}
