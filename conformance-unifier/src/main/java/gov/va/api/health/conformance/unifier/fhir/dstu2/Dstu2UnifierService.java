package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.BaseUnifierService;
import gov.va.api.health.conformance.unifier.fhir.OpenApiTransformer;
import gov.va.api.health.dstu2.api.information.WellKnown;
import gov.va.api.health.dstu2.api.resources.Conformance;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate unification of DSTU2 Metadata and WellKnown. */
@Service
public class Dstu2UnifierService extends BaseUnifierService<Conformance, WellKnown, OpenAPI> {
  /**
   * Construct a unifier service for DSTU2 type resources.
   *
   * @param client Rest client.
   * @param s3ClientWriterService Amazon S3 writer service.
   * @param conformanceTransformer Conformance transformer.
   * @param wellKnownTransformer WellKnown transformer.
   */
  @Autowired
  public Dstu2UnifierService(
      ConformanceClient client,
      AmazonS3ClientWriterService s3ClientWriterService,
      Dstu2ConformanceTransformer conformanceTransformer,
      OpenApiTransformer openApiTransformer,
      Dstu2WellKnownTransformer wellKnownTransformer) {
    super(
        client,
        conformanceTransformer,
        wellKnownTransformer,
        openApiTransformer,
        s3ClientWriterService);
  }

  @Override
  protected Query<Conformance> queryMetadata(final String url) {
    return Query.forType(Conformance.class).url(url).build();
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
