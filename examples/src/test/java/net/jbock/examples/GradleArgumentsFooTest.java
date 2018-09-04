package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class GradleArgumentsFooTest {

  private ParserTestFixture<GradleArguments.Foo> f =
      ParserTestFixture.create(GradleArguments_Foo_Parser.create());

  @Test
  void testParserForNestedClass() {
    f.assertThat("--bar=4").succeeds("bar", 4);
  }


  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  GradleArguments",
        "",
        "SYNOPSIS",
        "  GradleArguments [<options>]",
        "",
        "DESCRIPTION",
        "",
        "OPTIONS",
        "  --bar <bar>",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
