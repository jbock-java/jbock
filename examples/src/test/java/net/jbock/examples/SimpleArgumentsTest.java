package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class SimpleArgumentsTest {

  private final ParserFixture<SimpleArguments> f =
      ParserFixture.create(SimpleArguments_Parser::parse);

  @Test
  public void invalidOptions() {
    f.assertThat("xf", "1").failsWithLine1("Invalid option: xf");
    f.assertThat("-xf", "1").failsWithLine1("Invalid option: -xf");
  }

  @Test
  public void success() {
    f.assertThat("--file", "1").succeeds("file", "1");
  }

  @Test
  public void errorHelpNotFirstArguent() {
    f.assertThat("--file", "1", "--help").failsWithLines(
        "Usage: SimpleArguments [OPTION]...",
        "Invalid option: --help",
        "Try 'SimpleArguments --help' for more information.",
        "");
  }

  @Test
  public void testPrint() {
    f.assertPrints(
        "NAME",
        "  SimpleArguments",
        "",
        "SYNOPSIS",
        "  SimpleArguments [OPTION]...",
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
