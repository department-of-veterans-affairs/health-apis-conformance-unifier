package gov.va.health.unifier.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OpenApiV3Source {
  public static final Pattern EVERYTHING = Pattern.compile(".*");
  @NonNull String name;
  @NonNull OpenAPI openApi;
  @NonNull @Builder.Default Filter pathFilter = Filter.includeEverything();
  @NonNull @Builder.Default Filter schemaFilter = Filter.includeEverything();

  @Value
  @Builder
  public static class Filter implements Predicate<String> {

    /**
     * If specified, only items matching this regex will be included. Excluded filter will be
     * ignored. If neither include or exclude are specified, then all items are included.
     */
    Predicate<String> include;
    /**
     * If included is not specified, any item matching this regex will be excluded. All others will
     * be included. If neither include or exclude are specified, then all items are included.
     */
    Predicate<String> exclude;

    public static Filter includeEverything() {
      return Filter.builder().build();
    }

    @Override
    public boolean test(String s) {
      if (include != null) {
        return include.test(s);
      }
      if (exclude != null) {
        return !exclude.test(s);
      }
      return true;
    }
  }
}
