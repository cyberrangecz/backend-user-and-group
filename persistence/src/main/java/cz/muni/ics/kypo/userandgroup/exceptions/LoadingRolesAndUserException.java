package cz.muni.ics.kypo.userandgroup.exceptions;

public class LoadingRolesAndUserException extends RuntimeException {

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
