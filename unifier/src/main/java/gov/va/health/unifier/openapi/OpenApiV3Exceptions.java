package gov.va.health.unifier.openapi;

public class OpenApiV3Exceptions {

  public static class OpenApiV3MergeException extends RuntimeException {

    public OpenApiV3MergeException(String message) {
      super(message);
    }

    public OpenApiV3MergeException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class OpenApiDuplicateKeyConflictException extends OpenApiV3MergeException {
    public OpenApiDuplicateKeyConflictException(final String message) {
      super(message);
    }
  }

  public static class DuplicateCapabilityResourceException extends RuntimeException {

    public DuplicateCapabilityResourceException(final String message) {
      super(message);
    }
  }

  public static class OpenApiPathDuplicateException extends RuntimeException {
    public OpenApiPathDuplicateException(final String message) {
      super(message);
    }
  }
}
