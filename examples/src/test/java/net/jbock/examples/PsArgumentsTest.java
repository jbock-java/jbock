package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PsArgumentsTest {

  private ParserTestFixture<PsArguments> f =
      ParserTestFixture.create(PsArguments_Parser.create());

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  PsArguments",
        "",
        "SYNOPSIS",
        "  PsArguments [OPTIONS...]",
        "",
        "DESCRIPTION",
        "",
        "OPTIONS",
        "  -a, --all",
        "",
        "  -w <number>, --width <number>",
        "    This is the description.",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
