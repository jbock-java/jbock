package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class RestArgumentsTest {

  private ParserTestFixture<RestArguments> f =
      ParserTestFixture.create(RestArguments_Parser.create());

  @Test
  void testDashAllowed() {
    f.assertThat("-", "a").succeeds("rest", asList("-", "a"), "file", emptyList());
  }

  @Test
  void testDoubleDashAllowed() {
    // -- has no special meaning, because there's only one positional list
    f.assertThat("--", "a").succeeds("rest", asList("--", "a"), "file", emptyList());
  }

  @Test
  void testMixed() {
    f.assertThat("--file=1", "--file", "2", "-", "a").succeeds(
        "file", asList("1", "2"),
        "rest", asList("-", "a"));
    f.assertThat("--file=1", "--file", "2", "--", "a").succeeds(
        "file", asList("1", "2"),
        "rest", asList("--", "a"));
  }

  @Test
  void testPrint() {
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
