package gov.va.health.unifier.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicateKey;
import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicatePath;
import gov.va.health.unifier.openapi.OpenApiV3Source.Filter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.File;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class OpenApiV3UnifierTest {

  public static final String INITIAL_OPENAPI = "src/test/resources/openapi-v3-initial.json";

  @Test
  void combinesAsExpected() {
    OpenAPI actual =
        OpenApiV3Unifier.startingWith(() -> openApiFromPath(INITIAL_OPENAPI))
            .apply(
                List.of(
                    OpenApiV3Source.builder()
                        .name("example-1")
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-1.json"))
                        .build(),
                    OpenApiV3Source.builder()
                        .name("example-2")
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-2.json"))
                        .build()));
    assertThat(actual)
        .isEqualTo(openApiFromPath("src/test/resources/openapi-v3-example-unified.json"));
  }

  @Test
  void pathFiltersAreApplied() {
    OpenAPI actual =
        OpenApiV3Unifier.startingWith(() -> openApiFromPath(INITIAL_OPENAPI))
            .apply(
                List.of(
                    OpenApiV3Source.builder()
                        .name("example-1")
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-1.json"))
                        .pathFilter(
                            Filter.builder()
                                .include(s -> s.equals("/Foo/{id}"))
                                .exclude(s -> !s.equals("/Foo/{id}"))
                                .build())
                        .build(),
                    OpenApiV3Source.builder()
                        .name("example-2")
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-2.json"))
                        .pathFilter(Filter.builder().include(s -> s.startsWith("/Blah")).build())
                        .build()));
    OpenAPI expected = openApiFromPath("src/test/resources/openapi-v3-example-unified.json");
    expected.getPaths().remove("/Blah/{id}");
    expected.getPaths().remove("/Blah");
    expected.getPaths().remove("/Foo");
    assertThat(Json.pretty(actual)).isEqualTo(Json.pretty(expected));
  }

  @Test
  void schemaFiltersAreApplied() {
    OpenAPI actual =
        OpenApiV3Unifier.startingWith(() -> openApiFromPath(INITIAL_OPENAPI))
            .apply(
                List.of(
                    OpenApiV3Source.builder()
                        .name("example-1")
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-1.json"))
                        .schemaFilter(
                            Filter.builder()
                                .include(s -> s.startsWith("Foo"))
                                .exclude(s -> s.startsWith("Blah"))
                                .build())
                        .build(),
                    OpenApiV3Source.builder()
                        .name("example-2")
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-2.json"))
                        .schemaFilter(
                            Filter.builder().include(s -> s.startsWith("Parameters")).build())
                        .build()));
    OpenAPI expected = openApiFromPath("src/test/resources/openapi-v3-example-unified.json");
    expected.getComponents().getSchemas().remove("Blah");
    expected.getComponents().getSchemas().remove("BlahBundle");
    expected.getComponents().getSchemas().remove("OperationOutcome");
    assertThat(Json.pretty(actual)).isEqualTo(Json.pretty(expected));
  }

  @Test
  void duplicatePathThrowsException() {
    assertThatExceptionOfType(DuplicatePath.class)
        .isThrownBy(
            () ->
                OpenApiV3Unifier.startingWith(() -> openApiFromPath(INITIAL_OPENAPI))
                    .apply(
                        List.of(
                            OpenApiV3Source.builder()
                                .name("example-1")
                                .openApi(
                                    openApiFromPath("src/test/resources/openapi-v3-example-1.json"))
                                .build(),
                            OpenApiV3Source.builder()
                                .name("duplicates")
                                .openApi(
                                    openApiFromPath(
                                        "src/test/resources/openapi-v3-duplicate-path.json"))
                                .build())));
  }

  @Test
  void schemaConflictThrowsException() {
    assertThatExceptionOfType(DuplicateKey.class)
        .isThrownBy(
            () ->
                OpenApiV3Unifier.startingWith(() -> openApiFromPath(INITIAL_OPENAPI))
                    .apply(
                        List.of(
                            OpenApiV3Source.builder()
                                .name("example-1")
                                .openApi(
                                    openApiFromPath("src/test/resources/openapi-v3-example-1.json"))
                                .build(),
                            OpenApiV3Source.builder()
                                .name("conflicting-schema")
                                .openApi(
                                    openApiFromPath(
                                        "src/test/resources/openapi-v3-conflict-schema.json"))
                                .build())));
  }

  @SneakyThrows
  private OpenAPI openApiFromPath(String path) {
    return Json.mapper().readValue(new File(path), OpenAPI.class);
  }
}
