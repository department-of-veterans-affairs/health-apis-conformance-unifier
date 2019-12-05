package gov.va.api.health.conformance.unifier.fhir.stu3;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.BaseUnifierService;
import gov.va.api.health.stu3.api.information.WellKnown;
import gov.va.api.health.stu3.api.resources.CapabilityStatement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate unification of STU3 Metadata and WellKnown. */
@Service
public class Stu3UnifierService extends BaseUnifierService {

  private final Stu3CapabilityTransformer capabilityTransformer;
  private final Stu3WellKnownTransformer wellKnownTransformer;

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
      Stu3WellKnownTransformer wellKnownTransformer) {
    super(client, s3ClientWriterService);
    this.capabilityTransformer = capabilityTransformer;
    this.wellKnownTransformer = wellKnownTransformer;
  }

  @Override
  protected Object translateMetadata(final List<String> urlList) {

    List<CapabilityStatement> capabilityList = new ArrayList<>();
    for (String url : urlList) {
      capabilityList.add(client.search(Query.forType(CapabilityStatement.class).url(url).build()));
    }

    return capabilityTransformer.apply(capabilityList);
  }

  @Override
  protected Object translateWellKnown(final List<String> urlList) {

    List<WellKnown> wellKnownList = new ArrayList<>();
    for (String url : urlList) {
      wellKnownList.add(client.search(Query.forType(WellKnown.class).url(url).build()));
    }

    return wellKnownTransformer.apply(wellKnownList);
  }
}
