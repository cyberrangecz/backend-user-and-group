package cz.muni.ics.kypo.userandgroup.enums.dto;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Source", description = "The source.")
public enum Source {

    INTERNAL("Internal"),
    PERUN("Perun");

    private final String nameOfSource;

    Source(String name) {
        this.nameOfSource = name;
    }

    public String getNameOfSource() {
        return nameOfSource;
    }
}
