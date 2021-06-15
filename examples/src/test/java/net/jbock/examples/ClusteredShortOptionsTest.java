package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class ClusteredShortOptionsTest {

  private final ClusteredShortOptionsParser parser = new ClusteredShortOptionsParser();

  private final ParserTestFixture<ClusteredShortOptions> f = ParserTestFixture.create(parser::parse);

  @Test
  void testAttached() {
    f.assertThat("-abcfInputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
  }


  @Test
  void testSurprise() {
    f.assertThat("-abcf=InputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "=InputFile.txt"); // !
  }

  @Test
  void testAa() {
    f.assertThat("--aa", "-bcfInputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
  }

  @Test
  void testDetached() {
    f.assertThat("-abcf", "InputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
  }

  @Test
  void testClustering() {
    f.assertThat("-abc", "-fInputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
    f.assertThat("-ab", "-cfInputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
    f.assertThat("-a", "-b", "-c", "-fInputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
    f.assertThat("-a", "-b", "-c", "-f", "InputFile.txt").succeeds(
        "aaa", true,
        "bbb", true,
        "ccc", true,
        "file", "InputFile.txt");
  }
}