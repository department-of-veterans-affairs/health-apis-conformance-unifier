package gov.va.api.health.informational.openapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;

@ExtendWith(SpringExtension.class)
public class OpenApiControllerTest {
  OpenApiController controller = new OpenApiController(loadYaml());

  public Resource loadYaml() {
    return new ClassPathResource("/simpleOpenApi.yaml");
  }

  /*
   * I'd rather this did the same thing as testYaml and compare against a known good json file, but the autoformatter
   * in our pom keeps "helping" by making the JSON pretty. I think I've seen some ways to avoid that but I can't remember.
   */
  @Test
  public void testJsonEndpoint() {
    String jsonReturned = null;
    try {
      jsonReturned = controller.openapiJson();
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert jsonReturned.contains("0.1.9");
  }

  @Test
  public void testYamlEndpoint() {
    String actual = null;
    String expected = null;
    try {
      expected =
          StreamUtils.copyToString(
              getClass().getResourceAsStream("/simpleOpenApi.yaml"), StandardCharsets.UTF_8);
    } catch (IOException e) {
      System.out.println("Failed while trying to get yaml to compare against");
      e.printStackTrace();
    }
    try {
      actual = controller.openapiYaml();
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert actual.contentEquals(expected);
  }
}
