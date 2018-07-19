package cz.muni.ics.kypo.userandgroup.api.dto;

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
