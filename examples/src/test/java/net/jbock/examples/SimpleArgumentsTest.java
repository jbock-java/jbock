package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class SimpleArgumentsTest {

  private ParserTestFixture<SimpleArguments> f =
      ParserTestFixture.create(new SimpleArguments_Parser());

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
        "Usage: simple-arguments [options...]",
        "",
        "  -x, --x",
        "      --file FILE",
        "");
  }
}
