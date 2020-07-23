package gov.va.api.health.conformance.unifier.fhir;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transformer to merge multiple WellKnown into a single representative WellKnown. Implementing
 * transformer provides the types T:Capability.
 */
public abstract class BaseWellKnownTransformer<T> implements Function<List<T>, T> {

  /**
   * Implementing transformer to add all capabilities from the wellknown instance to the provided
   * list.
   *
   * @param wellKnown The wellknown to reference.
   * @param capabilityList The list of capability to add to.
   */
  protected abstract void addCapabilities(T wellKnown, List<String> capabilityList);

  /**
   * Implementing transformer to add all response from the wellknown instance to the provided list.
   *
   * @param wellKnown The wellknown to reference.
   * @param responseList The list of response to add to.
   */
  protected abstract void addResponse(T wellKnown, List<String> responseList);

  /**
   * Implementing transformer to add all scopes from the wellknown instance to the provided list.
   *
   * @param wellKnown The wellknown to reference.
   * @param scopeList The list of scopes to add to.
   */
  protected abstract void addScopes(T wellKnown, List<String> scopeList);

  @Override
  public T apply(List<T> wellKnownList) {
    T wellKnown = initialInstance();
    List<String> combinedCapabilitiesList = new ArrayList<>();
    List<String> combinedResponseList = new ArrayList<>();
    List<String> combinedScopesList = new ArrayList<>();
    wellKnownList.stream()
        .map(
            w -> {
              addCapabilities(w, combinedCapabilitiesList);
              return w;
            })
        .map(
            w -> {
              addResponse(w, combinedResponseList);
              return w;
            })
        .forEachOrdered(
            w -> {
              addScopes(w, combinedScopesList);
            });
    // Make sure the lists are unique (contain no duplicates).
    setCapabilities(
        wellKnown, combinedCapabilitiesList.stream().distinct().collect(Collectors.toList()));
    setResponse(wellKnown, combinedResponseList.stream().distinct().collect(Collectors.toList()));
    setScopes(wellKnown, combinedScopesList.stream().distinct().collect(Collectors.toList()));
    return wellKnown;
  }

  /**
   * Implementing transformer to implement initial instance of wellknown for which to add unified
   * lists.
   *
   * @return Initial instance of wellknown.
   */
  protected abstract T initialInstance();

  /**
   * Implementing transformer to set the wellknown to the provided unified list of capability.
   *
   * @param wellKnown The wellknown to modify.
   * @param capabilityList The unified list of capability.
   */
  protected abstract void setCapabilities(T wellKnown, List<String> capabilityList);

  /**
   * Implementing transformer to set the wellknown to the provided unified list of responses.
   *
   * @param wellKnown The wellknown to modify.
   * @param responseList The unified list of responses.
   */
  protected abstract void setResponse(T wellKnown, List<String> responseList);

  /**
   * Implementing transformer to set the wellknown to the provided unified list of scopes.
   *
   * @param wellKnown The wellknown to modify.
   * @param scopeList The unified list of scopes.
   */
  protected abstract void setScopes(T wellKnown, List<String> scopeList);
}
