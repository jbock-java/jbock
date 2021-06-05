package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PsArgumentsTest {

  private final ParserTestFixture<PsArguments> f =
      ParserTestFixture.create(new PsArgumentsParser());

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  ps-arguments [OPTION]...",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -a, --all         ",
        "  -w, --width WIDTH  This is the description.",
        "");
  }
}
