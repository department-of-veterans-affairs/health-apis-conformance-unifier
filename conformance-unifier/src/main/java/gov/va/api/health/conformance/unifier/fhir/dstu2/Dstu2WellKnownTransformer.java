package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.dstu2.api.information.WellKnown;
import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
import gov.va.api.health.informational.dstu2.wellknown.WellKnownUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Transformer to merge multiple DSTU2 WellKnown into a single representative WellKnown. */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2WellKnownTransformer implements Function<List<WellKnown>, WellKnown> {

  private final ConformanceStatementProperties conformanceStatementProperties;

  @Override
  public WellKnown apply(List<WellKnown> wellKnownList) {
    return combine(wellKnownList);
  }

  private WellKnown combine(List<WellKnown> wellKnownList) {
    WellKnown wellKnown =
        WellKnownUtilities.initializeWellKnownBuilder(conformanceStatementProperties, null);

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
