package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PsArgumentsTest {

  @Test
  void testPrint() {
    ParserTestFixture.create(PsArguments_Parser::parse).assertPrints(
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
