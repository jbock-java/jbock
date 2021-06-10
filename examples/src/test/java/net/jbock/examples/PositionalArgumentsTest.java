package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionalArgumentsTest {

  private final PositionalArgumentsParser parser = new PositionalArgumentsParser();

  private final ParserTestFixture<PositionalArguments> f =
      ParserTestFixture.create(parser);

  @Test
  void errorMissingParameters() {
    assertTrue(parser.parse("a").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter DEST"));
    assertTrue(parser.parse("a", "b").getLeft().map(f::castToError)
        .orElseThrow().message()
        .contains("Missing required parameter ANOTHER_INT"));
  }

  @Test
  void testRequiredOnly() {
    f.assertThat("a", "b", "1").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", Optional.empty(),
        "otherTokens", emptyList());
  }

  @Test
  void testEmpty() {
    f.assertThat("", "", "0").succeeds(
        "source", "",
        "dest", "",
        "anotherInt", 0,
        "optString", Optional.empty(),
        "otherTokens", emptyList());
  }

  @Test
  void testNoEscape() {
    f.assertThat("a", "b", "1", "c", "d").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", Optional.of("c"),
        "otherTokens", singletonList("d"));
  }

  @Test
  void testEscapeAllOPPositions() {
    f.assertThat("--", "-a", "-b", "-1", "-c", "-e").succeeds(
        "source", "-a",
        "dest", "-b",
        "anotherInt", -1,
        "optString", Optional.of("-c"),
        "otherTokens", singletonList("-e"));
    f.assertThat("a", "--", "-b", "-1", "-c", "-e").succeeds(
        "source", "a",
        "dest", "-b",
        "anotherInt", -1,
        "optString", Optional.of("-c"),
        "otherTokens", singletonList("-e"));
    f.assertThat("a", "b", "--", "-1", "-c", "-e").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", -1,
        "optString", Optional.of("-c"),
        "otherTokens", singletonList("-e"));
    f.assertThat("a", "b", "1", "--", "-c", "-e").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", Optional.of("-c"),
        "otherTokens", singletonList("-e"));
    f.assertThat("a", "b", "1", "c", "--", "-e").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", Optional.of("c"),
        "otherTokens", singletonList("-e"));
    f.assertThat("a", "b", "1", "c", "e", "--").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", Optional.of("c"),
        "otherTokens", singletonList("e"));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "USAGE",
        "  positional-arguments SOURCE DEST ANOTHER_INT [OPT_STRING] OTHER_TOKENS...",
        "",
        "PARAMETERS",
        "  SOURCE       ",
        "  DEST          Desc of dest.",
        "  ANOTHER_INT  ",
        "  OPT_STRING   ",
        "  OTHER_TOKENS ",
        "");
  }
}
