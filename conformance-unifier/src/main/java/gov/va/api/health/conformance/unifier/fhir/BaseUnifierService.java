package gov.va.api.health.conformance.unifier.fhir;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class services must implement to facilitate unification of Metadata and WellKnown for
 * implementor respective resource type.
 */
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Slf4j
public abstract class BaseUnifierService {

  /** Benefits claims client. */
  protected final ConformanceClient client;

  private final AmazonS3ClientWriterService s3ClientWriterService;

  /**
   * Translate metadata.
   *
   * @param urlList Urls.
   * @return Capability object.
   */
  protected abstract Object translateMetadata(final List<String> urlList);

  /**
   * Translate wellknown.
   *
   * @param urlList Urls.
   * @return R4 WellKnown object.
   */
  protected abstract Object translateWellKnown(final List<String> urlList);

  /**
   * Unify the urlList of endpoint type.
   *
   * @param endpointType Type of endpoint.
   * @param urlList List of URL to unify.
   */
  @SneakyThrows
  public void unify(final String endpointType, final List<String> urlList) {
    switch (EndpointTypeEnum.fromType(endpointType)) {
      case METADATA:
        unifyMetadata(urlList);
        break;
      case WELLKNOWN:
        unifyWellKnown(urlList);
        break;
      default:
        throw new IllegalArgumentException("Unsupported endpoint type: " + endpointType);
    }
  }

  /**
   * Unify.
   *
   * @param urlList Urls.
   * @return Capability.
   */
  private void unifyMetadata(final List<String> urlList) {
    s3ClientWriterService.writeToBucket(translateMetadata(urlList));
  }

  private void unifyWellKnown(final List<String> urlList) {
    s3ClientWriterService.writeToBucket(translateWellKnown(urlList));
  }
}
