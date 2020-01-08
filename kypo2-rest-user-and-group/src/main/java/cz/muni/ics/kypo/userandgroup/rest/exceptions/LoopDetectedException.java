package cz.muni.ics.kypo.userandgroup.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.LOOP_DETECTED,
    reason = "The server detected an infinite loop while processing the request (sent in lieu of 208 Already Reported).")
public class LoopDetectedException extends RuntimeException {

  public LoopDetectedException() {}

  public LoopDetectedException(String message) {
    super(message);
  }

  public LoopDetectedException(String message, Throwable ex) {
	super(message,ex);
  }
  
  public LoopDetectedException(Throwable ex) {
    super(ex);
  }

}
