package gov.va.api.health.conformance.unifier.fhir.stu3;

import gov.va.api.health.informational.stu3.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.stu3.wellknown.WellKnownUtilities;
import gov.va.api.health.stu3.api.information.WellKnown;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Transformer to merge multiple STU3 WellKnown into a single representative WellKnown. */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Stu3WellKnownTransformer implements Function<List<WellKnown>, WellKnown> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  public WellKnown apply(List<WellKnown> wellKnownList) {
    return combine(wellKnownList);
  }

  private WellKnown combine(List<WellKnown> wellKnownList) {
    WellKnown wellKnown =
        WellKnownUtilities.initializeWellKnownBuilder(capabilityStatementProperties, null);

    List<String> combinedCapabilitiesList = new ArrayList<>();
    List<String> combinedResponseList = new ArrayList<>();
    List<String> combinedScopesList = new ArrayList<>();

    for (WellKnown w : wellKnownList) {
      combinedCapabilitiesList.addAll(w.capabilities());
      combinedResponseList.addAll(w.responseTypeSupported());
      combinedScopesList.addAll(w.scopesSupported());
    }

    wellKnown.capabilities(
        combinedCapabilitiesList.stream().distinct().collect(Collectors.toList()));
    wellKnown.responseTypeSupported(
        combinedResponseList.stream().distinct().collect(Collectors.toList()));
    wellKnown.scopesSupported(combinedScopesList.stream().distinct().collect(Collectors.toList()));

    return wellKnown;
  }
}
