package gov.va.api.health.conformance.unifier.fhir.stu3;

import gov.va.api.health.conformance.unifier.fhir.BaseMetadataTransformer;
import gov.va.api.health.informational.stu3.capability.CapabilityResourcesProperties;
import gov.va.api.health.informational.stu3.capability.CapabilityStatementProperties;
import gov.va.api.health.informational.stu3.capability.CapabilityUtilities;
import gov.va.api.health.stu3.api.resources.CapabilityStatement;
import java.util.Collections;
import java.util.List;
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
    extends BaseMetadataTransformer<
        CapabilityStatement, CapabilityStatement.Rest, CapabilityStatement.RestResource> {

  private final CapabilityStatementProperties capabilityStatementProperties;

  @Override
  protected CapabilityStatement initialInstance() {
    return CapabilityUtilities.initializeCapabilityBuilder(
        "CapabilityStatement",
        capabilityStatementProperties,
        new CapabilityResourcesProperties(Collections.emptyList()));
  }

  @Override
  protected List<CapabilityStatement.RestResource> resource(CapabilityStatement.Rest rest) {
    return rest.resource();
  }

  @Override
  protected String resourceType(CapabilityStatement.RestResource resource) {
    return resource.type();
  }

  @Override
  protected List<CapabilityStatement.Rest> rest(CapabilityStatement capability) {
    return capability.rest();
  }

  @Override
  protected void setResources(
      CapabilityStatement.Rest rest, List<CapabilityStatement.RestResource> resourceList) {
    rest.resource(resourceList);
  }

  @Override
  protected boolean typeEquals(CapabilityStatement.RestResource resource, final String type) {
    return resource.type().equalsIgnoreCase(type);
  }
}
