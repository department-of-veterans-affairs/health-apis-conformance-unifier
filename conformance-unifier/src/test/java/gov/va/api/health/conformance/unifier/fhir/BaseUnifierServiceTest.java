package gov.va.api.health.conformance.unifier.fhir;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.conformance.unifier.awss3.AmazonS3ClientWriterService;
import gov.va.api.health.conformance.unifier.client.Query;
import gov.va.api.health.conformance.unifier.fhir.dstu2.Dstu2ConformanceTransformer;
import gov.va.api.health.conformance.unifier.fhir.dstu2.Dstu2WellKnownTransformer;
import gov.va.api.health.dstu2.api.information.WellKnown;
import gov.va.api.health.dstu2.api.resources.Conformance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BaseUnifierServiceTest {

  private final List<String> urlList = new ArrayList<>();

  private final Map<String, String> metadataMap = new HashMap<>();

  @Mock private AmazonS3ClientWriterService s3ClientWriterService;

  @Mock private Dstu2ConformanceTransformer conformanceTransformer;

  @Mock private Dstu2WellKnownTransformer wellKnownTransformer;

  private BaseUnifierService<Conformance, WellKnown> service;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);
    service =
        new BaseUnifierService<Conformance, WellKnown>(
            null, conformanceTransformer, wellKnownTransformer, s3ClientWriterService) {

          @Override
          protected Query<WellKnown> queryWellKnown(String url) {
            return null;
          }

          @Override
          protected Query<Conformance> queryMetadata(String url) {
            return null;
          }
        };
  }

  /** Test that the metadata unification uses the expected content type. */
  @Test
  public void unifyMetadata() {
    when(conformanceTransformer.apply(any())).thenReturn(Conformance.builder().build());
    service.unify(ResourceTypeEnum.DSTU2, EndpointTypeEnum.METADATA.type(), urlList, metadataMap);
    verify(s3ClientWriterService).writeToBucket(any(), any(), any(), eq("application/fhir+json"));
  }

  /** Test that well-known unification uses the expected content type. */
  @Test
  public void unifyWellKnown() {
    when(wellKnownTransformer.apply(any())).thenReturn(WellKnown.builder().build());
    service.unify(
        ResourceTypeEnum.DSTU2, EndpointTypeEnum.SMART_CONFIGURATION.type(), urlList, metadataMap);
    verify(s3ClientWriterService).writeToBucket(any(), any(), any(), eq("application/json"));
  }
}
