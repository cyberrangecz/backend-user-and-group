package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_MODIFIED, reason = "The requested resource was not modified")
public class ResourceNotModifiedException extends RuntimeException {

  public ResourceNotModifiedException() {}

  public ResourceNotModifiedException(String message) {
    super(message);
  }

  public ResourceNotModifiedException(String message, Throwable ex) {
	super(message,ex);
  }
  
  public ResourceNotModifiedException(Throwable ex) {
    super(ex);
  }

}
