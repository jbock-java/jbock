package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class PositionalArgumentsTest {

  private final ParserTestFixture<PositionalArguments> f =
      ParserTestFixture.create(new PositionalArguments_Parser());

  @Test
  void errorMissingParameters() {
    f.assertThat("a").failsWithMessage("Missing required: DEST");
    f.assertThat("a", "b").failsWithMessage("Missing required: ANOTHER_INT");
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
    f.assertPrintsHelp(
        "USAGE",
        "  positional-arguments <source> <dest> <another_int> [<opt_string>]",
        "        <other_tokens>...",
        "",
        "PARAMETERS",
        "  source       ",
        "  dest          Desc of dest.",
        "  another_int  ",
        "  opt_string   ",
        "  other_tokens ",
        "");
  }
}
