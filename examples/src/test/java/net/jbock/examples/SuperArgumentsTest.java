package net.jbock.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SuperArgumentsTest {

  @Test
  void testRest() {
    SuperArguments_Parser parser = new SuperArguments_Parser();
    SuperArguments_Parser.ParseResult result = parser.parse(new String[]{"-q", "foo", "-a", "1"});
    Assertions.assertTrue(result instanceof SuperArguments_Parser.ParsingSuccess);
    SuperArguments_Parser.ParsingSuccess success = (SuperArguments_Parser.ParsingSuccess) result;
    Assertions.assertEquals("foo", success.getResultWithRest().getResult().command());
    Assertions.assertTrue(success.getResultWithRest().getResult().quiet());
    Assertions.assertArrayEquals(new String[]{"-a", "1"}, success.getResultWithRest().getRest());
  }
}
