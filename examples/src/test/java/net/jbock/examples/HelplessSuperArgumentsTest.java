package net.jbock.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HelplessSuperArgumentsTest {

  private final HelplessSuperArguments_Parser parser = new HelplessSuperArguments_Parser();

  @Test
  void testHelpDisabled() {
    HelplessSuperArguments_Parser.ParseResult result = parser.parse(new String[]{"--help"});
    Assertions.assertTrue(result instanceof HelplessSuperArguments_Parser.ParsingFailed);
  }
}
