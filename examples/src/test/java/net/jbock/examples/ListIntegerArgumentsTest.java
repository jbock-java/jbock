package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListIntegerArgumentsTest {

  @Test
  void testPresent() {
    ListIntegerArguments args = new ListIntegerArguments_Parser().parseOrExit(new String[]{"-a", "1"});
    assertEquals(Collections.singletonList(1), args.a());
  }

  @Test
  void testAbsent() {
    ListIntegerArguments_Parser.ParseResult result = new ListIntegerArguments_Parser().parse(new String[]{});
    assertTrue(result instanceof ListIntegerArguments_Parser.ParsingFailed);
  }
}