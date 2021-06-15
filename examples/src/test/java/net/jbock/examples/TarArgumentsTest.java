package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void flagWithArgument() {
    assertTrue(parser.parse("-xf").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing argument after token: -f"));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
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
