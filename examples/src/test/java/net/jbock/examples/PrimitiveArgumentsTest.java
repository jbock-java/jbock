package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimitiveArgumentsTest {

  private ParserTestFixture<PrimitiveArguments> f =
      ParserTestFixture.create(PrimitiveArguments_Parser.create());

  @Test
  void simpleTest() {
    PrimitiveArguments parsed = f.parse(
        "-B", "1",
        "-S", "1",
        "-I", "1",
        "-L", "1",
        "-F", "1",
        "-D", "1",
        "-C", "A");
    assertEquals(1, parsed.simpleByte());
    assertEquals(1, parsed.simpleShort());
    assertEquals(1, parsed.simpleInt());
    assertEquals(1, parsed.simpleLong());
    assertEquals(1, parsed.simpleFloat());
    assertEquals(1, parsed.simpleDouble());
    assertEquals('A', parsed.simpleChar());
  }
}