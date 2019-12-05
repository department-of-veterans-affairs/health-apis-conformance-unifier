package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.conformance.unifier.exception.DuplicateCapabilityResourceException;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.informational.dstu2.conformance.ConformanceResourcesProperties;
import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
import gov.va.api.health.informational.dstu2.conformance.ConformanceUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Transformer to merge multiple DSTU2 Conformance Metadata into a single representative Conformance
 * Metadata.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2ConformanceTransformer implements Function<List<Conformance>, Conformance> {

  private final ConformanceStatementProperties conformanceStatementProperties;

  @Override
  public Conformance apply(List<Conformance> conformanceList) {
    return combine(conformanceList);
  }

  private Conformance combine(List<Conformance> conformanceList) {
    Conformance conformance =
        ConformanceUtilities.initializeConformanceBuilder(
            "Conformance",
            conformanceStatementProperties,
            new ConformanceResourcesProperties(Collections.emptyList()));
    List<Conformance.RestResource> combinedList = new ArrayList<>();
    for (Conformance c : conformanceList) {
      for (Conformance.Rest r : c.rest()) {
        for (Conformance.RestResource resource : r.resource()) {
          // Check if resource already exists. If so this is an error and fail.
          if (combinedList.stream().anyMatch(o -> o.type().equals(resource.type()))) {
            throw new DuplicateCapabilityResourceException(
                "Found Conformance with duplicate resource type [" + resource.type() + "].");
          }
          combinedList.add(resource);
        }
      }
    }
    // Assume only one rest entry to which we add all resources.
    conformance.rest().get(0).resource(combinedList);
    return conformance;
  }
}
