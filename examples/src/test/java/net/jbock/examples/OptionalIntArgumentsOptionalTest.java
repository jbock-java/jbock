package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntArgumentsOptionalTest {

  @Test
  void testPresent() {
    OptionalIntArgumentsOptional args = OptionalIntArgumentsOptional_Parser.create().parseOrExit(new String[]{"-a", "1"});
    assertEquals(OptionalInt.of(1), args.a());
  }

  @Test
  void testAbsent() {
    OptionalIntArgumentsOptional args = OptionalIntArgumentsOptional_Parser.create().parseOrExit(new String[]{});
    assertEquals(OptionalInt.empty(), args.a());
  }
}
