package gov.va.api.health.conformance.unifier.fhir.dstu2;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.ConformanceClient;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.BaseUnifierService;
import gov.va.api.health.dstu2.api.information.WellKnown;
import gov.va.api.health.dstu2.api.resources.Conformance;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to facilitate unification of DSTU2 Metadata and WellKnown. */
@Service
public class Dstu2UnifierService extends BaseUnifierService {

  private final Dstu2ConformanceTransformer conformanceTransformer;
  private final Dstu2WellKnownTransformer wellKnownTransformer;

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
      Dstu2WellKnownTransformer wellKnownTransformer) {
    super(client, s3ClientWriterService);
    this.conformanceTransformer = conformanceTransformer;
    this.wellKnownTransformer = wellKnownTransformer;
  }

  @Override
  protected Object translateMetadata(final List<String> urlList) {

    List<Conformance> conformanceList = new ArrayList<>();
    for (String url : urlList) {
      conformanceList.add(client.search(Query.forType(Conformance.class).url(url).build()));
    }

    return conformanceTransformer.apply(conformanceList);
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
