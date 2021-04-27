package gov.va.api.health.conformance.unifier.fhir.stu3;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.BaseUnifierService;
import gov.va.api.health.conformance.unifier.fhir.OpenApiTransformer;
import gov.va.api.health.stu3.api.information.WellKnown;
import gov.va.api.health.stu3.api.resources.CapabilityStatement;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate unification of STU3 Metadata and WellKnown. */
@Service
public class Stu3UnifierService
    extends BaseUnifierService<CapabilityStatement, WellKnown, OpenAPI> {
  /**
   * Construct a unifier service for STU3 type resources.
   *
   * @param client Rest client.
   * @param s3ClientWriterService Amazon S3 writer service.
   * @param capabilityTransformer Capability transformer.
   * @param wellKnownTransformer WellKnown transformer.
   */
  @Autowired
  public Stu3UnifierService(
      ConformanceClient client,
      AmazonS3ClientWriterService s3ClientWriterService,
      Stu3CapabilityTransformer capabilityTransformer,
      OpenApiTransformer openApiTransformer,
      Stu3WellKnownTransformer wellKnownTransformer) {
    super(
        client,
        capabilityTransformer,
        wellKnownTransformer,
        openApiTransformer,
        s3ClientWriterService);
  }

  @Override
  protected Query<CapabilityStatement> queryMetadata(final String url) {
    return Query.forType(CapabilityStatement.class).url(url).build();
  }

  @Override
  protected Query<OpenAPI> queryOpenApi(String url) {
    return Query.forType(OpenAPI.class).url(url).build();
  }

  @Override
  protected Query<WellKnown> queryWellKnown(final String url) {
    return Query.forType(WellKnown.class).url(url).build();
  }
}
