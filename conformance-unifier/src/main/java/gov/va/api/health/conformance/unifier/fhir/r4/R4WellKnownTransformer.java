package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.fhir.BaseWellKnownTransformer;
import gov.va.api.health.informational.r4.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.r4.wellknown.WellKnownUtilities;
import gov.va.api.health.r4.api.information.WellKnown;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Transformer to merge multiple R4 WellKnown into a single representative WellKnown. */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class R4WellKnownTransformer extends BaseWellKnownTransformer<WellKnown> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  protected void addCapabilities(WellKnown wellKnown, List<String> capabilityList) {
    capabilityList.addAll(wellKnown.capabilities());
  }

  @Override
  protected void addResponse(WellKnown wellKnown, List<String> responseList) {
    responseList.addAll(wellKnown.responseTypeSupported());
  }

  @Override
  protected void addScopes(WellKnown wellKnown, List<String> scopeList) {
    scopeList.addAll(wellKnown.scopesSupported());
  }

  @Override
  protected WellKnown initialInstance() {
    return WellKnownUtilities.initializeWellKnownBuilder(capabilityStatementProperties, null);
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
