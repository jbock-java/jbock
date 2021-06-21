package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class TarArgumentsTest {

  private final TarArgumentsParser parser = new TarArgumentsParser();

  private final ParserTestFixture<TarArguments> f =
      ParserTestFixture.create(parser::parse);

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
  void unfinishedUnixGroup() {
    f.assertThat("-xf")
        .fails("Missing argument after option name: -f");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  tar-arguments [OPTIONS] -f FILE",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -x, --x         ",
        "  -c, --c         ",
        "  -v, --v         ",
        "  -z, --z         ",
        "  -f, --file FILE ",
        "");
  }
}
