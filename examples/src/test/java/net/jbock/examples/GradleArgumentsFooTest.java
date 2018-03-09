package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class GradleArgumentsFooTest {

  private final ParserFixture<GradleArguments.Foo> f =
      ParserFixture.create(GradleArguments_Foo_Parser::parse);

  @Test
  public void testParserForNestedClass() {
    f.assertThat("--bar=4").succeeds("bar", 4);
  }


  @Test
  public void testPrint() {
    f.assertPrints(
        "NAME",
        "  GradleArguments",
        "",
        "SYNOPSIS",
        "  GradleArguments [OPTION]...",
        "",
        "DESCRIPTION",
        "",
        "  --bar NUMBER",
        "",
        "");
  }
}
