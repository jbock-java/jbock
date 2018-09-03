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
    f.assertThat().failsWithLine1("Missing parameter: SOURCE");
    f.assertThat("a").failsWithLine1("Missing parameter: DEST");
  }

  @Test
  void minimal() {
    f.assertThat("a", "b").succeeds(
        "optString", null,
        "source", "a",
        "dest", "b",
        "otherTokens", emptyList());
  }

  @Test
  void otherTokens() {
    f.assertThat("a", "b", "c", "d").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"));
  }

  @Test
  void ddTokens() {
    f.assertThat("a", "b", "c", "d", "--", "e").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", asList("d", "e"));
    f.assertThat("a", "b", "c", "--", "e").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", emptyList());
    f.assertThat("a", "b", "c").succeeds(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", emptyList());
    f.assertThat("a", "b").succeeds(
        "optString", null,
        "source", "a",
        "dest", "b",
        "otherTokens", emptyList());
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  PositionalArguments",
        "",
        "SYNOPSIS",
        "  PositionalArguments SOURCE DEST [OPT_STRING] [OTHER_TOKENS]...",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
