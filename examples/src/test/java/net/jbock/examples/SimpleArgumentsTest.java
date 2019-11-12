package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class SimpleArgumentsTest {

  private ParserTestFixture<SimpleArguments> f =
      ParserTestFixture.create(SimpleArguments_Parser.create());

  @Test
  void invalidOptions() {
    f.assertThat("xf", "1").failsWithUsageMessage("Invalid option: xf");
    f.assertThat("-xf", "1").failsWithUsageMessage("Invalid option: -xf");
  }

  @Test
  void success() {
    f.assertThat("--file", "1").succeeds("extract", false, "file", Optional.of("1"));
  }

  @Test
  void errorHelpNotFirstArgument() {
    f.assertThat("--file", "1", "--help").failsWithLines(
        "Usage:",
        "  SimpleArguments [OPTIONS...]",
        "",
        "Error:",
        "  Invalid option: --help",
        "",
        "Try '--help' for more information.",
        "",
        "");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  SimpleArguments",
        "",
        "SYNOPSIS",
        "  SimpleArguments [OPTIONS...]",
        "",
        "DESCRIPTION",
        "",
        "OPTIONS",
        "  -x",
        "",
        "  --file <file>",
        "",
        "  --help",
        "    print online help",
        "",
        "");
  }
}
