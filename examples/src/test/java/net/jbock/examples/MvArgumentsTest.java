package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class MvArgumentsTest {

  private final ParserFixture<MvArguments> f =
      ParserFixture.create(MvArguments_Parser::parse);

  @Test
  public void notEnoughArguments() {
    f.assertThat().isInvalid("Missing parameter: SOURCE");
    f.assertThat("a").isInvalid("Missing parameter: DEST");
  }

  @Test
  public void dashNotIgnored() {
    // see CommandLineArguments.ignoreDashes
    f.assertThat("-aa", "b").isInvalid("Invalid option: -aa");
  }

  @Test
  public void tooManyPositionalArguments() {
    f.assertThat("a", "b", "c").isInvalid("Invalid option: c");
  }

  @Test
  public void validInvocation() {
    f.assertThat("a", "b").isParsedAs(
        "source", "a",
        "dest", "b");
  }

  @Test
  public void testPrint() {
    printFixture(MvArguments_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  SOURCE DEST",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
