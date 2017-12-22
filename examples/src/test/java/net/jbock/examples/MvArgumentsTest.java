package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class MvArgumentsTest {

  private final ParserFixture<MvArguments> f =
      ParserFixture.create(MvArguments_Parser::parse);

  @Test
  public void notEnoughArguments() {
    f.assertThat().fails("Missing parameter: SOURCE");
    f.assertThat("a").fails("Missing parameter: DEST");
  }

  @Test
  public void dashNotIgnored() {
    // see CommandLineArguments.ignoreDashes
    f.assertThat("-aa", "b").fails("Invalid option: -aa");
  }

  @Test
  public void tooManyPositionalArguments() {
    f.assertThat("a", "b", "c").fails("Invalid option: c");
  }

  @Test
  public void validInvocation() {
    f.assertThat("a", "b").parsesTo(
        "source", "a",
        "dest", "b");
  }

  @Test
  public void testPrint() {
    f.assertPrints(
        "SYNOPSIS",
        "  SOURCE DEST",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
