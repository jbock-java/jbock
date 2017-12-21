package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;

import org.junit.Test;

public class PsArgumentsTest {

  @Test
  public void testPrint() {
    printFixture(PsArguments_Parser::printUsage).assertPrints(
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
