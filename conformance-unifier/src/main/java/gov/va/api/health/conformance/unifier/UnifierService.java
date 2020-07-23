package gov.va.api.health.conformance.unifier;

import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.dstu2.Dstu2UnifierService;
import gov.va.api.health.conformance.unifier.fhir.r4.R4UnifierService;
import gov.va.api.health.conformance.unifier.fhir.stu3.Stu3UnifierService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Unifier service that serves to route to appropriate resource specific unifier service based on
 * the resource type parameter.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UnifierService {

  /** DSTU2 unifier service. */
  private final Dstu2UnifierService dstu2UnifierService;

  /** STU3 unifier service. */
  private final Stu3UnifierService stu3UnifierService;

  /** R4 unifier service. */
  private final R4UnifierService r4UnifierService;

  /**
   * Unify the urlList of endpoint type.
   *
   * @param resourceType Type of resource.
   * @param endpointType String representing requested type of endpoint.
   * @param urlList List of URL to unify.
   * @param metadataMap Map of metadata to associate with the generated S3 object.
   */
  public void unify(
      final String resourceType,
      final String endpointType,
      final List<String> urlList,
      Map<String, String> metadataMap) {
    // Call appropriate unifier based on the resource type.
    switch (ResourceTypeEnum.fromType(resourceType)) {
      case R4:
        r4UnifierService.unify(ResourceTypeEnum.R4, endpointType, urlList, metadataMap);
        break;
      case DSTU2:
        dstu2UnifierService.unify(ResourceTypeEnum.DSTU2, endpointType, urlList, metadataMap);
        break;
      case STU3:
        stu3UnifierService.unify(ResourceTypeEnum.STU3, endpointType, urlList, metadataMap);
        break;
      default:
        throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
    }
  }
}
