package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_MODIFIED, reason = "The requested resource was not modified")
public class ResourceNotModifiedException extends RuntimeException {

    public ResourceNotModifiedException() {
    }

    public ResourceNotModifiedException(String message) {
        super(message);
    }

    public ResourceNotModifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotModifiedException(Throwable cause) {
        super(cause);
    }

    public ResourceNotModifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
