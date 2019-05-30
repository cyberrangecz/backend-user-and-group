package cz.muni.ics.kypo.userandgroup.api.exceptions;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
public class MicroserviceException extends RuntimeException {

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
