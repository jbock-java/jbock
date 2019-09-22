package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntegerArgumentsTest {

  @Test
  void testPresent() {
    OptionalIntegerArguments args = OptionalIntegerArguments_Parser.create().parseOrExit(new String[]{"-a", "1"});
    assertEquals(Optional.of(1), args.a());
  }

  @Test
  void testAbsent() {
    OptionalIntegerArguments_Parser.ParseResult result = OptionalIntegerArguments_Parser.create().parse(new String[]{});
    assertTrue(result instanceof OptionalIntegerArguments_Parser.ParsingFailed);
  }
}