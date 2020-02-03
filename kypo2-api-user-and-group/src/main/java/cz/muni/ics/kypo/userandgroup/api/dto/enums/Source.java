package cz.muni.ics.kypo.userandgroup.api.dto.enums;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Source", description = "The source.")
public enum Source {

    INTERNAL("Internal"),
    PERUN("Perun");

    private final String nameOfSource;

    private Source(String name) {
        this.nameOfSource = name;
    }

    public String getNameOfSource() {
        return nameOfSource;
    }
}
