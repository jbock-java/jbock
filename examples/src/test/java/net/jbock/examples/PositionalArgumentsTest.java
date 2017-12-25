package net.jbock.examples;

import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class PositionalArgumentsTest {

  private final ParserFixture<PositionalArguments> f =
      ParserFixture.create(PositionalArguments_Parser::parse);

  @Test
  public void errorMissingParameters() {
    f.assertThat().failsWithLine1("Missing parameter: SOURCE");
    f.assertThat("a").failsWithLine1("Missing parameter: DEST");
  }

  @Test
  public void minimal() {
    f.assertThat("a", "b").parsesTo(
        "source", "a",
        "dest", "b");
  }

  @Test
  public void otherTokens() {
    f.assertThat("a", "b", "c", "d").parsesTo(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"));
  }

  @Test
  public void ddTokens() {
    f.assertThat("a", "b", "c", "d", "--", "e").parsesTo(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"),
        "ddTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--", "e").parsesTo(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "ddTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--").parsesTo(
        "source", "a",
        "dest", "b",
        "optString", "c");
    f.assertThat("a", "b", "c").parsesTo(
        "source", "a",
        "dest", "b",
        "optString", "c");
    f.assertThat("a", "b").parsesTo(
        "source", "a",
        "dest", "b");
  }

  @Test
  public void testPrint() {
    f.assertPrints(
        "SYNOPSIS",
        "  SOURCE DEST [OPT_STRING] [OTHER_TOKENS]... [-- DD_TOKENS...]",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
