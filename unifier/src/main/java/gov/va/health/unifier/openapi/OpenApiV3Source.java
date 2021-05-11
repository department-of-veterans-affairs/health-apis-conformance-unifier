package gov.va.health.unifier.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OpenApiV3Source {
  OpenAPI openApi;
}
