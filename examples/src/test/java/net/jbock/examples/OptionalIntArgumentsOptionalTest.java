package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

class OptionalIntArgumentsOptionalTest {

  private final OptionalIntArgumentsOptionalParser parser = new OptionalIntArgumentsOptionalParser();

  private final ParserTestFixture<OptionalIntArgumentsOptional> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testPresent() {
    f.assertThat("-a", "1").succeeds("a", OptionalInt.of(1));
  }

  @Test
  void testAbsent() {
    f.assertThat(/* empty */).succeeds("a", OptionalInt.empty());
  }
}
