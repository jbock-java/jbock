package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtremelySimpleArgumentsTest {

  private final ParserTestFixture<ExtremelySimpleArguments> f =
      ParserTestFixture.create(new ExtremelySimpleArgumentsParser());

  @Test
  void simpleTest() {
    assertEquals(Collections.singletonList("1"), f.parse("1").hello());
  }
}
