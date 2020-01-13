package gov.va.api.health.conformance.unifier.fhir;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
   * Create a result s3 object name using the resource type and endpoint type.
   *
   * @param resourceTypeEnum Resource type.
   * @param endpointTypeEnum Endpoint type.
   * @return Object name.
   */
  private String objectName(
      final ResourceTypeEnum resourceTypeEnum, final EndpointTypeEnum endpointTypeEnum) {
    return resourceTypeEnum.type() + "-" + endpointTypeEnum.type();
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
   * @param resourceTypeEnum Type of resource.
   * @param endpointType String representing requested type of endpoint.
   * @param urlList List of URL to unify.
   * @param metadataMap Map of metadata to associate with the generated S3 object.
   */
  @SneakyThrows
  public void unify(
      final ResourceTypeEnum resourceTypeEnum,
      final String endpointType,
      final List<String> urlList,
      Map<String, String> metadataMap) {
    final EndpointTypeEnum endpointTypeEnum = EndpointTypeEnum.fromType(endpointType);
    final String objectName = objectName(resourceTypeEnum, endpointTypeEnum);
    switch (endpointTypeEnum) {
      case METADATA:
        unifyMetadata(objectName, urlList, metadataMap);
        break;
      case SMART_CONFIGURATION:
        unifyWellKnown(objectName, urlList, metadataMap);
        break;
      default:
        throw new IllegalArgumentException("Unsupported endpoint type: " + endpointType);
    }
  }

  /**
   * Unify metadata.
   *
   * @param objectName Name of S3 object.
   * @param urlList Urls.
   * @param metadataMap Map of metadata to associate with the generated S3 object.
   */
  private void unifyMetadata(
      final String objectName, final List<String> urlList, final Map<String, String> metadataMap) {
    List<T> metadataList = new ArrayList<>();
    for (String url : urlList) {
      metadataList.add(client.search(queryMetadata(url)));
    }
    s3ClientWriterService.writeToBucket(
        objectName, metadataMap, metadataTransformer.apply(metadataList), "application/fhir+json");
  }

  /**
   * Unify wellknown.
   *
   * @param objectName Name of S3 object.
   * @param urlList Urls.
   * @param metadataMap Map of metadata to associate with the generated S3 object.
   */
  private void unifyWellKnown(
      final String objectName, final List<String> urlList, final Map<String, String> metadataMap) {
    List<U> wellKnownList = new ArrayList<>();
    for (String url : urlList) {
      wellKnownList.add(client.search(queryWellKnown(url)));
    }
    s3ClientWriterService.writeToBucket(
        objectName, metadataMap, wellKnownTransformer.apply(wellKnownList), "application/json");
  }
}
