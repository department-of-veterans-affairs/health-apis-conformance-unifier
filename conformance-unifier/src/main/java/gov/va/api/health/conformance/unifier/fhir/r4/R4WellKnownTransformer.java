package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.fhir.BaseWellKnownTransformer;
import gov.va.api.health.fhir.api.Safe;
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
    WellKnown wellKnown =
        WellKnownUtilities.initializeWellKnownBuilder(capabilityStatementProperties, null);
    wellKnown.authorizationEndpoint2(wellKnown.authorizationEndpoint());
    wellKnown.tokenEndpoint2(wellKnown.tokenEndpoint());
    wellKnown.managementEndpoint2(wellKnown.managementEndpoint());
    wellKnown.revocationEndpoint2(wellKnown.revocationEndpoint());
    return wellKnown;
  }

  @Override
  protected void setCapabilities(WellKnown wellKnown, List<String> capabilityList) {
    wellKnown.capabilities(capabilityList);
  }

  @Override
  protected void setResponse(WellKnown wellKnown, List<String> responseList) {
    wellKnown.responseTypeSupported(responseList);
    wellKnown.responseTypesSupported(responseList);
  }

  @Override
  protected void setScopes(WellKnown wellKnown, List<String> scopeList) {
    wellKnown.scopesSupported(scopeList);
    wellKnown.scopesSupported2(scopeList);
  }
}
