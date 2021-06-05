package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MvArgumentsTest {

  private final MvArgumentsParser parser = new MvArgumentsParser();

  private final ParserTestFixture<MvArguments> f =
      ParserTestFixture.create(parser);

  @Test
  void notEnoughArguments() {
    assertTrue(parser.parse("a").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter: \u001B[1mDEST\u001B[m"));
  }

  @Test
  void invalidOption() {
    assertTrue(parser.parse("-aa", "b").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Invalid option: -aa"));
  }

  @Test
  void excessParam() {
    assertTrue(parser.parse("a", "b", "c").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Excess param: c"));
  }

  @Test
  void invalidOptionEscapeSequenceThird() {
    assertTrue(parser.parse("a", "b", "--", "c").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Excess param: c"));
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
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "\u001B[1mUSAGE\u001B[m",
        "  mv-arguments SOURCE DEST",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  SOURCE ",
        "  DEST   ",
        "");
  }
}
