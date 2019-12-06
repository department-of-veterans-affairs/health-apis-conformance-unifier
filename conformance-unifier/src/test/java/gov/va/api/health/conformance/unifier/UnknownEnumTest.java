package gov.va.api.health.conformance.unifier;

import static org.junit.Assert.assertEquals;

import gov.va.api.health.conformance.unifier.fhir.EndpointTypeEnum;
import gov.va.api.health.conformance.unifier.fhir.ResourceTypeEnum;
import org.junit.Test;

/** Test enum value lookup default values. */
public class UnknownEnumTest {

  /** Unrecognized Endpoint Type value should be UNKNOWN. */
  @Test
  public void unknownEndpointTypeEnumTest() {
    assertEquals(EndpointTypeEnum.UNKNOWN, EndpointTypeEnum.fromType("hello"));
  }

  /** Unrecognized Resource Type value should be UNKNOWN. */
  @Test
  public void unknownResourceTypeEnumTest() {
    assertEquals(ResourceTypeEnum.UNKNOWN, ResourceTypeEnum.fromType("world"));
  }
}
