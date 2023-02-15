package gov.va.health.unifier.openapi;

/** Defines exceptions for OAS merging. */
public class OpenApiV3Exceptions {

  /** Something bad happened when merging OAS files. */
  public static class OpenApiV3MergeException extends RuntimeException {
    public OpenApiV3MergeException(String message) {
      super(message);
    }
  }

  /** The merge configs resulted in a duplicate key in the merged OAS. */
  public static class DuplicateKey extends OpenApiV3MergeException {
    public DuplicateKey(final String message) {
      super(message);
    }
  }

  /** The merge configs resulted in a duplicate path in the merged OAS. */
  public static class DuplicatePath extends OpenApiV3MergeException {
    public DuplicatePath(final String message) {
      super(message);
    }
  }
}
