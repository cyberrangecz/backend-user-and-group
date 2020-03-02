package cz.muni.ics.kypo.userandgroup.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested entity could not be found")
public class EntityNotFoundException extends ExceptionWithEntity {
    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(EntityErrorDetail entityErrorDetail) {
        super(entityErrorDetail);
    }

    public EntityNotFoundException(EntityErrorDetail entityErrorDetail, Throwable cause) {
        super(entityErrorDetail, cause);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
