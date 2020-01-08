package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INSUFFICIENT_STORAGE, reason = "The server is unable to store the representation needed to complete the request.")
public class InsufficientStorageException extends RuntimeException {

  public InsufficientStorageException() {}

  public InsufficientStorageException(String message) {
    super(message);
  }

  public InsufficientStorageException(String message, Throwable ex) {
	super(message,ex);
  }
  
  public InsufficientStorageException(Throwable ex) {
    super(ex);
  }

}
