package cz.cyberrange.platform.userandgroup.definition.exceptions;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
    }

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundException(Throwable cause) {
        super(cause);
    }

    public FileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
