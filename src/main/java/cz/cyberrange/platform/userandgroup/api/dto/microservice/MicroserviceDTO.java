package cz.cyberrange.platform.userandgroup.api.dto.microservice;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/** The detail of a registered microservice, returned to the caller. */
@Schema(name = "MicroserviceDTO", description = "A service registered with the platform.")
public class MicroserviceDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(example = "training")
  private String name;

  @Schema(example = "/training/api/v1")
  private String endpoint;

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

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof MicroserviceDTO)) return false;
    MicroserviceDTO that = (MicroserviceDTO) object;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName());
  }

  @Override
  public String toString() {
    return "MicroserviceDTO{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", endpoint='"
        + endpoint
        + '\''
        + '}';
  }
}
