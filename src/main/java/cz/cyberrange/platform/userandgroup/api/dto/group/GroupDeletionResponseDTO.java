package cz.cyberrange.platform.userandgroup.api.dto.group;

import cz.cyberrange.platform.userandgroup.api.dto.enums.GroupDeletionStatusDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/** The outcome reported back to the caller right after a group deletion request. */
@Schema(name = "GroupDeletionResponseDTO", description = "The outcome of deleting one group.")
public class GroupDeletionResponseDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "SUCCESS")
  private GroupDeletionStatusDTO status;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public GroupDeletionStatusDTO getStatus() {
    return status;
  }

  public void setStatus(GroupDeletionStatusDTO status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof GroupDeletionResponseDTO)) return false;
    GroupDeletionResponseDTO that = (GroupDeletionResponseDTO) object;
    return Objects.equals(getId(), that.getId()) && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getStatus());
  }

  @Override
  public String toString() {
    return "GroupDeletionResponseDTO{" + "id=" + id + ", status=" + status + '}';
  }
}
