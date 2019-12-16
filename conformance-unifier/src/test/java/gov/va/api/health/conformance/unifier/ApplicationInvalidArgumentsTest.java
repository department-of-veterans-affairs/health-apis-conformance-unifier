package gov.va.api.health.conformance.unifier;

import gov.va.api.health.conformance.unifier.fhir.EndpointTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/** Test various scenarios of running application with invalid arguments. */
@RunWith(SpringRunner.class)
@ContextConfiguration(
  classes = Application.class,
  initializers = ConfigFileApplicationContextInitializer.class
)
public class ApplicationInvalidArgumentsTest {

  @Autowired Application unifierApplication;

  /** Test arguments to test unsupported endpoint type. */
  @Test(expected = IllegalArgumentException.class)
  @SneakyThrows
  public void invalidEndpointArgumentTest() {
    final String[] args =
        new String[] {
          ResourceTypeEnum.R4.type(),
          EndpointTypeEnum.UNKNOWN.type(),
          "http://www.fhir.com/metadata"
        };
    unifierApplication.run(new DefaultApplicationArguments(args));
  }

  /** Test invalid metadata arg. */
  @Test(expected = IllegalArgumentException.class)
  @SneakyThrows
  public void invalidMetadataArgumentsTest() {
    final String[] args =
        new String[] {
          "--" + Application.METADATA_ARG + "=blah=halb,hello",
          ResourceTypeEnum.UNKNOWN.type(),
          EndpointTypeEnum.METADATA.type(),
          "http://www.fhir.com/metadata"
        };
    unifierApplication.run(new DefaultApplicationArguments(args));
  }

  /** Test arguments to test wrong number of arguments. */
  @Test(expected = IllegalArgumentException.class)
  @SneakyThrows
  public void invalidNumberArgumentsTest() {
    final String[] args =
        new String[] {ResourceTypeEnum.R4.type(), EndpointTypeEnum.METADATA.type()};
    unifierApplication.run(new DefaultApplicationArguments(args));
  }

  /** Test arguments to test unsupported resource type. */
  @Test(expected = IllegalArgumentException.class)
  @SneakyThrows
  public void invalidResourceArgumentTest() {
    final String[] args =
        new String[] {
          ResourceTypeEnum.UNKNOWN.type(),
          EndpointTypeEnum.METADATA.type(),
          "http://www.fhir.com/metadata"
        };
    unifierApplication.run(new DefaultApplicationArguments(args));
  }

  /** Run the main method directly for code coverage. */
  @Test(expected = IllegalStateException.class)
  public void mainTest() {
    Application.main(new String[] {});
  }
}
