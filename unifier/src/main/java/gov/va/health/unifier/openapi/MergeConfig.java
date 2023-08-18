package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.openapi.Check.argument;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
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

  /** File path of the merged Open API. */
  private String out;

  /** Properties that override any details of contributors. */
  private OpenApiProperties properties;

  /** Lazy Getter for in. */
  public List<Contributor> in() {
    if (in == null) {
      in = List.of();
    }
    return List.copyOf(in);
  }

  /** Validate that in and out are both specified. */
  public void validate() {
    argument(out == null, "out must be specified");
    argument(in().isEmpty(), "in must be specified");
    in().forEach(Contributor::validate);
  }

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class Contributor {
    /** Path to the Open API json file. Only one of file or url may be specified. */
    private String file;

    /** URL to the Open API json file. Only one of file or url may be specified. */
    private String url;

    /** Filter configuration for Open API path items. */
    private RegexFilter pathFilter;

    /** Filter configuration for Open API schema items. */
    private RegexFilter schemaFilter;

    /** Filter configuration for Open API security and security scheme scopes. */
    private RegexFilter scopeFilter;

    public RegexFilter pathFilter() {
      return pathFilter == null ? new RegexFilter() : pathFilter;
    }

    public RegexFilter schemaFilter() {
      return schemaFilter == null ? new RegexFilter() : schemaFilter;
    }

    public RegexFilter scopeFilter() {
      return scopeFilter == null ? new RegexFilter() : scopeFilter;
    }

    public void validate() {
      argument(file == null && url == null, "file or url must be specified");
      argument(file != null && url != null, "file or url must be specified, not both");
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
    private List<ServerProperties> servers;
    private Map<String, SecuritySchemeProperties> securityScheme;

    /** Lazy getter for security scheme. */
    public Map<String, SecuritySchemeProperties> securityScheme() {
      if (securityScheme == null) {
        securityScheme = Map.of();
      }
      return Map.copyOf(securityScheme);
    }

    /** Lazy Initializer. */
    public List<ServerProperties> servers() {
      if (servers == null) {
        servers = List.of();
      }
      return List.copyOf(servers);
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
