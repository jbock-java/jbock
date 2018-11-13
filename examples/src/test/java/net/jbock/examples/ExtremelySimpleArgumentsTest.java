package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtremelySimpleArgumentsTest {

  private ParserTestFixture<ExtremelySimpleArguments> f =
      ParserTestFixture.create(ExtremelySimpleArguments_Parser.create());

  @Test
  void simpleTest() {
    assertTrue(f.parse("true").hello());
    assertFalse(f.parse("false").hello());
  }
}