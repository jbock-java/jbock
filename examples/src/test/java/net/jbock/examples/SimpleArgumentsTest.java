package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class SimpleArgumentsTest {

  private final ParserTestFixture<SimpleArguments> f =
      ParserTestFixture.create(new SimpleArgumentsParser());

  @Test
  void invalidOptions() {
    f.assertThat("xf", "1").failsWithMessage("Excess param: xf");
    f.assertThat("-xf", "1").failsWithMessage("Invalid token: -xf");
  }

  @Test
  void success() {
    f.assertThat("--file", "1").succeeds("extract", false, "file", Optional.of("1"));
  }

  @Test
  void errorHelpNotFirstArgument() {
    f.assertThat("--file", "1", "--help").failsWithMessage("Invalid option: --help");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "",
        "\u001B[1mUSAGE\u001B[m",
        "  simple-arguments [OPTION]...",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -x, --x      aa",
        "               AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "               AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "               aa aa",
        "  --file FILE ",
        "");
  }
}
