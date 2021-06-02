package net.jbock.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ClusteredShortOptionsTest {

  private final ClusteredShortOptionsParser parser = new ClusteredShortOptionsParser();

  @Test
  void testClustering() {
    assertAllSet(parse("-abcfInputFile.txt"));
    assertAllSet(parse("-abc", "-fInputFile.txt"));
    assertAllSet(parse("-ab", "-cfInputFile.txt"));
    assertAllSet(parse("-a", "-b", "-c", "-fInputFile.txt"));
    assertAllSet(parse("-a", "-b", "-c", "-f", "InputFile.txt"));
  }

  private void assertAllSet(ClusteredShortOptions options) {
    assertTrue(options.aaa());
    assertTrue(options.bbb());
    assertTrue(options.ccc());
    assertEquals("InputFile.txt", options.file());
  }

  private ClusteredShortOptions parse(String... args) {
    return parser.parse(args)
        .orElseThrow(notSuccess -> Assertions.<RuntimeException>fail("success expected but was " + notSuccess));
  }
}