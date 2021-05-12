package gov.va.health.unifier.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.health.unifier.openapi.OpenApiV3Source.Filter;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenApiV3SourceTest {

  static Stream<Arguments> filter() {
    return Stream.of(
        arguments(Filter.includeEverything(), List.of("a", "b", "c"), List.of()),
        arguments(Filter.builder().build(), List.of("a", "b", "c"), List.of()),
        arguments(Filter.builder().include("a"::equals).build(), List.of("a"), List.of("b", "c")),
        arguments(
            Filter.builder().include("a"::equals).exclude("a"::equals).build(),
            List.of("a"),
            List.of("b", "c")),
        arguments(Filter.builder().exclude("a"::equals).build(), List.of("b", "c"), List.of("a"))
        //
        );
  }

  @ParameterizedTest(name = "[{index}] included {1}, excluded {2}")
  @MethodSource
  void filter(Filter filter, List<String> accepted, List<String> rejected) {
    accepted.forEach(s -> assertThat(filter.test(s)).isTrue());
    rejected.forEach(s -> assertThat(filter.test(s)).isFalse());
  }
}
