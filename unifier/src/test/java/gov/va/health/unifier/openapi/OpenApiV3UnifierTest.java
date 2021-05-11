package gov.va.health.unifier.openapi;

import static org.assertj.core.api.Assertions.assertThat;

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
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-1.json"))
                        .build(),
                    OpenApiV3Source.builder()
                        .openApi(openApiFromPath("src/test/resources/openapi-v3-example-2.json"))
                        .build()));
    assertThat(actual)
        .isEqualTo(openApiFromPath("src/test/resources/openapi-v3-example-unified.json"));
  }

  @SneakyThrows
  private OpenAPI openApiFromPath(String path) {
    return Json.mapper().readValue(new File(path), OpenAPI.class);
  }
}
