package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.informational.openapi.OpenApiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("openapi")
public class CurrentOpenApiProperties extends OpenApiProperties {}
