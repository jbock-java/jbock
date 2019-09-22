package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalArgumentsTest {

  @Test
  void testPresent() {
    OptionalArguments args = OptionalArguments_Parser.create().parseOrExit(new String[]{"-a", "1"});
    assertEquals(Optional.of(1), args.a());
  }

  @Test
  void testAbsent() {
    OptionalArguments_Parser.ParseResult result = OptionalArguments_Parser.create().parse(new String[]{});
    assertTrue(result instanceof OptionalArguments_Parser.ParsingFailed);
  }
}