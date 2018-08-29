package net.jbock.examples;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class PositionalArgumentsTest {

  private ParserTestFixture<PositionalArguments> f =
      ParserTestFixture.create(PositionalArguments_Parser.create());

  @Test
  void errorMissingParameters() {
    f.assertThat().failsWithLine1("Missing parameter: SOURCE");
    f.assertThat("a").failsWithLine1("Missing parameter: DEST");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b").succeeds(
        "optString", null,
        "source", "a",
        "dest", "b",
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void otherTokens() {
    f.assertThat("a", "b", "c", "d").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"),
        "ddTokens", emptyList());
  }

  @Test
  void ddTokens() {
    f.assertThat("a", "b", "c", "d", "--", "e").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"),
        "ddTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--", "e").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", emptyList(),
        "ddTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
    f.assertThat("a", "b", "c").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
    f.assertThat("a", "b").succeeds(
        "optString", null,
        "source", "a",
        "dest", "b",
        "otherTokens", emptyList(),
        "ddTokens", emptyList());
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  PositionalArguments",
        "",
        "SYNOPSIS",
        "  PositionalArguments SOURCE DEST [OPT_STRING] [OTHER_TOKENS]... [-- DD_TOKENS...]",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
