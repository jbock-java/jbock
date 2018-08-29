package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class SimpleArgumentsTest {

  private ParserTestFixture<SimpleArguments> f =
      ParserTestFixture.create(SimpleArguments_Parser.create());

  @Test
  void invalidOptions() {
    f.assertThat("xf", "1").failsWithLine1("Invalid option: xf");
    f.assertThat("-xf", "1").failsWithLine1("Invalid option: -xf");
  }

  @Test
  void success() {
    f.assertThat("--file", "1").succeeds("file", "1", "extract", false);
  }

  @Test
  void errorHelpNotFirstArguent() {
    f.assertThat("--file", "1", "--help").failsWithLines(
        "Usage: SimpleArguments [OPTION]...",
        "Invalid option: --help",
        "Try 'SimpleArguments --help' for more information.",
        "");
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  SimpleArguments",
        "",
        "SYNOPSIS",
        "  SimpleArguments [OPTION]...",
        "",
        "DESCRIPTION",
        "",
        "  -x, --extract",
        "",
        "  --file VALUE",
        "",
        "");
  }
}
