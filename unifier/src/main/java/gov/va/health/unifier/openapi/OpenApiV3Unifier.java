package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.Print.println;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicateKey;
import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicatePath;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
          println("Processing %s", o.name());
          combineServers(openapi, o);
          combineSecurities(openapi, o);
          combinePaths(openapi, o);
          combineComponents(openapi, o);
        });
    return openapi;
  }

  protected void combineComponents(OpenAPI current, OpenApiV3Source toCombine) {
    Components currentComponents = current.getComponents();
    if (currentComponents == null) {
      currentComponents = new Components();
      current.components(currentComponents);
    }
    Components components = toCombine.openApi().getComponents();
    combineSecuritySchemes(currentComponents, toCombine);
    combineSchemas(currentComponents, toCombine);
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

  protected void combinePaths(OpenAPI current, OpenApiV3Source toCombine) {
    Paths unsorted = current.getPaths() == null ? new Paths() : current.getPaths();
    toCombine.openApi().getPaths().keySet().stream()
        .filter(toCombine.pathFilter())
        .forEach(
            keyToAdd -> {
              if (unsorted.containsKey(keyToAdd)) {
                throw new DuplicatePath(String.format("Path already exists: '%s'", keyToAdd));
              }
              println("Adding path %s", keyToAdd);
              unsorted.addPathItem(keyToAdd, toCombine.openApi().getPaths().get(keyToAdd));
            });
    current.paths(sortByKeyIntoCopy(unsorted, Paths::new));
  }

  @SuppressWarnings("rawtypes")
  private void combineSchemas(Components currentComponents, OpenApiV3Source toCombine) {
    var schemas =
        toCombine.openApi().getComponents().getSchemas().entrySet().stream()
            .filter(e -> toCombine.schemaFilter().test(e.getKey()))
            .collect(toMap(Entry::getKey, Entry::getValue));
    var unsorted = mergeMapFavoringOriginal(currentComponents.getSchemas(), schemas, "Schema");
    currentComponents.schemas(sortByKeyIntoCopy(unsorted, LinkedHashMap::new));
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

  protected void combineSecuritySchemes(Components currentComponents, OpenApiV3Source toCombine) {
    if (currentComponents.getSecuritySchemes() == null) {
      currentComponents.securitySchemes(new LinkedHashMap<>());
    }
    if (toCombine == null) {
      return;
    }
    toCombine
        .openApi()
        .getComponents()
        .getSecuritySchemes()
        .forEach(
            (key, value) -> {
              var currentValue = currentComponents.getSecuritySchemes().get(key);
              if (currentValue == null) {
                currentComponents.getSecuritySchemes().put(key, value);
                return;
              }
              mergeSecuritySchemes(currentValue, value);
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
          var currentValue = current.get(key);
          if (currentValue == null) {
            println("%s Adding new Key %s", prefix, key);
            current.put(key, value);
          } else if (value.equals(currentValue)) {
            println("%s Skipping conflicting key '%s' with identical value", prefix, key);
          } else {
            String message =
                String.format("%s CONFLICT key '%s' with non-equals values", prefix, key);
            println(message);
            println("Current value");
            println(Json.pretty(currentValue));
            println("Attempting to add value");
            println(Json.pretty(value));
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
    return servers.stream().anyMatch(s -> equalsIgnoreCase(s.getUrl(), server.getUrl()));
  }

  protected List<Server> servers(List<Server> current, List<Server> servers) {
    List<Server> serversToAdd =
        servers.stream().filter(s -> !serverExists(current, s)).collect(toList());
    current.addAll(serversToAdd);
    return current;
  }

  protected <V, S extends Map<String, V>, D extends LinkedHashMap<String, V>> D sortByKeyIntoCopy(
      S unsorted, Supplier<D> destination) {
    D sorted = destination.get();
    unsorted.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
    return sorted;
  }
}
