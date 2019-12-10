package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Override publication date of ConformanceStatementProperties to current time. */
@Configuration
@ConfigurationProperties("dstu2.conformance")
public class CurrentPublicationDateDstu2ConformanceStatementProperties
    extends ConformanceStatementProperties {

  @PostConstruct
  public void postConstruct() {
    setPublicationDate(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
  }
}
