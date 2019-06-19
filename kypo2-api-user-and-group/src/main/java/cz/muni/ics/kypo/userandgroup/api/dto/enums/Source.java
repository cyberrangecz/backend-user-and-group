package cz.muni.ics.kypo.userandgroup.api.dto.enums;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
