package gov.va.api.health.conformance.unifier.exception;

/** Exception for path duplication. */
public class OpenApiPathDuplicateException extends RuntimeException {
  public OpenApiPathDuplicateException(final String message) {
    super(message);
  }
}
