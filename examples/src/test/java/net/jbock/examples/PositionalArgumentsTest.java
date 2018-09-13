package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class PositionalArgumentsTest {

  private ParserTestFixture<PositionalArguments> f =
      ParserTestFixture.create(PositionalArguments_Parser.create());

  @Test
  void errorMissingParameters() {
    f.assertThat().failsWithLine4("Missing parameter: <SOURCE>");
    f.assertThat("a").failsWithLine4("Missing parameter: <DEST>");
    f.assertThat("a", "b").failsWithLine4("Missing parameter: <ANOTHER_INT>");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b", "1").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", null,
        "otherTokens", emptyList());
  }

  @Test
  void otherTokens() {
    f.assertThat("a", "b", "1", "c", "d").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", "c",
        "otherTokens", singletonList("d"));
  }

  @Test
  void ddTokens() {
    f.assertThat("a", "b", "1", "c", "d", "--", "e").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", "c",
        "otherTokens", asList("d", "e"));
    f.assertThat("a", "b", "1", "c", "--", "e").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", "c",
        "otherTokens", singletonList("e"));
    f.assertThat("a", "b", "1", "c", "--").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", "c",
        "otherTokens", emptyList());
    f.assertThat("a", "b", "1", "c").succeeds(
        "source", "a",
        "dest", "b",
        "anotherInt", 1,
        "optString", "c",
        "otherTokens", emptyList());
  }

  @Test
  void testPrint() {
    f.assertPrints(
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
