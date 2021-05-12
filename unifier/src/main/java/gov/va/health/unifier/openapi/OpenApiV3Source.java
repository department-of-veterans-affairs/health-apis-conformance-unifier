package gov.va.health.unifier.openapi;

import gov.va.health.unifier.openapi.MergeConfig.RegexFilter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.File;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

@Value
@Builder
public class OpenApiV3Source {
  public static final Pattern EVERYTHING = Pattern.compile(".*");
  @NonNull String name;
  @NonNull OpenAPI openApi;
  @NonNull @Builder.Default Filter pathFilter = Filter.includeEverything();
  @NonNull @Builder.Default Filter schemaFilter = Filter.includeEverything();

  @SneakyThrows
  public static OpenApiV3Source from(@NonNull MergeConfig.Contributor config) {
    return OpenApiV3Source.builder()
        .name(config.file())
        .openApi(Json.mapper().readValue(new File(config.file()), OpenAPI.class))
        .pathFilter(Filter.from(config.pathFilter()))
        .schemaFilter(Filter.from(config.schemaFilter()))
        .build();
  }

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

    public static Filter from(@NonNull RegexFilter config) {
      return OpenApiV3Source.Filter.builder()
          .include(regexPredicateOrNull(config.include()))
          .exclude(regexPredicateOrNull(config.exclude()))
          .build();
    }

    private static Predicate<String> regexPredicateOrNull(String pattern) {
      return pattern == null ? null : Pattern.compile(pattern).asMatchPredicate();
    }

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
