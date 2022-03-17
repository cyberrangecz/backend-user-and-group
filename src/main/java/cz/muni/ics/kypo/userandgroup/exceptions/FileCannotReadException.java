package cz.muni.ics.kypo.userandgroup.exceptions;

public class FileCannotReadException extends RuntimeException {

    public FileCannotReadException() {
    }

    public FileCannotReadException(String message) {
        super(message);
    }

    public FileCannotReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileCannotReadException(Throwable cause) {
        super(cause);
    }

    public FileCannotReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
