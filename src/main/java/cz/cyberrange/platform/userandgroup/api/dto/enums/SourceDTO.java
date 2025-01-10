package cz.cyberrange.platform.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Source", description = "The source.")
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
