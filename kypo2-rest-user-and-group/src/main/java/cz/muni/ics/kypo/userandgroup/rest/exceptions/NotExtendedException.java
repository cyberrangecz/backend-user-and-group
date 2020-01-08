package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_EXTENDED, reason = "Further extensions to the request are required for the server to fulfil it.")
public class NotExtendedException extends RuntimeException {

  public NotExtendedException() {}

  public NotExtendedException(String message) {
    super(message);
  }

  public NotExtendedException(String message, Throwable ex) {
	super(message,ex);
  }
  
  public NotExtendedException(Throwable ex) {
    super(ex);
  }

}
