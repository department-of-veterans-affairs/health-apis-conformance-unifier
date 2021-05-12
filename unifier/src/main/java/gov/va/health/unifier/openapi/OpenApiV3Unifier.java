package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.Print.println;
import static java.util.stream.Collectors.toList;

import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicateKey;
import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicatePath;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor(staticName = "startingWith")
public class OpenApiV3Unifier implements Function<List<? extends OpenApiV3Source>, OpenAPI> {

  @Getter private final Supplier<OpenAPI> initialInstance;

  private static List<String> mergeNoDuplicates(List<String> a, List<String> b) {
    Set<String> set = new LinkedHashSet<>(a);
    set.addAll(b);
    List<String> asList = new ArrayList<>(set);
    Collections.sort(asList);
    return asList;
  }

  @Override
  public OpenAPI apply(List<? extends OpenApiV3Source> openApiList) {
    OpenAPI openapi = initialInstance().get();
    openApiList.forEach(
        o -> {
          combineServers(openapi, o);
          combineSecurities(openapi, o);
          combinePaths(openapi, o);
          combineComponents(openapi, o);
        });
    return openapi;
  }

  protected void combineComponents(OpenAPI current, OpenApiV3Source toCombine) {
    if (current.getComponents() == null) {
      current.components(toCombine.openApi().getComponents());
    } else {
      Components currentComponents = current.getComponents();
      Components components = toCombine.openApi().getComponents();
      combineSecuritySchemes(
          currentComponents.getSecuritySchemes(), components.getSecuritySchemes());
      currentComponents.schemas(
          mergeMapFavoringOriginal(
              currentComponents.getSchemas(), components.getSchemas(), "Schema"));

      // TODO following have not been analyzed
      currentComponents.responses(
          mergeMapFavoringOriginal(
              currentComponents.getResponses(), components.getResponses(), "ApiResponse"));
      currentComponents.parameters(
          mergeMapFavoringOriginal(
              currentComponents.getParameters(), components.getParameters(), "Parameter"));
      currentComponents.examples(
          mergeMapFavoringOriginal(
              currentComponents.getExamples(), components.getExamples(), "Example"));
      currentComponents.requestBodies(
          mergeMapFavoringOriginal(
              currentComponents.getRequestBodies(), components.getRequestBodies(), "RequestBody"));
      currentComponents.headers(
          mergeMapFavoringOriginal(
              currentComponents.getHeaders(), components.getHeaders(), "Header"));
      currentComponents.links(
          mergeMapFavoringOriginal(currentComponents.getLinks(), components.getLinks(), "Link"));
      currentComponents.callbacks(
          mergeMapFavoringOriginal(
              currentComponents.getCallbacks(), components.getCallbacks(), "Callback"));
      currentComponents.extensions(
          mergeMapFavoringOriginal(
              currentComponents.getExtensions(), components.getExtensions(), "Extension"));
    }
  }

  protected void combinePaths(OpenAPI current, OpenApiV3Source toCombine) {
    if (current.getPaths() == null) {
      current.paths(toCombine.openApi().getPaths());
    } else {
      toCombine
          .openApi()
          .getPaths()
          .keySet()
          .forEach(
              keyToAdd -> {
                if (current.getPaths().containsKey(keyToAdd)) {
                  throw new DuplicatePath(String.format("Path already exists: '%s'", keyToAdd));
                }
                current
                    .getPaths()
                    .addPathItem(keyToAdd, toCombine.openApi().getPaths().get(keyToAdd));
              });
      // re-sort on keys
      Paths sorted = new Paths();
      current.getPaths().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
      current.paths(sorted);
    }
  }

  protected void combineSecurities(OpenAPI current, OpenApiV3Source toCombine) {
    if (current.getSecurity() == null) {
      current.security(toCombine.openApi().getSecurity());
    } else if (toCombine.openApi().getSecurity() != null) {
      current
          .getSecurity()
          .forEach(
              currentSecurity ->
                  currentSecurity
                      .keySet()
                      .forEach(
                          currentKey -> {
                            Optional<SecurityRequirement> found =
                                findSecurity(toCombine.openApi().getSecurity(), currentKey);
                            // merge lists without duplicates
                            found.ifPresent(
                                securityRequirement ->
                                    currentSecurity.addList(
                                        currentKey,
                                        mergeNoDuplicates(
                                            currentSecurity.get(currentKey),
                                            securityRequirement.get(currentKey))));
                          }));
    }
  }

  protected void combineSecuritySchemes(
      Map<String, SecurityScheme> current, Map<String, SecurityScheme> toCombine) {
    if (toCombine == null) {
      return;
    }
    toCombine.forEach(
        (key, value) -> {
          if (!current.containsKey(key)) {
            current.put(key, value);
            return;
          }
          mergeSecuritySchemes(current.get(key), value);
        });
  }

  protected void combineServers(OpenAPI current, OpenApiV3Source toCombine) {
    if (current.getServers() == null) {
      current.servers(toCombine.openApi().getServers());
    } else if (toCombine.openApi().getServers() == null) {
      return;
    }
    current.servers(servers(current.getServers(), toCombine.openApi().getServers()));
  }

  protected Scopes findImplicitFlowScope(SecurityScheme securityScheme) {
    if (securityScheme.getFlows() != null && securityScheme.getFlows().getImplicit() != null) {
      return securityScheme.getFlows().getImplicit().getScopes();
    }
    return null;
  }

  protected Optional<SecurityRequirement> findSecurity(
      List<SecurityRequirement> securities, String name) {
    return securities.stream().filter(s -> s.get(name) != null).findFirst();
  }

  /**
   * Attempts to merge a map's entries into a target map. If key exists, an equality check is
   * performed.
   *
   * @throws DuplicateKey if a duplicate key is found and values are not equal.
   */
  protected <T> Map<String, T> mergeMapFavoringOriginal(
      Map<String, T> current, Map<String, T> toMerge, String prefix) {
    if (toMerge == null) {
      return current;
    }
    if (current == null) {
      return toMerge;
    }
    toMerge.forEach(
        (key, value) -> {
          if (!current.containsKey(key)) {
            println("%s Adding new Key %s", prefix, key);
            current.put(key, value);
          } else if (value.equals(current.get(key))) {
            println("%s Skipping conflicting key '%s' with identical value", prefix, key);
          } else {
            String message =
                String.format("%s CONFLICT key '%s' with non-equals values", prefix, key);
            println(message);
            throw new DuplicateKey(message);
          }
        });
    return current;
  }

  protected void mergeSecuritySchemes(SecurityScheme current, SecurityScheme toMerge) {
    if (toMerge == null) {
      return;
    }
    if (current == null) {
      return;
    }
    // TODO we're only merging the scopes for now.
    Scopes currentScopes = findImplicitFlowScope(current);
    Scopes toMergeScopes = findImplicitFlowScope(toMerge);
    mergeMapFavoringOriginal(currentScopes, toMergeScopes, "ImplicitScope");
  }

  protected boolean serverExists(List<Server> servers, Server server) {
    var found =
        servers.stream()
            .filter(s -> StringUtils.equalsIgnoreCase(s.getUrl(), server.getUrl()))
            .findFirst();
    return found.isPresent();
  }

  protected List<Server> servers(List<Server> current, List<Server> servers) {
    List<Server> serversToAdd =
        servers.stream().filter(s -> !serverExists(current, s)).collect(toList());
    current.addAll(serversToAdd);
    return current;
  }
}
