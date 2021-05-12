package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.Print.println;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.health.unifier.openapi.MergeConfig.Contributor;
import gov.va.health.unifier.openapi.MergeConfig.OpenApiProperties;
import gov.va.health.unifier.openapi.MergeConfig.OpenApiProperties.ExternalDocumentationProperties;
import gov.va.health.unifier.openapi.MergeConfig.OpenApiProperties.SecuritySchemeProperties;
import gov.va.health.unifier.openapi.MergeConfig.OpenApiProperties.ServerProperties;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;

@Command(name = "config-sample", description = "Print a merge configuration sample.")
public class ConfigSampleCommand implements Callable<Integer> {

  @Override
  @SneakyThrows
  public Integer call() {
    var sample =
        MergeConfig.builder()
            .out("final-openapi.json")
            .in(
                List.of(
                    Contributor.builder().file("sources/openapi-1.json").build(),
                    Contributor.builder().file("sources/openapi-2.json").build()))
            .properties(
                OpenApiProperties.builder()
                    .title("Awesome API")
                    .description("This is a great API")
                    .version("v1.0")
                    .externalDocs(
                        ExternalDocumentationProperties.builder()
                            .description("Awesome Specification")
                            .url("https://example.com/awesome.html")
                            .build())
                    .server(
                        ServerProperties.builder()
                            .description("Production")
                            .url("https://example.com/awesome/api")
                            .build())
                    .securityScheme(
                        Map.of(
                            "OAuthFlow",
                            SecuritySchemeProperties.builder()
                                .authorizationUrl("https://example.com/oauth/authorization")
                                .tokenUrl("https://example.com/oauth/token")
                                .in(In.HEADER)
                                .type(Type.OAUTH2)
                                .build()))
                    .build())
            .build();
    println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(sample));
    return 0;
  }
}
