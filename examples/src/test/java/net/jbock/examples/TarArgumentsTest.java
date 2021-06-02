package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class TarArgumentsTest {

  private final ParserTestFixture<TarArguments> f =
      ParserTestFixture.create(new TarArgumentsParser());

  @Test
  void testExtract() {
    f.assertThat("-x", "-f", "foo.tar").succeeds(
        "extract", true,
        "create", false,
        "verbose", false,
        "compress", false,
        "file", "foo.tar");
    f.assertThat("-v", "-x", "-f", "foo.tar").succeeds(
        "extract", true,
        "create", false,
        "verbose", true,
        "compress", false,
        "file", "foo.tar");
  }

  @Test
  void flagWithArgument() {
    f.assertThat("-xf").failsWithMessage("Missing argument after token: -f");
    f.assertThat("--x=f").failsWithMessage("Invalid token: --x=f");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "USAGE",
        "  tar-arguments [OPTION]... -f FILE",
        "",
        "OPTIONS",
        "  -x, --x         ",
        "  -c, --c         ",
        "  -v, --v         ",
        "  -z, --z         ",
        "  -f, --file FILE ",
        "");
  }
}
