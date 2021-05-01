package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtremelySimpleArgumentsTest {

  private final ParserTestFixture<ExtremelySimpleArguments> f =
      ParserTestFixture.create(new ExtremelySimpleArguments_Parser());

  @Test
  void simpleTest() {
    assertEquals(OptionalInt.of(1), f.parse("1").hello());
  }
}
