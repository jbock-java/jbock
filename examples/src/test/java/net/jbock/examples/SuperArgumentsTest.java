package net.jbock.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuperArgumentsTest {

  private final SuperArguments_Parser parser = new SuperArguments_Parser();

  @Test
  void testRest() {
    SuperArguments_Parser.SuperArgumentsWithRest success = parseOrFail("-q", "foo", "-a", "1");
    SuperArguments result = success.getResult();
    assertEquals("foo", result.command());
    assertTrue(result.quiet());
    assertArrayEquals(new String[]{"-a", "1"}, success.getRest());
  }

  @Test
  void testEscapeSequenceNotRecognized() {
    SuperArguments_Parser.SuperArgumentsWithRest success = parseOrFail("-q", "--");
    SuperArguments result = success.getResult();
    assertEquals("--", result.command());
    assertTrue(result.quiet());
  }

  @Test
  void testHelp() {
    SuperArguments_Parser.ParseResult result = parser.parse(new String[]{"--help"});
    assertTrue(result instanceof SuperArguments_Parser.HelpRequested);
  }

  private SuperArguments_Parser.SuperArgumentsWithRest parseOrFail(String... args) {
    SuperArguments_Parser.ParseResult result = parser.parse(args);
    assertTrue(result instanceof SuperArguments_Parser.ParsingSuccess);
    return ((SuperArguments_Parser.ParsingSuccess) result).getResultWithRest();
  }
}
