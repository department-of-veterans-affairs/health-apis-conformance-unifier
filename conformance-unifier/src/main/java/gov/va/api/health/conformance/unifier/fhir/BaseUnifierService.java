package gov.va.api.health.conformance.unifier.fhir;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseUnifierService<T, U> {

  private final ConformanceClient client;

  private final Function<List<T>, T> metadataTransformer;

  private final Function<List<U>, U> wellKnownTransformer;

  private final AmazonS3ClientWriterService s3ClientWriterService;

  /**
   * Base name for metadata object name.
   *
   * @return Base name.
   */
  protected String baseMetadataName() {
    return "capability";
  }

  /**
   * Base name for wellknown object name.
   *
   * @return Base name.
   */
  protected String baseWellKnownName() {
    return "wellknown";
  }

  /**
   * Create a result s3 object name using the resource type and a base name associated with the
   * resource type.
   *
   * @param resourceTypeEnum Resource type.
   * @param baseName Base name.
   * @return Object name.
   */
  private String objectName(final ResourceTypeEnum resourceTypeEnum, final String baseName) {
    return resourceTypeEnum.type() + "-" + baseName;
  }

  /**
   * Query metadata.
   *
   * @param url Url to query.
   * @return Query object.
   */
  protected abstract Query<T> queryMetadata(final String url);

  /**
   * Query wellknown.
   *
   * @param url Url to query.
   * @return Query object.
   */
  protected abstract Query<U> queryWellKnown(final String url);

  /**
   * Unify the urlList of endpoint type.
   *
   * @param endpointType Type of endpoint.
   * @param urlList List of URL to unify.
   */
  @SneakyThrows
  public void unify(
      final ResourceTypeEnum resourceTypeEnum,
      final String endpointType,
      final List<String> urlList) {
    switch (EndpointTypeEnum.fromType(endpointType)) {
      case METADATA:
        unifyMetadata(objectName(resourceTypeEnum, baseMetadataName()), urlList);
        break;
      case SMART_CONFIGURATION:
        unifyWellKnown(objectName(resourceTypeEnum, baseWellKnownName()), urlList);
        break;
      default:
        throw new IllegalArgumentException("Unsupported endpoint type: " + endpointType);
    }
  }

  /**
   * Unify metadata.
   *
   * @param urlList Urls.
   */
  private void unifyMetadata(final String objectName, final List<String> urlList) {
    List<T> metadataList = new ArrayList<>();
    for (String url : urlList) {
      metadataList.add(client.search(queryMetadata(url)));
    }
    s3ClientWriterService.writeToBucket(objectName, metadataTransformer.apply(metadataList));
  }

  /**
   * Unify wellknown.
   *
   * @param urlList Urls.
   */
  private void unifyWellKnown(final String objectName, final List<String> urlList) {
    List<U> wellKnownList = new ArrayList<>();
    for (String url : urlList) {
      wellKnownList.add(client.search(queryWellKnown(url)));
    }
    s3ClientWriterService.writeToBucket(objectName, wellKnownTransformer.apply(wellKnownList));
  }
}
