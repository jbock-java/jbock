package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtremelySimpleArgumentsTest {

  private ParserTestFixture<ExtremelySimpleArguments> f =
      ParserTestFixture.create(ExtremelySimpleArguments_Parser.create());

  @Test
  void simpleTest() {
    assertEquals(1, f.parse("1").hello());
  }
}
