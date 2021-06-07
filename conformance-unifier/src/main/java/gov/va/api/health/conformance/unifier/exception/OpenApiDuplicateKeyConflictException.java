package gov.va.api.health.conformance.unifier.exception;

/** Exception for a duplicate key. */
public class OpenApiDuplicateKeyConflictException extends RuntimeException {
  public OpenApiDuplicateKeyConflictException(final String message) {
    super(message);
  }
}
