package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.fhir.BaseMetadataTransformer;
import gov.va.api.health.informational.r4.capability.CapabilityResourcesProperties;
import gov.va.api.health.informational.r4.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.r4.capability.CapabilityUtilities;
import gov.va.api.health.informational.r4.capability.MetadataCapabilityStatementModeEnum;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
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
    extends BaseMetadataTransformer<
        CapabilityStatement, CapabilityStatement.Rest, CapabilityStatement.CapabilityResource> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  protected CapabilityStatement initialInstance() {
    return CapabilityUtilities.initializeCapabilityStatementBuilder(
        MetadataCapabilityStatementModeEnum.FULL.getResourceType(),
        capabilityStatementProperties,
        new CapabilityResourcesProperties(Collections.emptyList()));
  }

  @Override
  protected List<CapabilityStatement.CapabilityResource> resource(CapabilityStatement.Rest rest) {
    return rest.resource();
  }

  @Override
  protected String resourceType(CapabilityStatement.CapabilityResource resource) {
    return resource.type();
  }

  @Override
  protected List<CapabilityStatement.Rest> rest(CapabilityStatement capability) {
    return capability.rest();
  }

  @Override
  protected void setResources(
      CapabilityStatement.Rest rest, List<CapabilityStatement.CapabilityResource> resourceList) {
    rest.resource(resourceList);
  }

  @Override
  protected boolean typeEquals(CapabilityStatement.CapabilityResource resource, final String type) {
    return resource.type().equalsIgnoreCase(type);
  }
}
