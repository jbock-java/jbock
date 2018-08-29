package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PsArgumentsTest {

  private ParserTestFixture<PsArguments> f =
          ParserTestFixture.create(PsArguments_Parser.create());

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  PsArguments",
        "",
        "SYNOPSIS",
        "  PsArguments [OPTION]...",
        "",
        "DESCRIPTION",
        "",
        "  -a, --all",
        "",
        "  -w, --wide NUMBER",
        "",
        "");
  }
}
