package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class HelplessArgumentsTest {

  private ParserTestFixture<HelplessArguments> f =
      ParserTestFixture.create(HelplessArguments_Parser.create());

  private String[] fullUsage = {
      "NAME",
      "  HelplessArguments",
      "",
      "SYNOPSIS",
      "  HelplessArguments [<options>] <REQUIRED>",
      "",
      "DESCRIPTION",
      "",
      "REQUIRED",
      "",
      "OPTIONS",
      "  --help",
      ""};

  @Test
  void success() {
    f.assertThat("x").succeeds("required", "x", "help", false);
    f.assertThat("x", "--help").succeeds("required", "x", "help", true);
    f.assertThat("--help", "x").succeeds("required", "x", "help", true);
  }

  @Test
  void errorNoArguments() {
    f.assertThat().failsWithLines(append(fullUsage, "Missing parameter: <REQUIRED>", ""));
  }

  @Test
  void errorInvalidOption() {
    f.assertThat("-p").failsWithLines(append(fullUsage, "Invalid option: -p", ""));
  }

  private static String[] append(String[] fullUsage, String... lines) {
    String[] result = Arrays.copyOf(fullUsage, fullUsage.length + lines.length);
    System.arraycopy(lines, 0, result, fullUsage.length, lines.length);
    return result;
  }
}
