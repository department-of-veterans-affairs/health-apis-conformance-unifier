package gov.va.api.health.conformance.unifier.fhir.stu3;

import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.informational.stu3.capability.CapabilityResourcesProperties;
import gov.va.api.health.informational.stu3.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.stu3.capability.CapabilityUtilities;
import gov.va.api.health.stu3.api.resources.CapabilityStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Transformer to merge multiple STU3 CapabilityStatement Metadata into a single representative
 * CapabilityStatement Metadata.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Stu3CapabilityTransformer
    implements Function<List<CapabilityStatement>, CapabilityStatement> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  public CapabilityStatement apply(List<CapabilityStatement> capabilityList) {
    return combine(capabilityList);
  }

  private CapabilityStatement combine(List<CapabilityStatement> capabilityList) {
    CapabilityStatement capability =
        CapabilityUtilities.initializeCapabilityBuilder(
            "CapabilityStatement",
            capabilityStatementProperties,
            new CapabilityResourcesProperties(Collections.emptyList()));
    List<CapabilityStatement.RestResource> combinedList = new ArrayList<>();
    for (CapabilityStatement c : capabilityList) {
      for (CapabilityStatement.Rest r : c.rest()) {
        for (CapabilityStatement.RestResource resource : r.resource()) {
          // Check if resource already exists. If so this is an error and fail.
          if (combinedList.stream().anyMatch(o -> o.type().equals(resource.type()))) {
            throw new DuplicateCapabilityResourceException(
                "Found CapabilityStatement with duplicate resource type ["
                    + resource.type()
                    + "].");
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
