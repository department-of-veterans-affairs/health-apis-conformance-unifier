package gov.va.api.health.conformance.unifier.fhir;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Enumeration of currently supported resource types to unify. */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum ResourceTypeEnum {
  UNKNOWN("unknown"),
  DSTU2("dstu2"),
  STU3("stu3"),
  R4("r4");
  private final String type;

  /**
   * Enum from type. If not recognized, UNKNOWN is returned.
   *
   * @param type String representation of resource type.
   * @return Enum or UNKNOWN if not found.
   */
  public static ResourceTypeEnum fromType(final String type) {
    for (ResourceTypeEnum e : ResourceTypeEnum.values()) {
      if (String.valueOf(e.type).equalsIgnoreCase(type)) {
        return e;
      }
    }
    return UNKNOWN;
  }
}
