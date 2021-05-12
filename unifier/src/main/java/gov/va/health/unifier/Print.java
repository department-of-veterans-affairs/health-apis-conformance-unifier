package gov.va.health.unifier;

import lombok.experimental.UtilityClass;
import picocli.CommandLine.Help.Ansi;

@UtilityClass
public class Print {
  public static void println(String ansiFormat, Object... args) {
    System.out.println(Ansi.AUTO.string(String.format(ansiFormat, args)));
  }
}
