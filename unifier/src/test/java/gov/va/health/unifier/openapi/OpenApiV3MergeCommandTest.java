package gov.va.health.unifier.openapi;

import gov.va.health.unifier.UnifierCommand;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenApiV3MergeCommandTest {

  @Test
  void tryMe() {
    Assertions.assertThat(
            UnifierCommand.mainNoExit(
                "openapi", "merge", "--config", "src/test/resources/openapi-v3-merge-config.json"))
        .isEqualTo(0);
  }
}
