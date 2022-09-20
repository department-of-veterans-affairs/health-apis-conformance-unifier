package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.conformance.unifier.fhir.BaseWellKnownTransformer;
import gov.va.api.health.dstu2.api.information.WellKnown;
import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
import gov.va.api.health.informational.dstu2.wellknown.WellKnownUtilities;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Transformer to merge multiple DSTU2 WellKnown into a single representative WellKnown. */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2WellKnownTransformer extends BaseWellKnownTransformer<WellKnown> {

  private final ConformanceStatementProperties conformanceStatementProperties;

  @Override
  protected void addCapabilities(WellKnown wellKnown, List<String> capabilityList) {
    capabilityList.addAll(Safe.list(wellKnown.capabilities()));
  }

  @Override
  protected void addResponse(WellKnown wellKnown, List<String> responseList) {
    responseList.addAll(Safe.list(wellKnown.responseTypeSupported()));
  }

  @Override
  protected void addScopes(WellKnown wellKnown, List<String> scopeList) {
    scopeList.addAll(Safe.list(wellKnown.scopesSupported()));
  }

  @Override
  protected WellKnown initialInstance() {
    return WellKnownUtilities.initializeWellKnownBuilder(conformanceStatementProperties, null);
  }

  @Override
  protected void setCapabilities(WellKnown wellKnown, List<String> capabilityList) {
    wellKnown.capabilities(capabilityList);
  }

  @Override
  protected void setResponse(WellKnown wellKnown, List<String> responseList) {
    wellKnown.responseTypeSupported(responseList);
  }

  @Override
  protected void setScopes(WellKnown wellKnown, List<String> scopeList) {
    wellKnown.scopesSupported(scopeList);
  }
}
