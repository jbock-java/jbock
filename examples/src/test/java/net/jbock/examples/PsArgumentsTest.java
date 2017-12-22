package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class PsArgumentsTest {

  @Test
  public void testPrint() {
    ParserFixture.create(PsArguments_Parser::parse).assertPrints(
        "SYNOPSIS",
        "  [OPTION]...",
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
