package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class GroupDeletionResponseDTO {

    private Long id;
    private GroupDeletionStatusDTO status;

    @ApiModelProperty(value = "Main identifiers of deleted group.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Result of deleting group.", example = "SUCCESS")
    public GroupDeletionStatusDTO getStatus() {
        return status;
    }

    public void setStatus(GroupDeletionStatusDTO status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDeletionResponseDTO that = (GroupDeletionResponseDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStatus());
    }

    @Override
    public String toString() {
        return "GroupDeletionResponseDTO{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}
