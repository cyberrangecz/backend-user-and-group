package cz.muni.ics.kypo.userandgroup.rest.DTO.group;

import cz.muni.ics.kypo.userandgroup.util.GroupDeletionStatus;

public class GroupDeletionResponseDTO {
    private Long id;

    private String name;

    private GroupDeletionStatus status;

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

    public GroupDeletionStatus getStatus() {
        return status;
    }

    public void setStatus(GroupDeletionStatus status) {
        this.status = status;
    }
}
