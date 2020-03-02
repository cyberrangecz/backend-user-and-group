package cz.muni.ics.kypo.userandgroup.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "The requested data cannot be processed.")
public class UnprocessableEntityException extends ExceptionWithEntity {

    public UnprocessableEntityException() {
        super();
    }

    public UnprocessableEntityException(EntityErrorDetail entityErrorDetail) {
        super(entityErrorDetail);
    }

    public UnprocessableEntityException(EntityErrorDetail entityErrorDetail, Throwable cause) {
        super(entityErrorDetail, cause);
    }

    public UnprocessableEntityException(Throwable cause) {
        super(cause);
    }
}
