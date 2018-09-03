package cz.muni.ics.kypo.userandgroup.api.dto.group;

import cz.muni.ics.kypo.userandgroup.api.dto.MicroserviceForGroupDeletionDTO;
import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;

import java.util.List;
import java.util.Objects;

public class GroupDeletionResponseDTO {
    private Long id;

    private List<MicroserviceForGroupDeletionDTO> microserviceForGroupDeletionDTOs;

    private GroupDeletionStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<MicroserviceForGroupDeletionDTO> getMicroserviceForGroupDeletionDTOs() {
        return microserviceForGroupDeletionDTOs;
    }

    public void setMicroserviceForGroupDeletionDTOs(List<MicroserviceForGroupDeletionDTO> microserviceForGroupDeletionDTOs) {
        this.microserviceForGroupDeletionDTOs = microserviceForGroupDeletionDTOs;
    }

    public void addMicroserviceForGroupDeletionDTO(MicroserviceForGroupDeletionDTO microserviceForGroupDeletionDTO) {
        this.microserviceForGroupDeletionDTOs.add(microserviceForGroupDeletionDTO);
    }

    public void removeMicroserviceForGroupDeletionDTO(MicroserviceForGroupDeletionDTO microserviceForGroupDeletionDTO) {
        this.microserviceForGroupDeletionDTOs.remove(microserviceForGroupDeletionDTO);
    }

    public GroupDeletionStatus getStatus() {
        return status;
    }

    public void setStatus(GroupDeletionStatus status) {
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
