package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;

class PsArgumentsTest {

  private final PsArgumentsParser parser = new PsArgumentsParser();

  private final ParserTestFixture<PsArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "\u001B[1mUSAGE\u001B[m",
        "  ps-arguments [OPTIONS]",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -a, --all         ",
        "  -w, --width WIDTH  This is the description.",
        "");
  }
}
