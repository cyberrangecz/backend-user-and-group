package cz.muni.ics.kypo.userandgroup.api.dto.group;

import java.util.Objects;

public class GroupForUsersDTO {

    private Long id;

    private String name;

    private String status;

    private Long externalId;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDTO groupDTO = (GroupDTO) o;
        return Objects.equals(getId(), groupDTO.getId()) &&
                Objects.equals(getName(), groupDTO.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "GroupForUsersDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
