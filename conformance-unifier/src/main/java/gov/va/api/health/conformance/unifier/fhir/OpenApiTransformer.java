package gov.va.api.health.conformance.unifier.fhir;

import gov.va.api.health.informational.openapi.OpenApiProperties;
import gov.va.api.health.informational.openapi.OpenApiUtilities;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class OpenApiTransformer extends BaseOpenApiTransformer {
  private final OpenApiProperties openApiProperties;

  @Override
  protected OpenAPI initialInstance() {
    return OpenApiUtilities.initializeOpenApi(openApiProperties);
  }
}
