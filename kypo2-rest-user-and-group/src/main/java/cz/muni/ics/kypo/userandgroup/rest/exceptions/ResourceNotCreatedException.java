package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "The requested resource was not created")
public class ResourceNotCreatedException extends RuntimeException {

    public ResourceNotCreatedException() {
    }

    public ResourceNotCreatedException(String message) {
        super(message);
    }

    public ResourceNotCreatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotCreatedException(Throwable cause) {
        super(cause);
    }

    public ResourceNotCreatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
