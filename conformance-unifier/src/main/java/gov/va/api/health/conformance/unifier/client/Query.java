package gov.va.api.health.conformance.unifier.client;

import lombok.Builder;
import lombok.Value;

/**
 * Type safe model for sending request to APIs.
 *
 * <pre>
 *   Query.forType(ClaimsShow.class)
 *     .url("http://somewhere/in/the/www")
 *     .build();
 * </pre>
 */
@Value
@Builder(toBuilder = true)
public class Query<T> {

  String url;

  Class<T> type;

  /** Start a builder chain to query for a given type. */
  public static <R> QueryBuilder<R> forType(Class<R> forType) {
    return Query.<R>builder().type(forType);
  }

  /** Get the path. */
  String toQueryString() {
    return url;
  }
}
