package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.jupiter.api.Test;

public class PsArgumentsTest {

  @Test
  public void testPrint() {
    ParserFixture.create(PsArguments_Parser::parse).assertPrints(
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
