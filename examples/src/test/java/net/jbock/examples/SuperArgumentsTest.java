package net.jbock.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SuperArgumentsTest {

  private final SuperArguments_Parser parser = new SuperArguments_Parser();

  @Test
  void testRest() {
    SuperArguments_Parser.SuperArgumentsWithRest success = parseOrFail("-q", "foo", "-a", "1");
    SuperArguments result = success.getResult();
    Assertions.assertEquals("foo", result.command());
    Assertions.assertTrue(result.quiet());
    Assertions.assertArrayEquals(new String[]{"-a", "1"}, success.getRest());
  }

  @Test
  void testEscapeSequenceNotRecognized() {
    SuperArguments_Parser.SuperArgumentsWithRest success = parseOrFail("-q", "--");
    SuperArguments result = success.getResult();
    Assertions.assertEquals("--", result.command());
    Assertions.assertTrue(result.quiet());
  }

  @Test
  void testHelp() {
    SuperArguments_Parser.ParseResult result = parser.parse(new String[]{"--help"});
    Assertions.assertTrue(result instanceof SuperArguments_Parser.HelpRequested);
  }

  private SuperArguments_Parser.SuperArgumentsWithRest parseOrFail(String... args) {
    SuperArguments_Parser.ParseResult result = parser.parse(args);
    Assertions.assertTrue(result instanceof SuperArguments_Parser.ParsingSuccess);
    return ((SuperArguments_Parser.ParsingSuccess) result).getResultWithRest();
  }
}
