package net.jbock.examples;

import java.util.Arrays;
import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class HelplessArgumentsTest {

  private final ParserFixture<HelplessArguments> f =
      ParserFixture.create(HelplessArguments_Parser::parse);

  private final String[] fullUsage = {
      "NAME",
      "  HelplessArguments",
      "",
      "SYNOPSIS",
      "  HelplessArguments [OPTION]... REQUIRED",
      "",
      "DESCRIPTION",
      "",
      "  --help",
      ""};

  @Test
  public void success() {
    f.assertThat("x").succeeds("required", "x");
    f.assertThat("x", "--help").succeeds("required", "x", "help", true);
    f.assertThat("--help", "x").succeeds("required", "x", "help", true);
  }

  @Test
  public void errorNoArguments() {
    f.assertThat().failsWithLines(append(fullUsage, "Missing parameter: REQUIRED", ""));
  }

  @Test
  public void errorInvalidOption() {
    f.assertThat("-p").failsWithLines(append(fullUsage, "Invalid option: -p", ""));
  }

  private static String[] append(String[] fullUsage, String... lines) {
    String[] result = Arrays.copyOf(fullUsage, fullUsage.length + lines.length);
    System.arraycopy(lines, 0, result, fullUsage.length, lines.length);
    return result;
  }
}
