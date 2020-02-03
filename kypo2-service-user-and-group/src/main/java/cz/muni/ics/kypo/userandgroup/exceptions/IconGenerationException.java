package cz.muni.ics.kypo.userandgroup.exceptions;

public class IconGenerationException extends RuntimeException {

    public IconGenerationException() {
    }

    public IconGenerationException(String message) {
        super(message);
    }

    public IconGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IconGenerationException(Throwable cause) {
        super(cause);
    }

    public IconGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
