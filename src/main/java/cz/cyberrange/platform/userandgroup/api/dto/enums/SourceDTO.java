package cz.cyberrange.platform.userandgroup.api.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Identifies whether a group was created directly in the platform or provided by Perun. */
@Schema(
    name = "Source",
    description = "Says whether a group was created in the platform or came from Perun.")
public enum SourceDTO {
  INTERNAL("Internal"),
  PERUN("Perun");

  private final String nameOfSource;

  SourceDTO(String name) {
    this.nameOfSource = name;
  }

  public String getNameOfSource() {
    return nameOfSource;
  }
}
