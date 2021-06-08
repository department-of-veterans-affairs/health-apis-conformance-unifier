package gov.va.api.health.conformance.unifier.exception;

/** Exception for duplicate capability resource. */
public class DuplicateCapabilityResourceException extends RuntimeException {

  public DuplicateCapabilityResourceException(final String message) {
    super(message);
  }
}
