package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;

class GradleArgumentsFooTest {

  private final GradleArguments_FooParser parser = new GradleArguments_FooParser();

  private final ParserTestFixture<GradleArguments.Foo> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testParserForNestedClass() {
    f.assertThat("--bar=4").succeeds("bar", Optional.of(4));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "USAGE",
        "  foo [OPTIONS]",
        "",
        "OPTIONS",
        "  --bar BAR ",
        "");
  }
}
