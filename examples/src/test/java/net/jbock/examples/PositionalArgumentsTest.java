package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class PositionalArgumentsTest {

  private ParserTestFixture<PositionalArguments> f =
      ParserTestFixture.create(PositionalArguments_Parser.create());

  @Test
  void errorMissingParameters() {
    f.assertThat().failsWithUsageMessage("Missing parameter: <SOURCE>");
    f.assertThat("a").failsWithUsageMessage("Missing parameter: <DEST>");
    f.assertThat("a", "b").failsWithUsageMessage("Missing parameter: <ANOTHER_INT>");
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
        "NAME",
        "  PositionalArguments",
        "",
        "SYNOPSIS",
        "  PositionalArguments <SOURCE> <DESTINATION> <ANOTHER_INT> [<opt_string>] [[--] <other_tokens...>]",
        "",
        "DESCRIPTION",
        "",
        "SOURCE",
        "",
        "DESTINATION",
        "  Desc of dest.",
        "",
        "ANOTHER_INT",
        "",
        "OPT_STRING",
        "",
        "OTHER_TOKENS",
        "",
        "OPTIONS",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
