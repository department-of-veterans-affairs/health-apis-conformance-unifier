package gov.va.api.health.conformance.unifier.exception;

public class OpenApiPathDuplicateException extends RuntimeException {
  public OpenApiPathDuplicateException(final String message) {
    super(message);
  }
}
