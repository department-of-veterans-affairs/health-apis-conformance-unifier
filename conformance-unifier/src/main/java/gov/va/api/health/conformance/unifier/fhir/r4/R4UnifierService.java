package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.BaseUnifierService;
import gov.va.api.health.r4.api.information.WellKnown;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate unification of R4 Metadata and WellKnown. */
@Service
public class R4UnifierService extends BaseUnifierService<CapabilityStatement, WellKnown> {

  /**
   * Construct a unifier service for R4 type resources.
   *
   * @param client Rest client.
   * @param s3ClientWriterService Amazon S3 writer service.
   * @param capabilityTransformer Capability transformer.
   * @param wellKnownTransformer WellKnown transformer.
   */
  @Autowired
  public R4UnifierService(
      ConformanceClient client,
      AmazonS3ClientWriterService s3ClientWriterService,
      R4CapabilityTransformer capabilityTransformer,
      R4WellKnownTransformer wellKnownTransformer) {
    super(client, capabilityTransformer, wellKnownTransformer, s3ClientWriterService);
  }

  @Override
  protected Query<CapabilityStatement> queryMetadata(final String url) {
    return Query.forType(CapabilityStatement.class).url(url).build();
  }

  @Override
  protected Query<WellKnown> queryWellKnown(final String url) {
    return Query.forType(WellKnown.class).url(url).build();
  }
}
