package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE,
    reason = "The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.")
public class ServiceUnavailableException extends RuntimeException {

  public ServiceUnavailableException() {}

  public ServiceUnavailableException(String message) {
    super(message);
  }

  public ServiceUnavailableException(String message, Throwable ex) {
	super(message,ex);
  }
  
  public ServiceUnavailableException(Throwable ex) {
    super(ex);
  }

}
