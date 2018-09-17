package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComplicatedMapperArgumentsTest {

  private ParserTestFixture<ComplicatedMapperArguments> f =
      ParserTestFixture.create(ComplicatedMapperArguments_Parser.create());

  @Test
  void number() {
    ComplicatedMapperArguments parsed = f.parse("--number", "12");
    assertEquals(1, parsed.number().intValue());
  }
}