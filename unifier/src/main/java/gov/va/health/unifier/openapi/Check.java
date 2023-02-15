package gov.va.health.unifier.openapi;

import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/** Methods for checking certain conditions are correct. */
@UtilityClass
public class Check {

  public static void argument(boolean condition, String message, Object... args) {
    throwIf(IllegalArgumentException::new, condition, message, args);
  }

  /** Throw exception if condition is met. */
  @SneakyThrows
  public static void throwIf(
      Function<String, Exception> newException, boolean condition, String message, Object... args) {
    if (condition) {
      throw newException.apply(String.format(message, args));
    }
  }
}
