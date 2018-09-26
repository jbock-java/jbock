package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomCollectorArgumentsTest {

  private ParserTestFixture<CustomCollectorArguments> f =
      ParserTestFixture.create(CustomCollectorArguments_Parser.create());

  @Test
  void testToSet() {
    CustomCollectorArguments parsed = f.parse(
        "-H", "A",
        "-H", "A",
        "-H", "HA");
    assertEquals(new HashSet<>(asList("A", "HA")), parsed.strings());
  }
}