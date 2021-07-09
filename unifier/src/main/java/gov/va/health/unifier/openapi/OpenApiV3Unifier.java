package gov.va.health.unifier.openapi;

import static gov.va.health.unifier.Print.println;
import static java.util.stream.Collectors.toMap;

import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicateKey;
import gov.va.health.unifier.openapi.OpenApiV3Exceptions.DuplicatePath;
import gov.va.health.unifier.openapi.OpenApiV3Source.Filter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
import java.util.stream.Collectors;
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
      current.security(new ArrayList<>(1));
    }
    if (toCombine.openApi().getSecurity() == null) {
      return;
    }

    toCombine
        .openApi()
        .getSecurity()
        .forEach(toCombineValue -> combineSecuritiesItem(current, toCombine, toCombineValue));
  }

  private void combineSecuritiesItem(
      OpenAPI current, OpenApiV3Source toCombine, SecurityRequirement toCombineValue) {
    toCombineValue.forEach(
        (name, scopes) -> combineSecuritiesItemScopes(current, toCombine, name, scopes));
  }

  private void combineSecuritiesItemScopes(
      OpenAPI current, OpenApiV3Source toCombine, String name, List<String> scopes) {
    Optional<SecurityRequirement> maybeCurrentSecurityRequirement =
        findSecurity(current.getSecurity(), name);
    SecurityRequirement currentSecurityRequirement;
    if (maybeCurrentSecurityRequirement.isEmpty()) {
      currentSecurityRequirement = new SecurityRequirement();
      current.getSecurity().add(currentSecurityRequirement);
    } else {
      currentSecurityRequirement = maybeCurrentSecurityRequirement.get();
    }
    List<String> currentScopes = currentSecurityRequirement.getOrDefault(name, new ArrayList<>());
    List<String> combineFilteredScopes =
        scopes.stream().filter(toCombine.scopeFilter()).collect(Collectors.toList());
    currentSecurityRequirement.put(name, mergeNoDuplicates(currentScopes, combineFilteredScopes));
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
            (schemeName, securityScheme) -> {
              var currentValue = currentComponents.getSecuritySchemes().get(schemeName);
              if (currentValue == null) {
                currentComponents.getSecuritySchemes().put(schemeName, securityScheme);
                var scopes = findFlowScopes(securityScheme);
                if (scopes != null) {
                  var filteredScopes = filteredCopy(scopes, toCombine.scopeFilter());
                  securityScheme.getFlows().getAuthorizationCode().scopes(filteredScopes);
                }
                return;
              }
              mergeSecuritySchemes(currentValue, toCombine, securityScheme);
            });
  }

  private Scopes filteredCopy(Scopes scopes, Filter filter) {
    if (scopes == null) {
      return null;
    }
    Scopes filteredScopes = new Scopes();
    scopes.entrySet().stream()
        .filter(e -> filter.test(e.getKey()))
        .forEach(e -> filteredScopes.addString(e.getKey(), e.getValue()));
    return filteredScopes;
  }

  protected Scopes findFlowScopes(SecurityScheme securityScheme) {
    if (securityScheme.getFlows() == null
        || securityScheme.getFlows().getAuthorizationCode() == null) {
      return null;
    }
    return securityScheme.getFlows().getAuthorizationCode().getScopes();
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

  protected void mergeSecuritySchemes(
      SecurityScheme current, OpenApiV3Source toCombine, SecurityScheme toCombineValue) {
    if (toCombineValue == null) {
      return;
    }
    if (current == null) {
      return;
    }
    // TODO we're only merging the scopes for now.
    Scopes currentScopes = findFlowScopes(current);
    Scopes toMergeScopes = filteredCopy(findFlowScopes(toCombineValue), toCombine.scopeFilter());
    mergeMapFavoringOriginal(currentScopes, toMergeScopes, "AuthorizationCode");
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
