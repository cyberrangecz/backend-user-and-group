package cz.muni.ics.kypo.userandgroup.exceptions;

public class LoadingRolesAndUserException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LoadingRolesAndUserException() {
        super();
    }

    public LoadingRolesAndUserException(String message) {
        super(message);
    }

    public LoadingRolesAndUserException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public LoadingRolesAndUserException(Throwable throwable) {
        super(throwable);
    }
}
