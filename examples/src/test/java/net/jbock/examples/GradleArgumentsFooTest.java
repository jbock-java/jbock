package net.jbock.examples;

import net.jbock.either.Optional;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class GradleArgumentsFooTest {

  private final GradleArguments_FooParser parser = new GradleArguments_FooParser();

  private final ParserTestFixture<GradleArguments.Foo> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testParserForNestedClass() {
    f.assertThat("--bar=4")
        .has(GradleArguments.Foo::bar, Optional.of(4));
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  foo [OPTIONS]",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  --bar BAR ",
        "");
  }
}
