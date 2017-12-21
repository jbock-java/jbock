package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class SimpleArgumentsTest {

  private final ParserFixture<SimpleArguments> f =
      ParserFixture.create(SimpleArguments_Parser::parse);

  @Test
  public void invalidOptions() {
    f.assertThat("xf", "1").isInvalid("Invalid option: xf");
    f.assertThat("-xf", "1").isInvalid("Invalid option: -xf");
  }

  @Test
  public void testPrint() {
    printFixture(SimpleArguments_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  [OPTION]...",
        "",
        "DESCRIPTION",
        "",
        "  -x, --extract",
        "",
        "  --file VALUE",
        "",
        "");
  }
}
