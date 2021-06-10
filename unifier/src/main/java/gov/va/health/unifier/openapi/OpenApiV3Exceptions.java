package gov.va.health.unifier.openapi;

public class OpenApiV3Exceptions {

  public static class OpenApiV3MergeException extends RuntimeException {
    public OpenApiV3MergeException(String message) {
      super(message);
    }
  }

  public static class DuplicateKey extends OpenApiV3MergeException {
    public DuplicateKey(final String message) {
      super(message);
    }
  }

  public static class DuplicatePath extends OpenApiV3MergeException {
    public DuplicatePath(final String message) {
      super(message);
    }
  }

  public static class DuplicateParameter extends OpenApiV3MergeException {
    public DuplicateParameter(final String message) {
      super(message);
    }
  }
}
