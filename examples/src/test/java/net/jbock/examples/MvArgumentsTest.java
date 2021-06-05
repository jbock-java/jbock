package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class MvArgumentsTest {

  private final ParserTestFixture<MvArguments> f =
      ParserTestFixture.create(new MvArgumentsParser());

  @Test
  void notEnoughArguments() {
    f.assertThat("a").failsWithMessage("Missing required parameter: \u001B[1mDEST\u001B[m");
  }

  @Test
  void invalidOption() {
    f.assertThat("-aa", "b").failsWithMessage("Invalid option: -aa");
  }

  @Test
  void excessParam() {
    f.assertThat("a", "b", "c").failsWithMessage("Excess param: c");
  }

  @Test
  void invalidOptionEscapeSequenceThird() {
    f.assertThat("a", "b", "--", "c").failsWithMessage("Excess param: c");
  }

  @Test
  void validInvocation() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
  }

  @Test
  void valid() {
    f.assertThat("a", "b").succeeds(
        "source", "a",
        "dest", "b");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "\u001B[1mUSAGE\u001B[m",
        "  mv-arguments SOURCE DEST",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  SOURCE ",
        "  DEST   ",
        "");
  }
}
