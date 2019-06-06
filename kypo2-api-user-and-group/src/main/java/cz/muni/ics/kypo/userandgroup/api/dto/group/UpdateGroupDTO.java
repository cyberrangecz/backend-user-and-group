package cz.muni.ics.kypo.userandgroup.api.dto.group;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.userandgroup.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class UpdateGroupDTO {

    @NotNull(message = "{updateGroupDto.id.NotNull.message}")
    private Long id;
    @NotEmpty(message = "{updateGroupDto.name.NotEmpty.message}")
    private String name;
    @NotEmpty(message = "{updateGroupDto.description.NotEmpty.message}")
    private String description;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;

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

    @ApiModelProperty(value = "Time until the group is valid.", example = "2019-11-20T10:28:02.727Z")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UpdateGroupDTO)) return false;
        UpdateGroupDTO that = (UpdateGroupDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "UpdateGroupDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
