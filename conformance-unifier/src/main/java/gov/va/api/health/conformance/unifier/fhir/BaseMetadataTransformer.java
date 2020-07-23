package gov.va.api.health.conformance.unifier.fhir;

import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Transformer to merge multiple Metadata into a single representative Metadata. Implementing
 * transformer provides the types T:Capability, R:Rest, and C:Resource.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}), access = AccessLevel.PROTECTED)
public abstract class BaseMetadataTransformer<T, R, C> implements Function<List<T>, T> {

  @Override
  public T apply(List<T> capabilityList) {
    T capability = initialInstance();
    List<C> combinedList = new ArrayList<>();
    capabilityList.forEach(
        c -> {
          rest(c)
              .forEach(
                  r -> {
                    for (C resource : resource(r)) {
                      // Check if resource already exists. If so this is an error and fail.
                      final String type = resourceType(resource);
                      if (combinedList.stream().anyMatch(o -> typeEquals(o, type))) {
                        throw new DuplicateCapabilityResourceException(
                            "Found Metadata with duplicate resource type [" + type + "].");
                      }
                      combinedList.add(resource);
                    }
                  });
        });
    // Assume only one rest entry (the first and only in the list) to which we add all resources.
    setResources(rest(capability).get(0), combinedList);
    return capability;
  }

  /**
   * Implementing transformer to implement initial instance of metadata for which to add unified
   * resources.
   *
   * @return Initial instance of metadata.
   */
  protected abstract T initialInstance();

  /**
   * Implementing transformer would return list of resources for the provided rest instance.
   *
   * @param rest Rest object.
   * @return List of resources.
   */
  protected abstract List<C> resource(R rest);

  /**
   * Implementing transformer would return the resource type for the provided resource.
   *
   * @param resource Resource object.
   * @return Resource type.
   */
  protected abstract String resourceType(C resource);

  /**
   * Implementing transformer would return a list of rest obtained from the provided capability.
   *
   * @param capability Metadata capability.
   * @return List of rest.
   */
  protected abstract List<R> rest(T capability);

  /**
   * Set the rest object with the list of provided unified resources.
   *
   * @param rest Rest object to modify.
   * @param resourceList Resource list to add to rest object.
   */
  protected abstract void setResources(R rest, List<C> resourceList);

  /**
   * Check if the resource instance matches the specified type.
   *
   * @param resource Resource to check.
   * @param type The type to compare against.
   * @return Boolean if the type equals.
   */
  protected abstract boolean typeEquals(C resource, final String type);
}
