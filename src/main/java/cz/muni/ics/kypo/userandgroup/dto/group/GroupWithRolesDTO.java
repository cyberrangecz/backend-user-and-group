package cz.muni.ics.kypo.userandgroup.dto.group;

import cz.muni.ics.kypo.userandgroup.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.userandgroup.dto.role.RoleDTO;
import cz.muni.ics.kypo.userandgroup.enums.dto.Source;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * GroupDTO encapsulates information about a group including its roles.
 */
@ApiModel(value = "GroupWithRolesDto", description = "The information about a group including its roles.")
public class GroupWithRolesDTO {

    @ApiModelProperty(value = "Main identifier of group.", example = "1", position = 1)
    private Long id;
    @ApiModelProperty(value = "A name of the group.", example = "Main group of organizers")
    private String name;
    @ApiModelProperty(value = "A description of the group.", example = "Organizers group for training run in June.")
    private String description;
    @ApiModelProperty(value = "Set of roles of  group.")
    private Set<RoleDTO> roles = new HashSet<>();
    @ApiModelProperty(value = "Source of the group, whether its internal or from perun.", example = "Internal")
    private Source source;
    @ApiModelProperty(value = "Sign if the group can be deleted.", example = "false")
    private boolean canBeDeleted = true;
    @ApiModelProperty(value = "Time until the group is valid.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;

    public GroupWithRolesDTO() {
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

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "GroupWithRolesDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", source=" + source +
                ", canBeDeleted=" + canBeDeleted +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
