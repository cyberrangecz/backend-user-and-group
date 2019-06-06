package cz.muni.ics.kypo.userandgroup.api.dto.group;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.userandgroup.api.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.userandgroup.api.dto.Source;
import cz.muni.ics.kypo.userandgroup.api.dto.enums.UserAndGroupStatusDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.api.dto.user.UserForGroupsDTO;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class GroupDTO {
    private Long id;
    private String name;
    private String description;
    private Set<RoleDTO> roles = new HashSet<>();
    private Set<UserForGroupsDTO> users = new HashSet<>();
    private Source source;
    private boolean canBeDeleted = true;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;

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
    public Set<UserForGroupsDTO> getUsers() {
        return users;
    }

    public void setUsers(Set<UserForGroupsDTO> users) {
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

    @ApiModelProperty(value = "Time until the group is valid.", example = "2017-10-19 10:23:54+02")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GroupDTO)) return false;
        GroupDTO groupDTO = (GroupDTO) object;
        return Objects.equals(getId(), groupDTO.getId()) &&
                Objects.equals(getName(), groupDTO.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
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
