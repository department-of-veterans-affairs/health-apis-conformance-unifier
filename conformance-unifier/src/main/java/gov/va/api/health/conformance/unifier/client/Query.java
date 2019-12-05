package gov.va.api.health.conformance.unifier.client;

import lombok.Builder;
import lombok.Value;

/**
 * Type safe model for sending request to Benefits Claims.
 *
 * <pre>
 *   Query.forType(ClaimsShow.class)
 *     .id("123")
 *     .build();
 *
 *     Will append to path the optional id if specified:
 *     {{path}}/123
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

  /** We won't do a lookup in the id service if its an exception. */
  String toQueryString() {
    StringBuilder queryString = new StringBuilder(url);
    // queryString.append("?id=").append(id);
    //    if (id != null) {
    //      queryString.append("/").append(id);
    //    }
    return queryString.toString();
  }
}
