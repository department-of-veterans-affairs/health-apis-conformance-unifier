package gov.va.api.health.conformance.unifier.fhir;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Enumeration of currently supported endpoint types to unify. */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum EndpointTypeEnum {
  UNKNOWN("unknown"),
  METADATA("metadata"),
  SMART_CONFIGURATION("smart-configuration");
  private final String type;

  /**
   * Enum from type. If not recognized, UNKNOWN is returned.
   *
   * @param type String representation of endpoint type.
   * @return Enum or UNKNOWN if not found.
   */
  public static EndpointTypeEnum fromType(final String type) {
    for (EndpointTypeEnum e : EndpointTypeEnum.values()) {
      if (String.valueOf(e.type).equalsIgnoreCase(type)) {
        return e;
      }
    }
    return UNKNOWN;
  }
}
