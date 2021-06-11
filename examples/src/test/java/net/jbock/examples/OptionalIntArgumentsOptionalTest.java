package net.jbock.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionalIntArgumentsOptionalTest {

  private final OptionalIntArgumentsOptionalParser parser = new OptionalIntArgumentsOptionalParser();

  @Test
  void testPresent() {
    OptionalIntArgumentsOptional args = parser.parse("-a", "1")
        .orElseThrow(l -> Assertions.<RuntimeException>fail("expecting success but found: " + l));
    assertEquals(OptionalInt.of(1), args.a());
  }

  @Test
  void testAbsent() {
    String[] emptyInput = {};
    OptionalIntArgumentsOptional args = parser.parse(emptyInput)
        .orElseThrow(l -> Assertions.<RuntimeException>fail("expecting success but found: " + l));
    assertEquals(OptionalInt.empty(), args.a());
  }
}
