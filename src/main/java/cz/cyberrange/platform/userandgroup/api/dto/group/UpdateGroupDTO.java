package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.utils.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates information about a group to be updated.
 */
@ApiModel(value = "UpdateGroupDTO", description = "Data that need to be provided to update particular group.")
public class UpdateGroupDTO {

    @ApiModelProperty(value = "Main identifier of group.", required = true, example = "1", position = 1)
    @NotNull(message = "{group.id.NotNull.message}")
    private Long id;
    @ApiModelProperty(value = "A name of the group.", required = true, example = "Main group.")
    @NotEmpty(message = "{group.name.NotEmpty.message}")
    private String name;
    @ApiModelProperty(value = "A description of the group.", required = true, example = "Group for main users.")
    @NotEmpty(message = "{group.description.NotEmpty.message}")
    private String description;
    @ApiModelProperty(name = "expiration_date", value = "Time until the group is valid.", example = "2019-11-20T10:28:02.727Z")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;

    /**
     * Gets the ID of the the group.
     *
     * @return the ID of the group.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the group.
     *
     * @param id the ID of the group.
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * Sets the description of the group.
     *
     * @param description the description of the group.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets expiration date of the group.
     *
     * @return the expiration date of the group.
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
