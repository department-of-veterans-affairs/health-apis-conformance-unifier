package gov.va.api.health.conformance.unifier.fhir.r4;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.BaseUnifierService;
import gov.va.api.health.r4.api.information.WellKnown;
import gov.va.api.health.r4.api.resources.Capability;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate unification of R4 Metadata and WellKnown. */
@Service
public class R4UnifierService extends BaseUnifierService {

  private final R4CapabilityTransformer capabilityTransformer;
  private final R4WellKnownTransformer wellKnownTransformer;

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
    super(client, s3ClientWriterService);
    this.capabilityTransformer = capabilityTransformer;
    this.wellKnownTransformer = wellKnownTransformer;
  }

  @Override
  protected Object translateMetadata(final List<String> urlList) {

    List<Capability> capabilityList = new ArrayList<>();
    for (String url : urlList) {
      capabilityList.add(client.search(Query.forType(Capability.class).url(url).build()));
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
