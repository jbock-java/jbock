package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class FewerPositionalArgumentsTest {

  private ParserTestFixture<FewerPositionalArguments> f =
      ParserTestFixture.create(FewerPositionalArguments_Parser.create());

  @Test
  void valid() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
  }

  @Test
  void excessOption() {
    f.assertThat("a", "b", "c").failsWithLine1("Invalid option: c");
  }

  @Test
  void excessOptionDoubleDash() {
    // even though escape is allowed, this fails because there's no positional list defined
    f.assertThat("a", "b", "--", "c").failsWithLine1("Invalid option: --");
  }
}
