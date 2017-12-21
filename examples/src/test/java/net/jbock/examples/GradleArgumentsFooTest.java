package net.jbock.examples;

import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class GradleArgumentsFooTest {

  private final ParserFixture<GradleArguments.Foo> f =
      ParserFixture.create(GradleArguments_Foo_Parser::parse);

  @Test
  public void testParserForNestedClass() {
    f.assertThat("--bar=4").isParsedAs("bar", 4);
  }


  @Test
  public void testPrint() {
    printFixture(GradleArguments_Foo_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  [OPTION]...",
        "",
        "DESCRIPTION",
        "",
        "  --bar NUMBER",
        "",
        "");
  }
}
