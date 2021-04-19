package gov.va.api.health.conformance.unifier.fhir;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.conformance.unifier.exception.OpenApiDuplicateKeyConflictException;
import gov.va.api.health.conformance.unifier.exception.OpenApiPathDuplicateException;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}), access = AccessLevel.PROTECTED)
public abstract class BaseOpenApiTransformer implements Function<List<OpenAPI>, OpenAPI> {
  protected static List<String> mergeNoDuplicates(List<String> a, List<String> b) {
    Set<String> set = new LinkedHashSet<>(a);
    set.addAll(b);
    List<String> asList = new ArrayList<>(set);
    Collections.sort(asList);
    return asList;
  }

  @Override
  public OpenAPI apply(List<OpenAPI> openApiList) {
    OpenAPI openapi = initialInstance();
    openApiList.forEach(
        o -> {
          combineServers(openapi, o);
          combineSecurities(openapi, o);
          combinePaths(openapi, o);
          combineComponents(openapi, o);
        });
    return openapi;
  }

  protected void combineComponents(OpenAPI current, OpenAPI toCombine) {
    if (current.getComponents() == null) {
      current.components(toCombine.getComponents());
    } else {
      Components currentComponents = current.getComponents();
      Components components = toCombine.getComponents();
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

  protected void combinePaths(OpenAPI current, OpenAPI toCombine) {
    if (current.getPaths() == null) {
      current.paths(toCombine.getPaths());
    } else {
      toCombine
          .getPaths()
          .keySet()
          .forEach(
              keyToAdd -> {
                if (current.getPaths().containsKey(keyToAdd)) {
                  throw new OpenApiPathDuplicateException(
                      String.format("Path already exists: '%s'", keyToAdd));
                }
                current.getPaths().addPathItem(keyToAdd, toCombine.getPaths().get(keyToAdd));
              });
      // re-sort on keys
      Paths sorted = new Paths();
      current.getPaths().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
      current.paths(sorted);
    }
  }

  protected void combineSecurities(OpenAPI current, OpenAPI toCombine) {
    if (current.getSecurity() == null) {
      current.security(toCombine.getSecurity());
    } else if (toCombine.getSecurity() != null) {
      current
          .getSecurity()
          .forEach(
              currentSecurity -> {
                currentSecurity
                    .keySet()
                    .forEach(
                        currentKey -> {
                          Optional<SecurityRequirement> found =
                              findSecurity(toCombine.getSecurity(), currentKey);
                          if (found.isPresent()) {
                            // merge lists without duplicates
                            currentSecurity.addList(
                                currentKey,
                                mergeNoDuplicates(
                                    currentSecurity.get(currentKey), found.get().get(currentKey)));
                          }
                        });
              });
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

  protected void combineServers(OpenAPI current, OpenAPI toCombine) {
    if (current.getServers() == null) {
      current.servers(toCombine.getServers());
    } else {
      current.servers(servers(current.getServers(), toCombine.getServers()));
    }
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

  protected abstract OpenAPI initialInstance();

  /**
   * Attempts to merge a map's entries into a target map. If key exists, an equality check is
   * performed.
   *
   * @throws OpenApiDuplicateKeyConflictException if a duplicate key is found and values are not
   *     equal.
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
            log.warn("{} Adding new Key {}", prefix, key);
            current.put(key, value);
          } else if (value.equals(current.get(key))) {
            log.warn("{} Skipping conflicting key '{}' with identical value", prefix, key);
          } else {
            String errMsg =
                String.format("%s CONFLICT key '%s' with non-equals values", prefix, key);
            log.error(errMsg);
            throw new OpenApiDuplicateKeyConflictException(errMsg);
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
