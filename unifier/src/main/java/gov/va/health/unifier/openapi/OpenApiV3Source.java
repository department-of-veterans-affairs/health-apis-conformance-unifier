package gov.va.health.unifier.openapi;

import gov.va.health.unifier.openapi.MergeConfig.RegexFilter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.File;
import java.net.URL;
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

  @NonNull @Builder.Default Filter scopeFilter = Filter.includeEverything();

  /** Build an Open API source from a contributor. */
  @SneakyThrows
  public static OpenApiV3Source from(@NonNull MergeConfig.Contributor config) {
    OpenApiV3SourceBuilder source =
        OpenApiV3Source.builder()
            .pathFilter(Filter.from(config.pathFilter()))
            .schemaFilter(Filter.from(config.schemaFilter()))
            .scopeFilter(Filter.from(config.scopeFilter()));
    if (config.file() != null) {
      source
          .name(config.file())
          .openApi(Json.mapper().readValue(new File(config.file()), OpenAPI.class));
    }
    if (config.url() != null) {
      source
          .name(config.url())
          .openApi(Json.mapper().readValue(new URL(config.url()), OpenAPI.class));
    }
    return source.build();
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

    /** Build a Filter from a Regex. */
    public static Filter from(@NonNull RegexFilter config) {
      return OpenApiV3Source.Filter.builder()
          .include(regexPredicateOrNull(config.include()))
          .exclude(regexPredicateOrNull(config.exclude()))
          .build();
    }

    public static Filter includeEverything() {
      return Filter.builder().build();
    }

    private static Predicate<String> regexPredicateOrNull(String pattern) {
      return pattern == null ? null : Pattern.compile(pattern).asMatchPredicate();
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
