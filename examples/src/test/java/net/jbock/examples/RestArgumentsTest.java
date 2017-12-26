package net.jbock.examples;

import static java.util.Arrays.asList;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class RestArgumentsTest {

  private final ParserFixture<RestArguments> f =
      ParserFixture.create(RestArguments_Parser::parse);

  @Test
  public void testDashAllowed() {
    f.assertThat("-", "a").succeeds("rest", asList("-", "a"));
  }

  @Test
  public void testDoubleDashAllowed() {
    // -- has no special meaning, because there's only one positional list
    f.assertThat("--", "a").succeeds("rest", asList("--", "a"));
  }

  @Test
  public void testMixed() {
    f.assertThat("--file=1", "--file", "2", "-", "a").succeeds(
        "file", asList("1", "2"),
        "rest", asList("-", "a"));
    f.assertThat("--file=1", "--file", "2", "--", "a").succeeds(
        "file", asList("1", "2"),
        "rest", asList("--", "a"));
  }

  @Test
  public void testPrint() {
    f.assertPrints(
        "NAME",
        "  RestArguments",
        "",
        "SYNOPSIS",
        "  RestArguments [OPTION]... [REST]...",
        "",
        "DESCRIPTION",
        "",
        "  --file VALUE...",
        "",
        "");
  }
}
