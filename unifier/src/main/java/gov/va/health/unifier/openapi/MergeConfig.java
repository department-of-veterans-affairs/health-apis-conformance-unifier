package gov.va.health.unifier.openapi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@Builder
@Data
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class MergeConfig {

  private List<Contributor> in;

  private String out;

  private OpenApiProperties properties;

  public List<Contributor> in() {
    if (in == null) {
      in = new ArrayList<>();
    }
    return in;
  }

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class Contributor {
    private String file;

    @SneakyThrows
    OpenApiV3Source asOpenApiV3Source() {
      return OpenApiV3Source.builder()
          .openApi(Json.mapper().readValue(new File(file()), OpenAPI.class))
          .build();
    }
  }

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class OpenApiProperties {
    private String title;
    private String description;
    private String version;
    private ExternalDocumentationProperties externalDocs;
    private ServerProperties server;
    private Map<String, SecuritySchemeProperties> securityScheme;

    public Map<String, SecuritySchemeProperties> securityScheme() {
      if (securityScheme == null) {
        securityScheme = new HashMap<>();
      }
      return securityScheme;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class ExternalDocumentationProperties {
      private String url;
      private String description;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class ServerProperties {
      private String url;
      private String description;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class SecuritySchemeProperties {
      private String authorizationUrl;
      private String tokenUrl;
      private Type type;
      private In in;
    }
  }
}
