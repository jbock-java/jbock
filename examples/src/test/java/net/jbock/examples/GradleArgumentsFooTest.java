package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class GradleArgumentsFooTest {

  private ParserTestFixture<GradleArguments.Foo> f =
      ParserTestFixture.create(GradleArguments_Foo_Parser.create());

  @Test
  void testParserForNestedClass() {
    f.assertThat("--bar=4").succeeds("bar", Optional.of(4));
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  GradleArguments",
        "",
        "SYNOPSIS",
        "  GradleArguments [OPTIONS...]",
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
