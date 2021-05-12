package gov.va.health.unifier.openapi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Builder
@Data
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class MergeConfig {

  /** Input configuration. You should two or more to be useful. */
  private List<Contributor> in;

  /** File path of the merged Open API */
  private String out;

  /** Properties that override any details of contributors. */
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
    /** Path to the Open API json file. */
    private String file;
    /** Filter configuration for Open API path items. */
    private RegexFilter pathFilter;
    /** Filter configuration for Open API schema items. */
    private RegexFilter schemaFilter;

    public RegexFilter pathFilter() {
      return pathFilter == null ? new RegexFilter() : pathFilter;
    }

    public RegexFilter schemaFilter() {
      return schemaFilter == null ? new RegexFilter() : schemaFilter;
    }
  }

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class RegexFilter {

    /**
     * If specified, only items matching this regex will be included. Excluded filter will be
     * ignored. If neither include or exclude are specified, then all items are included.
     */
    private String include;

    /**
     * If included is not specified, any item matching this regex will be excluded. All others will
     * be included. If neither include or exclude are specified, then all items are included.
     */
    private String exclude;
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
