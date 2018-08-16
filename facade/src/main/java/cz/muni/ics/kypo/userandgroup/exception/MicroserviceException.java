package cz.muni.ics.kypo.userandgroup.exception;

public class MicroserviceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MicroserviceException() {
        super();
    }

    public MicroserviceException(String message) {
        super(message);
    }

    public MicroserviceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MicroserviceException(Throwable throwable) {
        super(throwable);
    }
}
