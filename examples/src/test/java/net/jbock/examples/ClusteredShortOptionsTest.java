package net.jbock.examples;

import net.jbock.examples.ClusteredShortOptions_Parser.ParseResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClusteredShortOptionsTest {

  @Test
  void testClustering() {
    assertAllSet(parse("-abcfInputFile.txt"));
    assertAllSet(parse("-abc", "-fInputFile.txt"));
    assertAllSet(parse("-ab", "-cfInputFile.txt"));
    assertAllSet(parse("-a", "-b", "-c", "-fInputFile.txt"));
    assertAllSet(parse("-a", "-b", "-c", "-f", "InputFile.txt"));
  }

  private void assertAllSet(ClusteredShortOptions options) {
    Assertions.assertTrue(options.aaa());
    Assertions.assertTrue(options.bbb());
    Assertions.assertTrue(options.ccc());
    Assertions.assertEquals("InputFile.txt", options.file());
  }

  private ClusteredShortOptions parse(String... args) {
    ParseResult result = new ClusteredShortOptions_Parser().parse(args);
    if (!(result instanceof ClusteredShortOptions_Parser.ParsingSuccess)) {
      Assertions.fail("success expected but was " + result);
    }
    return ((ClusteredShortOptions_Parser.ParsingSuccess) result).getResult();
  }
}