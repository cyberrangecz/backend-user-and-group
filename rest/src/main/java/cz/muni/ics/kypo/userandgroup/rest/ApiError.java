package cz.muni.ics.kypo.userandgroup.rest;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Represents a possible representation of errors to be used with the @ControllerAdvice global
 * exception handler.
 * </p>
 * 
 * <p>
 * That is parent class for all REST API Kypo2 implementation. Following example suggest one of the
 * possible applications of this class in your REST API project:
 * </p>
 * 
 * <pre>
 * <code>
 * public class ApiErrorMonitoring extends ApiError {
 * 
 * 	public ApiErrorMonitoring(APIErrorBuilder builder) {
 *		super(builder);
 * 	}
 * 
 * }
 * </code>
 * </pre>
 * 
 * @author Pavel Šeda
 * 
 */
public class ApiError {

  protected long timestamp;
  protected HttpStatus status;
  protected String message;
  protected List<String> errors;
  protected String path;

  public static class APIErrorBuilder {
    // required member variables
    private final HttpStatus status;
    private final String message;
    // optional member variables
    private List<String> errors = new ArrayList<>();
    private String path = "";

    public APIErrorBuilder(HttpStatus status, String message) {
      Objects.requireNonNull(status, "HttpStatus is necessary to not be null.");
      Objects.requireNonNull(message, "It is required to provide error message.");
      this.status = status;
      this.message = message;
    }

    public APIErrorBuilder setError(String error) {
      Objects.requireNonNull(error, "Given error could not be null");
      this.errors = Arrays.asList(error);
      return this;
    }

    public APIErrorBuilder setErrors(List<String> errors) {
      Objects.requireNonNull(errors, "Given list of errors could not be null");
      this.errors = errors;
      return this;
    }

    public APIErrorBuilder setPath(String path) {
      Objects.requireNonNull(path, "Given path could not be null");
      this.path = path;
      return this;
    }

    public ApiError build() {
      return new ApiError(this);
    }
  }

  protected ApiError(APIErrorBuilder builder) {
    super();
    this.timestamp = System.currentTimeMillis();
    this.status = builder.status;
    this.message = builder.message;
    this.errors = builder.errors;
    this.path = builder.path;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(final HttpStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(final List<String> errors) {
    this.errors = errors;
  }

  public void setError(final String error) {
    errors = Arrays.asList(error);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "ApiError [timestamp=" + timestamp + ", status=" + status + ", message=" + message + ", errors=" + errors + ", path=" + path + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, status, message, errors, path);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof ApiError))
      return false;
    ApiError other = (ApiError) obj;
    if (errors == null) {
      if (other.errors != null)
        return false;
    } else if (!errors.equals(other.getErrors()))
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.getMessage()))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.getPath()))
      return false;
    if (status != other.getStatus())
      return false;
    if (timestamp != other.getTimestamp())
      return false;
    return true;
  }


}
