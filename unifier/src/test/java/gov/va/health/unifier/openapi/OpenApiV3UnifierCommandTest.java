package gov.va.health.unifier.openapi;

import gov.va.health.unifier.UnifierCommand;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenApiV3UnifierCommandTest {

  @Test
  void tryMe() {
    Assertions.assertThat(UnifierCommand.mainNoExit("openapi", "--config", "merge.json"))
        .isEqualTo(0);
  }
}
