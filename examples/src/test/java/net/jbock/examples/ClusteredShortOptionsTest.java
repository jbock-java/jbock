package net.jbock.examples;

import net.jbock.util.NotSuccess;
import net.jbock.util.SyntaxError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusteredShortOptionsTest {

  private final ClusteredShortOptionsParser parser = new ClusteredShortOptionsParser();

  @Test
  void testAttached() {
    assertAllSet(parse("-abcfInputFile.txt"));
  }

  @Test
  void testAa() {
    assertAllSet(parse("--aa", "-bcfInputFile.txt"));
  }

  @Test
  void testDetached() {
    assertAllSet(parse("-abcf", "InputFile.txt"));
  }

  @Test
  void testClustering() {
    assertAllSet(parse("-abc", "-fInputFile.txt"));
    assertAllSet(parse("-ab", "-cfInputFile.txt"));
    assertAllSet(parse("-a", "-b", "-c", "-fInputFile.txt"));
    assertAllSet(parse("-a", "-b", "-c", "-f", "InputFile.txt"));
  }

  @Test
  void testNotClustering() {
    Optional<NotSuccess> left = parser.parse("-abcf=InputFile.txt").getLeft();
    Assertions.assertTrue(left.isPresent());
    Assertions.assertTrue(left.get() instanceof SyntaxError);
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