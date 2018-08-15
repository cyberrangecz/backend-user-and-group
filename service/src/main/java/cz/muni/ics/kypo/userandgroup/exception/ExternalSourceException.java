package cz.muni.ics.kypo.userandgroup.exception;

public class ExternalSourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExternalSourceException() {
        super();
    }

    public ExternalSourceException(String message) {
        super(message);
    }

    public ExternalSourceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ExternalSourceException(Throwable throwable) {
        super(throwable);
    }
}
