package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.fhir.BaseMetadataTransformer;
import gov.va.api.health.informational.r4.capability.CapabilityResourcesProperties;
import gov.va.api.health.informational.r4.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.r4.capability.CapabilityUtilities;
import gov.va.api.health.informational.r4.capability.MetadataCapabilityStatementModeEnum;
import gov.va.api.health.r4.api.resources.Capability;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Transformer to merge multiple R4 Capability Metadata into a single representative Capability
 * Metadata.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class R4CapabilityTransformer
    extends BaseMetadataTransformer<Capability, Capability.Rest, Capability.CapabilityResource> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  protected Capability initialInstance() {
    return CapabilityUtilities.initializeCapabilityBuilder(
        MetadataCapabilityStatementModeEnum.FULL.getResourceType(),
        capabilityStatementProperties,
        new CapabilityResourcesProperties(Collections.emptyList()));
  }

  @Override
  protected List<Capability.CapabilityResource> resource(Capability.Rest rest) {
    return rest.resource();
  }

  @Override
  protected String resourceType(Capability.CapabilityResource resource) {
    return resource.type();
  }

  @Override
  protected List<Capability.Rest> rest(Capability capability) {
    return capability.rest();
  }

  @Override
  protected void setResources(
      Capability.Rest rest, List<Capability.CapabilityResource> resourceList) {
    rest.resource(resourceList);
  }

  @Override
  protected boolean typeEquals(Capability.CapabilityResource resource, final String type) {
    return resource.type().equalsIgnoreCase(type);
  }
}
