package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntArgumentsTest {

  @Test
  void testPresent() {
    OptionalIntArguments args = OptionalIntArguments_Parser.create().parseOrExit(new String[]{"-a", "1"});
    assertEquals(OptionalInt.of(1), args.a());
  }

  @Test
  void testAbsent() {
    OptionalIntArguments_Parser.ParseResult result = OptionalIntArguments_Parser.create().parse(new String[]{});
    assertTrue(result instanceof OptionalIntArguments_Parser.ParsingFailed);
  }
}
