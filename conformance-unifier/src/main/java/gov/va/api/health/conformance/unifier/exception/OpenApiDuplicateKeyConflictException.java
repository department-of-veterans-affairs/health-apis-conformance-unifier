package gov.va.api.health.conformance.unifier.exception;

public class OpenApiDuplicateKeyConflictException extends RuntimeException {
  public OpenApiDuplicateKeyConflictException(final String message) {
    super(message);
  }
}
