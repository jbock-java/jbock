package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplicatedMapperArgumentsTest {

  private final ParserTestFixture<ComplicatedMapperArguments> f =
      ParserTestFixture.create(new ComplicatedMapperArguments_Parser());

  @Test
  void number() {
    ComplicatedMapperArguments parsed = f.parse(
        "--number", "12",
        "--numbers", "3",
        "--numbers", "oops");
    assertEquals(1, parsed.number().intValue());
    assertEquals(2, parsed.numbers().size());
    assertEquals(Integer.valueOf(3), parsed.numbers().get(0).get());
    assertThrows(NumberFormatException.class, () -> parsed.numbers().get(1).get());
  }
}