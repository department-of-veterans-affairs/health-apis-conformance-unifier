package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.informational.r4.capability.CapabilityResourcesProperties;
import gov.va.api.health.informational.r4.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.r4.capability.CapabilityUtilities;
import gov.va.api.health.informational.r4.capability.MetadataCapabilityStatementModeEnum;
import gov.va.api.health.r4.api.resources.Capability;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Transformer to merge multiple R4 Capability Metadata into a single representative Capability
 * Metadata.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class R4CapabilityTransformer implements Function<List<Capability>, Capability> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  public Capability apply(List<Capability> capabilityList) {
    return combine(capabilityList);
  }

  private Capability combine(List<Capability> capabilityList) {
    Capability capability =
        CapabilityUtilities.initializeCapabilityBuilder(
            MetadataCapabilityStatementModeEnum.FULL.getResourceType(),
            capabilityStatementProperties,
            new CapabilityResourcesProperties(Collections.emptyList()));
    List<Capability.CapabilityResource> combinedList = new ArrayList<>();
    for (Capability c : capabilityList) {
      for (Capability.Rest r : c.rest()) {
        for (Capability.CapabilityResource resource : r.resource()) {
          // Check if resource already exists. If so this is an error and fail.
          if (combinedList.stream().anyMatch(o -> o.type().equals(resource.type()))) {
            throw new DuplicateCapabilityResourceException(
                "Found Capability with duplicate resource type [" + resource.type() + "].");
          }
          combinedList.add(resource);
        }
      }
    }
    // Assume only one rest entry to which we add all resources.
    capability.rest().get(0).resource(combinedList);
    return capability;
  }
}
