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
