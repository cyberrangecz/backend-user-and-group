package cz.muni.ics.kypo.userandgroup.api.dto.group;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class UpdateGroupDTO {

    @NotNull(message = "{updateGroupDto.id.NotNull.message}")
    private Long id;
    @NotEmpty(message = "{updateGroupDto.name.NotEmpty.message}")
    private String name;
    @NotEmpty(message = "{updateGroupDto.description.NotEmpty.message}")
    private String description;

    @ApiModelProperty(value = "Main identifier of group.", required = true, example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "A name of the group.", required = true, example = "Main group.")
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
        return "UpdateGroupDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
