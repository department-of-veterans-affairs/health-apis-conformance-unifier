package gov.va.health.unifier.openapi;

import gov.va.health.unifier.openapi.MergeConfig.OpenApiProperties;
import gov.va.health.unifier.openapi.MergeConfig.OpenApiProperties.SecuritySchemeProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class InitialOpenApiV3 implements Supplier<OpenAPI> {

  private final OpenApiProperties properties;

  @Override
  public OpenAPI get() {
    OpenAPI openapi = new OpenAPI();
    openapi.info(info());
    openapi.externalDocs(externalDocumentation());
    openapi.servers(servers());
    openapi.components(components());
    return openapi;
  }

  private Components components() {
    if (properties.securityScheme().isEmpty()) {
      return null;
    }
    Components components = new Components();
    components.securitySchemes(securitySchemes());
    return components;
  }

  private Map<String, SecurityScheme> securitySchemes() {
    return properties.securityScheme().entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> securityScheme(e.getValue())));
  }

  private SecurityScheme securityScheme(SecuritySchemeProperties schemeProperties) {
    OAuthFlow implicitFlow = new OAuthFlow();
    implicitFlow.authorizationUrl(schemeProperties.authorizationUrl());
    implicitFlow.tokenUrl(schemeProperties.tokenUrl());
    implicitFlow.scopes(new Scopes());
    OAuthFlows flows = new OAuthFlows();
    flows.implicit(implicitFlow);
    SecurityScheme securityScheme = new SecurityScheme();
    securityScheme.type(schemeProperties.type());
    securityScheme.in(schemeProperties.in());
    securityScheme.flows(flows);
    return securityScheme;
  }

  private Info info() {
    Info info = new Info();
    info.title(properties.title());
    info.description(properties.description());
    info.version(properties.version());
    return info;
  }

  private List<Server> servers() {
    if (properties.server() == null) {
      return null;
    }
    Server server = new Server();
    server.url(properties.server().url());
    server.description(properties.server().description());
    /* We need a mutable list for the servers so other can be added later */
    List<Server> servers = new ArrayList<>(1);
    servers.add(server);
    return servers;
  }

  private ExternalDocumentation externalDocumentation() {
    if (properties.externalDocs() == null) {
      return null;
    }
    ExternalDocumentation externalDocs = new ExternalDocumentation();
    externalDocs.description(properties.externalDocs().description());
    externalDocs.url(properties.externalDocs().url());
    return externalDocs;
  }
}
