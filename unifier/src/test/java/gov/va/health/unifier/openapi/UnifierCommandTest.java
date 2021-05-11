package gov.va.health.unifier.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.health.unifier.UnifierCommand;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.File;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class UnifierCommandTest {

  @Test
  @SneakyThrows
  void openapiMerge() {
    assertThat(
            UnifierCommand.mainNoExit(
                "openapi", "merge", "--config", "src/test/resources/openapi-v3-merge-config.json"))
        .isEqualTo(0);
    File output = new File("target/openapi.json");
    assertThat(output).exists();
    OpenAPI result = Json.mapper().readValue(output, OpenAPI.class);
    assertThat(result.getPaths().size()).isGreaterThan(0);
  }
}
