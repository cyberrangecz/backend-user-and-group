package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.utils.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.userandgroup.api.dto.enums.SourceDTO;
import cz.cyberrange.platform.userandgroup.api.dto.enums.UserAndGroupStatusDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * GroupViewDTO encapsulates information about a group.
 */
@ApiModel(value = "GroupViewDTO", description = "The basic information about a group.")
public class GroupViewDTO {

    @ApiModelProperty(value = "Main identifier of group.", example = "1", position = 1)
    private Long id;
    @ApiModelProperty(value = "A name of the group.", example = "Main group of organizers")
    private String name;
    @ApiModelProperty(value = "A description of the group.", example = "Organizers group for training run in June.")
    private String description;
    @ApiModelProperty(value = "Source of the group, whether its internal or from perun.", example = "Internal")
    private SourceDTO source;
    @ApiModelProperty(value = "Sign if the group can be deleted.", example = "false")
    private boolean canBeDeleted = true;
    @ApiModelProperty(value = "Time until the group is valid.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime expirationDate;

    /**
     * Convert the external ID to {@link SourceDTO}.
     *
     * @param externalId the external id
     */
    public void convertExternalIdToSource(Long externalId) {
        if (externalId == null) {
            this.source = SourceDTO.INTERNAL;
        } else {
            this.source = SourceDTO.PERUN;
        }
    }

    /**
     * Convert {@link UserAndGroupStatusDTO} to can be deleted.
     *
     * @param status the status
     */
    public void convertStatusToCanBeDeleted(UserAndGroupStatusDTO status) {
        if (status.equals(UserAndGroupStatusDTO.DELETED)) {
            this.canBeDeleted = true;
        }
        if (status.equals(UserAndGroupStatusDTO.VALID)) {
            this.canBeDeleted = false;
        }
    }

    /**
     * Gets the ID of the group.
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
     * Gets the {@link SourceDTO} of the group.
     *
     * @return the {@link SourceDTO} of the group.
     */
    public SourceDTO getSource() {
        return source;
    }

    /**
     * Sets the {@link SourceDTO} of the group.
     *
     * @param source the {@link SourceDTO} of the group.
     */
    public void setSource(SourceDTO source) {
        this.source = source;
    }

    /**
     * Mark if the group can be deleted.
     *
     * @return true if the group can be deleted, false otherwise
     */
    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    /**
     * Sets mark if the group can be deleted.
     *
     * @param canBeDeleted true if group can be deleted, false otherwise
     */
    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
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
        if (!(object instanceof GroupViewDTO)) return false;
        GroupViewDTO groupDTO = (GroupViewDTO) object;
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
