package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class GradleArgumentsFooTest {

  private final ParserTestFixture<GradleArguments.Foo> f =
      ParserTestFixture.create(new GradleArguments_Foo_Parser());

  @Test
  void testParserForNestedClass() {
    f.assertThat("--bar=4").succeeds("bar", Optional.of(4));
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "USAGE",
        "  foo [OPTION]...",
        "",
        "OPTIONS",
        "  --bar BAR ",
        "");
  }
}
