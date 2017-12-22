package net.jbock.examples;

import static java.util.Collections.singletonList;
import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class PositionalArgumentsTest {

  private final ParserFixture<PositionalArguments> f =
      ParserFixture.create(PositionalArguments_Parser::parse);

  @Test
  public void errorMissingParameters() {
    f.assertThat().isInvalid("Missing parameter: SOURCE");
    f.assertThat("a").isInvalid("Missing parameter: DEST");
  }

  @Test
  public void minimal() {
    f.assertThat("a", "b").isParsedAs(
        "source", "a",
        "dest", "b");
  }

  @Test
  public void otherTokens() {
    f.assertThat("a", "b", "c", "d").isParsedAs(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"));
  }

  @Test
  public void ddTokens() {
    f.assertThat("a", "b", "c", "d", "--", "e").isParsedAs(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "otherTokens", singletonList("d"),
        "ddTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--", "e").isParsedAs(
        "source", "a",
        "dest", "b",
        "optString", "c",
        "ddTokens", singletonList("e"));
    f.assertThat("a", "b", "c", "--").isParsedAs(
        "source", "a",
        "dest", "b",
        "optString", "c");
    f.assertThat("a", "b", "c").isParsedAs(
        "source", "a",
        "dest", "b",
        "optString", "c");
    f.assertThat("a", "b").isParsedAs(
        "source", "a",
        "dest", "b");
  }

  @Test
  public void testPrint() {
    printFixture(PositionalArguments_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  SOURCE DEST [OPT_STRING] [OTHER_TOKENS]... [-- DD_TOKENS...]",
        "",
        "DESCRIPTION",
        "",
        "");
  }
}
