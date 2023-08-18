package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.informational.r4.capability.CapabilityStatementProperties;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Override publication date of CapabilityStatementProperties to current time. */
@Configuration
@ConfigurationProperties("r4.capability")
public class CurrentPublicationDateR4CapabilityStatementProperties
    extends CapabilityStatementProperties {

  @PostConstruct
  public void postConstruct() {
    setPublicationDate(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
  }
}
