package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.conformance.unifier.fhir.BaseMetadataTransformer;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.informational.dstu2.conformance.ConformanceResourcesProperties;
import gov.va.api.health.informational.dstu2.conformance.ConformanceStatementProperties;
import gov.va.api.health.informational.dstu2.conformance.ConformanceUtilities;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Transformer to merge multiple DSTU2 Conformance Metadata into a single representative Conformance
 * Metadata.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2ConformanceTransformer
    extends BaseMetadataTransformer<Conformance, Conformance.Rest, Conformance.RestResource> {

  private final ConformanceStatementProperties conformanceStatementProperties;

  @Override
  protected Conformance initialInstance() {
    return ConformanceUtilities.initializeConformanceBuilder(
        "Conformance",
        conformanceStatementProperties,
        new ConformanceResourcesProperties(Collections.emptyList()));
  }

  @Override
  protected List<Conformance.RestResource> resource(Conformance.Rest rest) {
    return rest.resource();
  }

  @Override
  protected String resourceType(Conformance.RestResource resource) {
    return resource.type();
  }

  @Override
  protected List<Conformance.Rest> rest(Conformance capability) {
    return capability.rest();
  }

  @Override
  protected void setResources(Conformance.Rest rest, List<Conformance.RestResource> resourceList) {
    rest.resource(resourceList);
  }

  @Override
  protected boolean typeEquals(Conformance.RestResource resource, final String type) {
    return resource.type().equalsIgnoreCase(type);
  }
}
